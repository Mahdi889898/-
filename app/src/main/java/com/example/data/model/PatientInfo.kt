package com.example.data.model

data class PatientInfo(
    val name: String = "",
    val age: String = "",
    val gender: String = "آقا", // "آقا", "خانم"
    val weightKg: String = "",
    val knownAllergies: String = "",
    val chronicConditions: String = "",
    val isPregnantOrLactating: Boolean = false
)
