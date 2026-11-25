// dto/PharmacieUpdateRequest.kt
package tn.pharmaciesdegarde.api.dto

// PUT /admin/pharmacies/{id} : tout est optionnel sauf nom, adresse, téléphone
data class PharmacieUpdateRequest(
    val nom: String,
    val adresse: String,
    val telephone: String,
    val ville: String? = null,
    val gouvernorat: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val photo: String? = null
)