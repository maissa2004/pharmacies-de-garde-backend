package tn.pharmaciesdegarde.api.dto

data class PharmacieRequest(
    val nom: String,
    val adresse: String,
    val telephone: String,
    val ville: String = "Non renseignée",
    val gouvernorat: String = "Non renseigné",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val photo: String? = null
)