package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Medication
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import com.example.data.model.DoctorPrescription
import com.example.ui.screens.ApkGuideDialog
import com.example.ui.screens.ConsultationScreen
import com.example.ui.screens.DoctorChatScreen
import com.example.ui.screens.DrugGuideScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PrescriptionDetailScreen
import com.example.ui.theme.MedicalTealPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MedicalViewModel

enum class AppScreen {
    HOME,
    CONSULTATION,
    PRESCRIPTION_DETAIL,
    DOCTOR_CHAT,
    HISTORY,
    DRUG_GUIDE
}

class MainActivity : ComponentActivity() {

    private val viewModel: MedicalViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mandatory App Check initialization with launch intent
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                // Ensure RTL layout for Persian language
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        DoctorAppNavHost(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun DoctorAppNavHost(viewModel: MedicalViewModel) {
    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
    var showApkDialog by remember { mutableStateOf(false) }

    val activePrescription by viewModel.activePrescription.collectAsState()

    // Handle back button on sub-screens
    if (currentScreen != AppScreen.HOME) {
        BackHandler {
            currentScreen = when (currentScreen) {
                AppScreen.DOCTOR_CHAT -> AppScreen.PRESCRIPTION_DETAIL
                AppScreen.PRESCRIPTION_DETAIL -> AppScreen.HOME
                AppScreen.CONSULTATION -> AppScreen.HOME
                AppScreen.HISTORY -> AppScreen.HOME
                AppScreen.DRUG_GUIDE -> AppScreen.HOME
                AppScreen.HOME -> AppScreen.HOME
            }
        }
    }

    if (showApkDialog) {
        ApkGuideDialog(onDismiss = { showApkDialog = false })
    }

    val showBottomNav = currentScreen in listOf(
        AppScreen.HOME,
        AppScreen.HISTORY,
        AppScreen.DRUG_GUIDE
    )

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    NavigationBarItem(
                        selected = currentScreen == AppScreen.HOME,
                        onClick = { currentScreen = AppScreen.HOME },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == AppScreen.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                contentDescription = "خانه"
                            )
                        },
                        label = { Text("خانه") },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = MedicalTealPrimary.copy(alpha = 0.2f)),
                        modifier = Modifier.testTag("nav_item_home")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.CONSULTATION,
                        onClick = {
                            viewModel.resetConsultation()
                            currentScreen = AppScreen.CONSULTATION
                        },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == AppScreen.CONSULTATION) Icons.Filled.AddCircle else Icons.Outlined.AddCircleOutline,
                                contentDescription = "ویزیت جدید"
                            )
                        },
                        label = { Text("ویزیت جدید") },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = MedicalTealPrimary.copy(alpha = 0.2f)),
                        modifier = Modifier.testTag("nav_item_consultation")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.HISTORY,
                        onClick = { currentScreen = AppScreen.HISTORY },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == AppScreen.HISTORY) Icons.Filled.History else Icons.Outlined.History,
                                contentDescription = "نسخه‌های من"
                            )
                        },
                        label = { Text("نسخه‌ها") },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = MedicalTealPrimary.copy(alpha = 0.2f)),
                        modifier = Modifier.testTag("nav_item_history")
                    )

                    NavigationBarItem(
                        selected = currentScreen == AppScreen.DRUG_GUIDE,
                        onClick = { currentScreen = AppScreen.DRUG_GUIDE },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == AppScreen.DRUG_GUIDE) Icons.Filled.Medication else Icons.Outlined.Medication,
                                contentDescription = "داروها"
                            )
                        },
                        label = { Text("داروها") },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = MedicalTealPrimary.copy(alpha = 0.2f)),
                        modifier = Modifier.testTag("nav_item_drugs")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.HOME -> HomeScreen(
                    viewModel = viewModel,
                    onStartConsultation = { currentScreen = AppScreen.CONSULTATION },
                    onOpenPrescription = { prescription ->
                        viewModel.selectPrescription(prescription)
                        currentScreen = AppScreen.PRESCRIPTION_DETAIL
                    },
                    onOpenDrugGuide = { currentScreen = AppScreen.DRUG_GUIDE },
                    onOpenApkGuide = { showApkDialog = true }
                )

                AppScreen.CONSULTATION -> ConsultationScreen(
                    viewModel = viewModel,
                    onNavigateBack = { currentScreen = AppScreen.HOME },
                    onPrescriptionReady = {
                        currentScreen = AppScreen.PRESCRIPTION_DETAIL
                    }
                )

                AppScreen.PRESCRIPTION_DETAIL -> {
                    if (activePrescription != null) {
                        PrescriptionDetailScreen(
                            prescription = activePrescription!!,
                            onNavigateBack = { currentScreen = AppScreen.HOME },
                            onOpenChat = { currentScreen = AppScreen.DOCTOR_CHAT }
                        )
                    } else {
                        currentScreen = AppScreen.HOME
                    }
                }

                AppScreen.DOCTOR_CHAT -> DoctorChatScreen(
                    viewModel = viewModel,
                    onNavigateBack = { currentScreen = AppScreen.PRESCRIPTION_DETAIL }
                )

                AppScreen.HISTORY -> HistoryScreen(
                    viewModel = viewModel,
                    onOpenPrescription = { prescription ->
                        viewModel.selectPrescription(prescription)
                        currentScreen = AppScreen.PRESCRIPTION_DETAIL
                    }
                )

                AppScreen.DRUG_GUIDE -> DrugGuideScreen(
                    onNavigateBack = { currentScreen = AppScreen.HOME }
                )
            }
        }
    }
}
