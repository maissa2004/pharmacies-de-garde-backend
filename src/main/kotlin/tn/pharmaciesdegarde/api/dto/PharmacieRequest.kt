package tn.pharmaciesdegarde.api.dto
// POST /admin/pharmacies : ville, gouvernorat, latitude, longitude obligatoires
data class PharmacieRequest(
    val nom: String,
    val adresse: String,
    val telephone: String,
    val ville: String,
    val gouvernorat: String,
    val latitude: Double,
    val longitude: Double,
    val photo: String? = null
)

