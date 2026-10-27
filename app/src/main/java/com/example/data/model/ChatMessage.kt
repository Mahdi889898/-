package com.example.data.model

data class ChatMessage(
    val id: String = System.currentTimeMillis().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageSender {
    USER,
    DOCTOR
}

data class DrugGuideItem(
    val id: String,
    val nameFa: String,
    val nameEn: String,
    val category: String, // تسکین درد و تب، سرماخوردگی و حساسیت، گوارش، عفونت و آنتی‌بیوتیک، آرام‌بخش
    val indication: String, // موارد مصرف
    val dosageAdult: String, // دوز معمول بزرگسالان
    val sideEffects: String, // عوارض شایع
    val contraindications: String, // موارد منع مصرف و تداخل
    val advice: String // نکات مهم
)
