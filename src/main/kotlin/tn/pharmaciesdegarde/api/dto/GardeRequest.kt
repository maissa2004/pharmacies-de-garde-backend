package tn.pharmaciesdegarde.api.dto

import tn.pharmaciesdegarde.api.entity.TypeGarde
import java.time.LocalDateTime

data class GardeRequest(
    val pharmacieId: Long,
    val gouvernorat: String,          // région sélectionnée
    val dateDebut: LocalDateTime,
    val dateFin: LocalDateTime,
    val type: TypeGarde
)