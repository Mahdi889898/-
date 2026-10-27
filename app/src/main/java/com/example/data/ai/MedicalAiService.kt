package com.example.data.ai

import android.util.Log
import com.example.data.model.ChatMessage
import com.example.data.model.DoctorPrescription
import com.example.data.model.MedicationItem
import com.example.data.model.PatientInfo
import com.example.data.model.UrgencyLevel
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "MedicalAiService"

class MedicalAiService {

    // 1. Structured JSON Schema for Medical Prescription
    private val medicationSchema by lazy {
        Schema.obj(
            properties = mapOf(
                "nameFa" to Schema.string("Persian name of the drug, e.g. قرص استامینوفن"),
                "nameEn" to Schema.string("Generic or scientific English name, e.g. Acetaminophen"),
                "dosage" to Schema.string("Dosage, e.g. ۵۰۰ میلی‌گرم"),
                "form" to Schema.string("Form: قرص، کپسول، شربت، قطره، پماد یا اسپری"),
                "frequency" to Schema.string("Directions, e.g. هر ۸ ساعت بعد از غذا"),
                "duration" to Schema.string("Course length, e.g. ۳ الی ۵ روز"),
                "instructions" to Schema.string("Precautions and dietary notes")
            )
        )
    }

    private val prescriptionSchema by lazy {
        Schema.obj(
            properties = mapOf(
                "probableDiagnosis" to Schema.string("Probable medical diagnosis in Persian"),
                "clinicalAssessment" to Schema.string("Clinical explanation and reason for symptoms"),
                "urgencyLevel" to Schema.enumeration(
                    listOf("ROUTINE", "MODERATE", "EMERGENCY"),
                    "Triage urgency level"
                ),
                "urgencyReason" to Schema.string("Explanation of the urgency level in Persian"),
                "medications" to Schema.array(
                    medicationSchema,
                    "List of recommended therapeutic medications and OTC remedies suitable for patient"
                ),
                "homeRemedies" to Schema.array(
                    Schema.string(),
                    "Practical non-pharmacological care, hydration, rest, herbal remedies and dietary tips"
                ),
                "warningSigns" to Schema.array(
                    Schema.string(),
                    "Red-flag emergency warning signs requiring immediate hospital or in-person physician visit"
                ),
                "specialistReferral" to Schema.string("Medical specialty to visit if symptoms persist"),
                "doctorAdvice" to Schema.string("Empathetic closing clinical advice from the doctor")
            )
        )
    }

    // Lazy initialization of GenerativeModel with Structured Output
    private val diagnosisModel by lazy {
        val config = generationConfig {
            responseMimeType = "application/json"
            responseSchema = prescriptionSchema
        }
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = config,
            systemInstruction = com.google.firebase.ai.type.content {
                text(
                    """
                    شما یک پزشک متخصص بالینی، دلسوز، دقیق و حاذق هستید (دکتر هوشمند).
                    وظیفه شما این است که بر اساس شرح حال، علائم بیماری، مشخصات بیمار (سن، جنسیت، وزن، حساسیت‌های دارویی، بیماری‌های زمینه‌ای یا بارداری) یک ارزیابی کامل پزشکی و یک نسخه درمانی دقیق و علمی تجویز کنید.
                    
                    اصول مهم:
                    ۱. نام داروها را با دوز، نحوه و فواصل مصرف دقیق (مثلاً هر ۸ ساعت، هر ۱۲ ساعت، بعد از غذا و ...) به زبان فارسی روان و نام علمی انگلیسی بنویسید.
                    ۲. به حساسیت‌های دارویی و بیماری‌های زمینه‌ای بیمار توجه اکید داشته باشید تا داروی نامناسب تجویز نشود.
                    ۳. اگر علائم بیمار نشان‌دهنده یک وضعیت حاد و خطرناک است (مثل سکته قلبی یا مغزی، تنگی نفس شدید، آپاندیسیت، خونریزی شدید، مسمومیت)، سطح فوریت را EMERGENCY قرار دهید و علائم هشدار را با تأکید فراوان اعلام کنید.
                    ۴. مراقبت‌های خانگی، رژیم غذایی و مایعات لازم را کامل توضیح دهید.
                    ۵. پاسخ حتماً طبق ساختار JSON خواسته شده برگردانده شود.
                    """.trimIndent()
                )
            }
        )
    }

    // Interactive Follow-up Chat Model
    private val chatModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            systemInstruction = com.google.firebase.ai.type.content {
                text(
                    """
                    شما همان پزشک معالج بیمار هستید که برای او نسخه صادر کرده‌اید.
                    به سؤالات تکمیلی بیمار درباره داروها، تداخلات دارویی، رژیم غذایی، زمان اثر داروها و نحوه مراقبت با بیانی گرم، علمی، شفاف و آرامش‌بخش به زبان فارسی پاسخ دهید.
                    """.trimIndent()
                )
            }
        )
    }

    suspend fun diagnoseAndPrescribe(
        patientInfo: PatientInfo,
        chiefComplaint: String,
        symptomDuration: String,
        severityLevel: Int,
        additionalSymptoms: List<String>
    ): DoctorPrescription {
        val prompt = buildString {
            append("اطلاعات بیمار:\n")
            append("- نام: ${patientInfo.name.ifBlank { "بیمار" }}\n")
            append("- سن: ${patientInfo.age.ifBlank { "بزرگسال" }} سال\n")
            append("- جنسیت: ${patientInfo.gender}\n")
            if (patientInfo.weightKg.isNotBlank()) append("- وزن: ${patientInfo.weightKg} کیلوگرم\n")
            if (patientInfo.knownAllergies.isNotBlank()) append("- حساسیت دارویی/غذایی: ${patientInfo.knownAllergies}\n")
            if (patientInfo.chronicConditions.isNotBlank()) append("- بیماری‌های زمینه‌ای قبلی: ${patientInfo.chronicConditions}\n")
            if (patientInfo.isPregnantOrLactating) append("- وضعیت: باردار یا شیرده (ملاحظه دارویی اکید)\n")
            append("\nشرح حال و علائم فعلی:\n")
            append("- شکایت اصلی و شرح مشکل: $chiefComplaint\n")
            append("- مدت زمان بروز علائم: $symptomDuration\n")
            append("- شدت علائم (از ۱ تا ۱۰): $severityLevel\n")
            if (additionalSymptoms.isNotEmpty()) {
                append("- سایر علائم همراه: ${additionalSymptoms.joinToString("، ")}\n")
            }
            append("\nلطفاً ارزیابی بالینی کامل انجام داده و نسخه دقیق درمانی را تجویز نمایید.")
        }

        return try {
            val response = withTimeout(60_000L) {
                diagnosisModel.generateContent(prompt)
            }
            val jsonString = response.text ?: throw IllegalStateException("پاسخی از مدل دریافت نشد.")
            Log.d(TAG, "Raw response received successfully")
            parsePrescriptionJson(jsonString, patientInfo, chiefComplaint)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error generating prescription", e)
            throw e
        }
    }

    private fun parsePrescriptionJson(
        jsonString: String,
        patientInfo: PatientInfo,
        symptoms: String
    ): DoctorPrescription {
        val root = JSONObject(jsonString)

        val probableDiagnosis = root.optString("probableDiagnosis", "بررسی بالینی")
        val clinicalAssessment = root.optString("clinicalAssessment", "")
        val urgencyStr = root.optString("urgencyLevel", "ROUTINE")
        val urgencyReason = root.optString("urgencyReason", "")
        val specialistReferral = root.optString("specialistReferral", "پزشک عمومی یا داخلی")
        val doctorAdvice = root.optString("doctorAdvice", "")

        val urgencyLevel = try {
            UrgencyLevel.valueOf(urgencyStr)
        } catch (_: Exception) {
            UrgencyLevel.ROUTINE
        }

        val medications = mutableListOf<MedicationItem>()
        val medsArray = root.optJSONArray("medications")
        if (medsArray != null) {
            for (i in 0 until medsArray.length()) {
                val medObj = medsArray.getJSONObject(i)
                medications.add(
                    MedicationItem(
                        nameFa = medObj.optString("nameFa", "دارو"),
                        nameEn = medObj.optString("nameEn", ""),
                        dosage = medObj.optString("dosage", ""),
                        form = medObj.optString("form", "قرص"),
                        frequency = medObj.optString("frequency", "طبق دستور"),
                        duration = medObj.optString("duration", ""),
                        instructions = medObj.optString("instructions", "")
                    )
                )
            }
        }

        val homeRemedies = mutableListOf<String>()
        val remediesArray = root.optJSONArray("homeRemedies")
        if (remediesArray != null) {
            for (i in 0 until remediesArray.length()) {
                homeRemedies.add(remediesArray.getString(i))
            }
        }

        val warningSigns = mutableListOf<String>()
        val warningsArray = root.optJSONArray("warningSigns")
        if (warningsArray != null) {
            for (i in 0 until warningsArray.length()) {
                warningSigns.add(warningsArray.getString(i))
            }
        }

        return DoctorPrescription(
            patientName = patientInfo.name.ifBlank { "بیمار محترم" },
            patientAge = patientInfo.age.ifBlank { "-" },
            patientGender = patientInfo.gender,
            symptomsSummary = symptoms,
            probableDiagnosis = probableDiagnosis,
            clinicalAssessment = clinicalAssessment,
            urgencyLevel = urgencyLevel,
            urgencyReason = urgencyReason,
            medications = medications,
            homeRemedies = homeRemedies,
            warningSigns = warningSigns,
            specialistReferral = specialistReferral,
            doctorAdvice = doctorAdvice
        )
    }

    fun streamDoctorChat(
        prescriptionContext: DoctorPrescription?,
        conversationHistory: List<ChatMessage>,
        userMessage: String
    ): Flow<String> = flow {
        val sb = StringBuilder()
        sb.append("بافت پرونده بیمار:\n")
        if (prescriptionContext != null) {
            sb.append("- بیمار: ${prescriptionContext.patientName} (${prescriptionContext.patientAge} ساله)\n")
            sb.append("- تشخیص اولیه: ${prescriptionContext.probableDiagnosis}\n")
            sb.append("- داروهای تجویزی: ${prescriptionContext.medications.joinToString { "${it.nameFa} (${it.dosage})" }}\n")
        }
        sb.append("\nتاریخچه گفتگو:\n")
        conversationHistory.takeLast(6).forEach { msg ->
            val senderLabel = if (msg.sender == com.example.data.model.MessageSender.USER) "بیمار" else "دکتر"
            sb.append("$senderLabel: ${msg.text}\n")
        }
        sb.append("سؤال جدید بیمار: $userMessage\n")
        sb.append("پاسخ پزشک: ")

        try {
            withTimeout(60_000L) {
                chatModel.generateContentStream(sb.toString()).collect { chunk ->
                    val text = chunk.text.orEmpty()
                    if (text.isNotEmpty()) {
                        emit(text)
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Chat streaming error", e)
            throw e
        }
    }
}
