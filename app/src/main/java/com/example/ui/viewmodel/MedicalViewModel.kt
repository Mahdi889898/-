package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.MedicalAiService
import com.example.data.local.AppDatabase
import com.example.data.model.ChatMessage
import com.example.data.model.DoctorPrescription
import com.example.data.model.MessageSender
import com.example.data.model.PatientInfo
import com.example.data.repository.PrescriptionRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "MedicalViewModel"

sealed interface ConsultationUiState {
    data object Idle : ConsultationUiState
    data class Loading(val progressMessage: String) : ConsultationUiState
    data class Success(val prescription: DoctorPrescription) : ConsultationUiState
    data class Error(val message: String) : ConsultationUiState
}

class MedicalViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PrescriptionRepository by lazy {
        val db = AppDatabase.getInstance(application)
        PrescriptionRepository(
            prescriptionDao = db.prescriptionDao(),
            aiService = MedicalAiService()
        )
    }

    // Patient Form State
    val patientInfo = MutableStateFlow(PatientInfo())
    val chiefComplaint = MutableStateFlow("")
    val symptomDuration = MutableStateFlow("۱ تا ۳ روز")
    val severityLevel = MutableStateFlow(5f)
    val selectedSymptoms = MutableStateFlow<Set<String>>(emptySet())

    // Consultation Processing State
    private val _consultationState = MutableStateFlow<ConsultationUiState>(ConsultationUiState.Idle)
    val consultationState: StateFlow<ConsultationUiState> = _consultationState.asStateFlow()

    // Currently Active Prescription (for view & chat)
    private val _activePrescription = MutableStateFlow<DoctorPrescription?>(null)
    val activePrescription: StateFlow<DoctorPrescription?> = _activePrescription.asStateFlow()

    // Chat with Doctor
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isChatStreaming = MutableStateFlow(false)
    val isChatStreaming: StateFlow<Boolean> = _isChatStreaming.asStateFlow()

    // Saved Prescriptions from Room
    val savedPrescriptions: StateFlow<List<DoctorPrescription>> =
        repository.allPrescriptions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun toggleSymptom(symptom: String) {
        val current = selectedSymptoms.value.toMutableSet()
        if (current.contains(symptom)) {
            current.remove(symptom)
        } else {
            current.add(symptom)
        }
        selectedSymptoms.value = current
    }

    fun submitConsultation() {
        val complaint = chiefComplaint.value.trim()
        if (complaint.isBlank() && selectedSymptoms.value.isEmpty()) {
            _consultationState.value = ConsultationUiState.Error("لطفاً شرح علائم بیماری خود را بنویسید یا علائم همراه را انتخاب کنید.")
            return
        }

        viewModelScope.launch {
            _consultationState.value = ConsultationUiState.Loading("در حال بررسی بالینی علائم و تدوین نسخه درمانی هوشمند...")
            try {
                val prescription = repository.diagnoseAndSave(
                    patientInfo = patientInfo.value,
                    chiefComplaint = complaint.ifBlank { selectedSymptoms.value.joinToString("، ") },
                    duration = symptomDuration.value,
                    severity = severityLevel.value.toInt(),
                    additionalSymptoms = selectedSymptoms.value.toList()
                )

                _activePrescription.value = prescription
                _consultationState.value = ConsultationUiState.Success(prescription)

                // Initialize chat with a welcoming doctor message
                _chatMessages.value = listOf(
                    ChatMessage(
                        sender = MessageSender.DOCTOR,
                        text = "سلام ${prescription.patientName} عزیز. نسخه درمانی و توصیه‌های شما آماده شد. چنانچه در مورد نحوه مصرف داروها، تداخلات یا هر نکته دیگری سؤالی دارید، در خدمت شما هستم."
                    )
                )
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Medical consultation timed out", e)
                _consultationState.value = ConsultationUiState.Error("زمان ویزیت هوشمند به پایان رسید. لطفاً اتصال اینترنت خود را بررسی و دوباره تلاش فرمایید.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Consultation failed", e)
                _consultationState.value = ConsultationUiState.Error(
                    e.localizedMessage ?: "خطا در برقراری ارتباط با پزشک هوشمند. لطفاً مجدداً امتحان کنید."
                )
            }
        }
    }

    fun selectPrescription(prescription: DoctorPrescription) {
        _activePrescription.value = prescription
        _chatMessages.value = listOf(
            ChatMessage(
                sender = MessageSender.DOCTOR,
                text = "سلام، در خصوص نسخه ${prescription.prescriptionCode} و داروهای تجویزی آماده پاسخگویی به سؤالات شما هستم."
            )
        )
    }

    fun sendFollowUpQuestion(question: String) {
        val trimmed = question.trim()
        if (trimmed.isBlank() || _isChatStreaming.value) return

        val userMsg = ChatMessage(sender = MessageSender.USER, text = trimmed)
        val updatedList = _chatMessages.value + userMsg
        _chatMessages.value = updatedList

        viewModelScope.launch {
            _isChatStreaming.value = true
            val accumulated = StringBuilder()
            val tempDoctorMsg = ChatMessage(sender = MessageSender.DOCTOR, text = "")
            _chatMessages.value = updatedList + tempDoctorMsg

            try {
                repository.streamDoctorChat(
                    prescriptionContext = _activePrescription.value,
                    history = updatedList,
                    userMessage = trimmed
                ).collect { chunk ->
                    accumulated.append(chunk)
                    _chatMessages.value = updatedList + ChatMessage(
                        id = tempDoctorMsg.id,
                        sender = MessageSender.DOCTOR,
                        text = accumulated.toString()
                    )
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Doctor chat timed out", e)
                _chatMessages.value = updatedList + ChatMessage(
                    sender = MessageSender.DOCTOR,
                    text = "متأسفانه ارتباط با پزشک با تأخیر مواجه شد. لطفاً دوباره سؤالتان را بپرسید."
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Doctor chat error", e)
                _chatMessages.value = updatedList + ChatMessage(
                    sender = MessageSender.DOCTOR,
                    text = "خطایی رخ داد: ${e.localizedMessage ?: "عدم پاسخگویی سرویس"}"
                )
            } finally {
                _isChatStreaming.value = false
            }
        }
    }

    fun deletePrescription(prescription: DoctorPrescription) {
        viewModelScope.launch {
            try {
                repository.deletePrescription(prescription)
                if (_activePrescription.value?.id == prescription.id) {
                    _activePrescription.value = null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting prescription", e)
            }
        }
    }

    fun resetConsultation() {
        chiefComplaint.value = ""
        selectedSymptoms.value = emptySet()
        severityLevel.value = 5f
        _consultationState.value = ConsultationUiState.Idle
    }
}
