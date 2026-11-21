package tn.pharmaciesdegarde.api.dto

data class PharmacieRequest(
    val nom: String,
    val adresse: String,
    val telephone: String,
    val ville: String,
    val gouvernorat: String,
    val latitude: Double,     // plus de valeur par défaut
    val longitude: Double,
    val photo: String? = null
)