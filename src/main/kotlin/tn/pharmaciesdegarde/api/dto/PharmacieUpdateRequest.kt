// dto/PharmacieUpdateRequest.kt
package tn.pharmaciesdegarde.api.dto

data class PharmacieUpdateRequest(
    val nom: String,
    val adresse: String,
    val telephone: String
)