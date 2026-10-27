package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.DoctorPrescription
import com.example.data.model.MedicationItem
import com.example.data.model.UrgencyLevel
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "prescriptions")
data class PrescriptionEntity(
    @PrimaryKey
    val id: String,
    val timestamp: Long,
    val patientName: String,
    val patientAge: String,
    val patientGender: String,
    val symptomsSummary: String,
    val probableDiagnosis: String,
    val clinicalAssessment: String,
    val urgencyLevel: String,
    val urgencyReason: String,
    val medicationsJson: String,
    val homeRemediesJson: String,
    val warningSignsJson: String,
    val specialistReferral: String,
    val doctorAdvice: String,
    val prescriptionCode: String
) {
    fun toDomainModel(): DoctorPrescription {
        val meds = mutableListOf<MedicationItem>()
        try {
            val jsonArr = JSONArray(medicationsJson)
            for (i in 0 until jsonArr.length()) {
                val obj = jsonArr.getJSONObject(i)
                meds.add(
                    MedicationItem(
                        nameFa = obj.optString("nameFa"),
                        nameEn = obj.optString("nameEn"),
                        dosage = obj.optString("dosage"),
                        form = obj.optString("form"),
                        frequency = obj.optString("frequency"),
                        duration = obj.optString("duration"),
                        instructions = obj.optString("instructions")
                    )
                )
            }
        } catch (_: Exception) {}

        val remedies = mutableListOf<String>()
        try {
            val jsonArr = JSONArray(homeRemediesJson)
            for (i in 0 until jsonArr.length()) {
                remedies.add(jsonArr.getString(i))
            }
        } catch (_: Exception) {}

        val warnings = mutableListOf<String>()
        try {
            val jsonArr = JSONArray(warningSignsJson)
            for (i in 0 until jsonArr.length()) {
                warnings.add(jsonArr.getString(i))
            }
        } catch (_: Exception) {}

        val urgency = try {
            UrgencyLevel.valueOf(urgencyLevel)
        } catch (_: Exception) {
            UrgencyLevel.ROUTINE
        }

        return DoctorPrescription(
            id = id,
            timestamp = timestamp,
            patientName = patientName,
            patientAge = patientAge,
            patientGender = patientGender,
            symptomsSummary = symptomsSummary,
            probableDiagnosis = probableDiagnosis,
            clinicalAssessment = clinicalAssessment,
            urgencyLevel = urgency,
            urgencyReason = urgencyReason,
            medications = meds,
            homeRemedies = remedies,
            warningSigns = warnings,
            specialistReferral = specialistReferral,
            doctorAdvice = doctorAdvice,
            prescriptionCode = prescriptionCode
        )
    }

    companion object {
        fun fromDomainModel(item: DoctorPrescription): PrescriptionEntity {
            val medsArray = JSONArray()
            item.medications.forEach { med ->
                val obj = JSONObject()
                obj.put("nameFa", med.nameFa)
                obj.put("nameEn", med.nameEn)
                obj.put("dosage", med.dosage)
                obj.put("form", med.form)
                obj.put("frequency", med.frequency)
                obj.put("duration", med.duration)
                obj.put("instructions", med.instructions)
                medsArray.put(obj)
            }

            val remediesArray = JSONArray()
            item.homeRemedies.forEach { remediesArray.put(it) }

            val warningsArray = JSONArray()
            item.warningSigns.forEach { warningsArray.put(it) }

            return PrescriptionEntity(
                id = item.id,
                timestamp = item.timestamp,
                patientName = item.patientName,
                patientAge = item.patientAge,
                patientGender = item.patientGender,
                symptomsSummary = item.symptomsSummary,
                probableDiagnosis = item.probableDiagnosis,
                clinicalAssessment = item.clinicalAssessment,
                urgencyLevel = item.urgencyLevel.name,
                urgencyReason = item.urgencyReason,
                medicationsJson = medsArray.toString(),
                homeRemediesJson = remediesArray.toString(),
                warningSignsJson = warningsArray.toString(),
                specialistReferral = item.specialistReferral,
                doctorAdvice = item.doctorAdvice,
                prescriptionCode = item.prescriptionCode
            )
        }
    }
}
