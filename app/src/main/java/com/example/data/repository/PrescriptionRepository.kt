package com.example.data.repository

import com.example.data.ai.MedicalAiService
import com.example.data.local.PrescriptionDao
import com.example.data.local.PrescriptionEntity
import com.example.data.model.ChatMessage
import com.example.data.model.DoctorPrescription
import com.example.data.model.PatientInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PrescriptionRepository(
    private val prescriptionDao: PrescriptionDao,
    private val aiService: MedicalAiService
) {
    val allPrescriptions: Flow<List<DoctorPrescription>> =
        prescriptionDao.getAllPrescriptions().map { entities ->
            entities.map { it.toDomainModel() }
        }

    suspend fun diagnoseAndSave(
        patientInfo: PatientInfo,
        chiefComplaint: String,
        duration: String,
        severity: Int,
        additionalSymptoms: List<String>
    ): DoctorPrescription {
        val prescription = aiService.diagnoseAndPrescribe(
            patientInfo = patientInfo,
            chiefComplaint = chiefComplaint,
            symptomDuration = duration,
            severityLevel = severity,
            additionalSymptoms = additionalSymptoms
        )

        // Save to Room DB
        prescriptionDao.insertPrescription(PrescriptionEntity.fromDomainModel(prescription))
        return prescription
    }

    suspend fun getPrescriptionById(id: String): DoctorPrescription? {
        return prescriptionDao.getPrescriptionById(id)?.toDomainModel()
    }

    suspend fun deletePrescription(prescription: DoctorPrescription) {
        prescriptionDao.deletePrescription(PrescriptionEntity.fromDomainModel(prescription))
    }

    fun streamDoctorChat(
        prescriptionContext: DoctorPrescription?,
        history: List<ChatMessage>,
        userMessage: String
    ): Flow<String> {
        return aiService.streamDoctorChat(prescriptionContext, history, userMessage)
    }
}
