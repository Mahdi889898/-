package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MedicationItem(
    val nameFa: String,
    val nameEn: String,
    val dosage: String,
    val form: String, // قرص، شربت، کپسول، پماد، قطره، آمپول
    val frequency: String, // مثلا هر ۸ ساعت یک عدد بعد از غذا
    val duration: String, // مثلا ۵ روز
    val instructions: String // توضیحات مصرف و نکات احتیاطی
)

enum class UrgencyLevel(val titleFa: String) {
    ROUTINE("معمولی و غیراورژانسی"),
    MODERATE("نیازمند مراقبت و پیگیری"),
    EMERGENCY("فوریت پزشکی / مراجعه سریع به اورژانس")
}

data class DoctorPrescription(
    val id: String = System.currentTimeMillis().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val patientName: String,
    val patientAge: String,
    val patientGender: String,
    val symptomsSummary: String,
    val probableDiagnosis: String,
    val clinicalAssessment: String,
    val urgencyLevel: UrgencyLevel = UrgencyLevel.ROUTINE,
    val urgencyReason: String = "",
    val medications: List<MedicationItem> = emptyList(),
    val homeRemedies: List<String> = emptyList(),
    val warningSigns: List<String> = emptyList(),
    val specialistReferral: String = "",
    val doctorAdvice: String = "",
    val doctorStamp: String = "دکتر هوشمند - نظام پزشکی هوش مصنوعی سلامت",
    val prescriptionCode: String = "RX-${(100000..999999).random()}",
    val disclaimer: String = "توجه: این نسخه و توصیه‌ها توسط هوش مصنوعی پزشکی بر اساس شرح حال اعلامی شما صادر شده و جایگزین ویزیت حضوری در شرایط بحرانی نمی‌باشد."
) {
    val formattedDate: String
        get() {
            val sdf = SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

    fun toShareableText(): String {
        val sb = StringBuilder()
        sb.append("📋 نسخه و دستورات درمانی پزشک هوشمند\n")
        sb.append("کد پیگیری نسخه: $prescriptionCode\n")
        sb.append("تاریخ: $formattedDate\n")
        sb.append("-----------------------------\n")
        sb.append("👤 بیمار: $patientName | سن: $patientAge | جنسیت: $patientGender\n")
        sb.append("🩺 علائم و شرح حال: $symptomsSummary\n")
        sb.append("🔬 تشخیص احتمالی: $probableDiagnosis\n")
        sb.append("⚠️ سطح فوریت: ${urgencyLevel.titleFa}\n\n")

        if (medications.isNotEmpty()) {
            sb.append("💊 اقلام دارویی تجویزی (Rx):\n")
            medications.forEachIndexed { index, med ->
                sb.append("${index + 1}. ${med.nameFa} (${med.nameEn}) - ${med.form} ${med.dosage}\n")
                sb.append("   دستور مصرف: ${med.frequency} (طول دوره: ${med.duration})\n")
                if (med.instructions.isNotBlank()) {
                    sb.append("   نکته: ${med.instructions}\n")
                }
            }
            sb.append("\n")
        }

        if (homeRemedies.isNotEmpty()) {
            sb.append("🍵 مراقبت‌های خانگی و تغذیه:\n")
            homeRemedies.forEach { sb.append("• $it\n") }
            sb.append("\n")
        }

        if (warningSigns.isNotEmpty()) {
            sb.append("🚨 علائم هشدار (مراجعه فوری در صورت مشاهده):\n")
            warningSigns.forEach { sb.append("! $it\n") }
            sb.append("\n")
        }

        if (specialistReferral.isNotBlank()) {
            sb.append("🏥 تخصص پیشنهادی جهت ویزیت: $specialistReferral\n\n")
        }

        if (doctorAdvice.isNotBlank()) {
            sb.append("📝 توصیه نهایی پزشک: $doctorAdvice\n\n")
        }

        sb.append("⚠️ $disclaimer\n")
        return sb.toString()
    }
}
