package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.DoctorPrescription
import com.example.ui.theme.MedicalAlertRed
import com.example.ui.theme.MedicalNavyHeader
import com.example.ui.theme.MedicalTealPrimary
import com.example.ui.viewmodel.ConsultationUiState
import com.example.ui.viewmodel.MedicalViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ConsultationScreen(
    viewModel: MedicalViewModel,
    onNavigateBack: () -> Unit,
    onPrescriptionReady: (DoctorPrescription) -> Unit,
    modifier: Modifier = Modifier
) {
    val patient by viewModel.patientInfo.collectAsState()
    val complaint by viewModel.chiefComplaint.collectAsState()
    val duration by viewModel.symptomDuration.collectAsState()
    val severity by viewModel.severityLevel.collectAsState()
    val selectedSymptoms by viewModel.selectedSymptoms.collectAsState()
    val uiState by viewModel.consultationState.collectAsState()

    var isProfileExpanded by remember { mutableStateOf(false) }

    val commonSymptomChips = listOf(
        "تب", "لرز", "سردرد", "گلودرد", "سرفه خشک", "سرفه خلط‌دار",
        "تنگی نفس", "دل‌پیچه", "حالت تهوع", "استفراغ", "اسهال",
        "سرگیجه", "بدن‌درد و کوفتگی", "آبریزش بینی", "خارش و کهیر پوستی",
        "سوزش ادرار", "بی‌خوابی و اضطراب"
    )

    val durationOptions = listOf(
        "چند ساعت اخیر",
        "۱ تا ۳ روز",
        "حدود ۱ هفته",
        "بیش از ۲ هفته"
    )

    // Check if successfully generated
    if (uiState is ConsultationUiState.Success) {
        val prescription = (uiState as ConsultationUiState.Success).prescription
        onPrescriptionReady(prescription)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ویزیت و شرح حال بیماری",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("btn_back_consultation")
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MedicalNavyHeader
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Patient Profile Header Card (Collapsible)
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isProfileExpanded = !isProfileExpanded }
                            .testTag("card_patient_profile"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MedicalTealPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (patient.name.isNotBlank()) "مشخصات بیمار: ${patient.name} (${patient.age} ساله)" else "مشخصات بیمار (سن، حساسیت‌ها، بیماری زمینه‌ای)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Icon(
                                    imageVector = if (isProfileExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null
                                )
                            }

                            AnimatedVisibility(visible = isProfileExpanded) {
                                Column(modifier = Modifier.padding(top = 12.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth()) {
                                        OutlinedTextField(
                                            value = patient.name,
                                            onValueChange = { viewModel.patientInfo.value = patient.copy(name = it) },
                                            label = { Text("نام یا نام مستعار") },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("input_patient_name"),
                                            singleLine = true
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        OutlinedTextField(
                                            value = patient.age,
                                            onValueChange = { viewModel.patientInfo.value = patient.copy(age = it) },
                                            label = { Text("سن (سال)") },
                                            modifier = Modifier
                                                .width(100.dp)
                                                .testTag("input_patient_age"),
                                            singleLine = true
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(text = "جنسیت: ", style = MaterialTheme.typography.bodyMedium)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        listOf("آقا", "خانم").forEach { g ->
                                            FilterChip(
                                                selected = patient.gender == g,
                                                onClick = { viewModel.patientInfo.value = patient.copy(gender = g) },
                                                label = { Text(g) },
                                                modifier = Modifier.padding(end = 6.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = patient.knownAllergies,
                                        onValueChange = { viewModel.patientInfo.value = patient.copy(knownAllergies = it) },
                                        label = { Text("حساسیت‌های دارویی (مثلاً پنی‌سیلین، آسپرین)") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("input_patient_allergies"),
                                        singleLine = true
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    OutlinedTextField(
                                        value = patient.chronicConditions,
                                        onValueChange = { viewModel.patientInfo.value = patient.copy(chronicConditions = it) },
                                        label = { Text("بیماری‌های زمینه‌ای (دیابت، فشار خون، آسم، قلبی)") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("input_patient_chronic"),
                                        singleLine = true
                                    )

                                    if (patient.gender == "خانم") {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "بارداری یا دوران شیردهی",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            Switch(
                                                checked = patient.isPregnantOrLactating,
                                                onCheckedChange = {
                                                    viewModel.patientInfo.value = patient.copy(isPregnantOrLactating = it)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Chief Complaint Text Field
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "شرح بیماری و علائم فعلی:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MedicalNavyHeader
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = complaint,
                        onValueChange = { viewModel.chiefComplaint.value = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .testTag("input_chief_complaint"),
                        placeholder = {
                            Text("مثال: از دیروز سردرد شدید ضربان‌دار دارم، چشمام به نور حساس شده و کمی حالت تهوع دارم...")
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Duration Selection
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "مدت زمان شروع علائم:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        durationOptions.forEach { opt ->
                            FilterChip(
                                selected = duration == opt,
                                onClick = { viewModel.symptomDuration.value = opt },
                                label = { Text(opt, style = MaterialTheme.typography.bodySmall) },
                                modifier = Modifier.testTag("chip_duration_$opt")
                            )
                        }
                    }
                }

                // Severity Slider
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "شدت درد یا علائم:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${severity.toInt()} از ۱۰",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (severity >= 7) MedicalAlertRed else MedicalTealPrimary
                        )
                    }
                    Slider(
                        value = severity,
                        onValueChange = { viewModel.severityLevel.value = it },
                        valueRange = 1f..10f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = if (severity >= 7) MedicalAlertRed else MedicalTealPrimary,
                            activeTrackColor = if (severity >= 7) MedicalAlertRed else MedicalTealPrimary
                        ),
                        modifier = Modifier.testTag("slider_severity")
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "خفیف (۱)", style = MaterialTheme.typography.labelSmall)
                        Text(text = "متوسط (۵)", style = MaterialTheme.typography.labelSmall)
                        Text(text = "شدید و غیرقابل تحمل (۱۰)", style = MaterialTheme.typography.labelSmall)
                    }
                }

                // Accompanying Symptoms Chips
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "علائم همراه دیگر (اختیاری):",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        commonSymptomChips.forEach { chip ->
                            val isSelected = selectedSymptoms.contains(chip)
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.toggleSymptom(chip) },
                                label = { Text(chip, style = MaterialTheme.typography.bodySmall) },
                                leadingIcon = if (isSelected) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else null,
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MedicalTealPrimary,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("chip_accompanying_$chip")
                            )
                        }
                    }
                }

                // Error Message if failed
                if (uiState is ConsultationUiState.Error) {
                    item {
                        Spacer(modifier = Modifier.height(14.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .testTag("card_consultation_error"),
                            colors = CardDefaults.cardColors(containerColor = MedicalAlertRed.copy(alpha = 0.12f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = null,
                                    tint = MedicalAlertRed
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "خطا در فرآیند ویزیت",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MedicalAlertRed
                                    )
                                    Text(
                                        text = (uiState as ConsultationUiState.Error).message,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // Submit Button
                item {
                    val isLoading = uiState is ConsultationUiState.Loading
                    Button(
                        onClick = { viewModel.submitConsultation() },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_submit_consultation"),
                        colors = ButtonDefaults.buttonColors(containerColor = MedicalTealPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "در حال تشخیص و تنظیم نسخه...",
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Icon(imageVector = Icons.Default.LocalHospital, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ارسال به پزشک و صدور نسخه دیجیتال",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
