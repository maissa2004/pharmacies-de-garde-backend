package tn.pharmaciesdegarde.api.repository

import org.springframework.data.jpa.repository.JpaRepository
import tn.pharmaciesdegarde.api.entity.Garde
import java.time.LocalDateTime

interface GardeRepository : JpaRepository<Garde, Long> {
    fun findByPharmacieGouvernoratIgnoreCaseOrderByDateDebut(gouvernorat: String): List<Garde>

    // Chevauchement : une garde existante commence avant la fin ET finit après le début
    fun existsByPharmacieIdAndDateDebutLessThanAndDateFinGreaterThan(
        pharmacieId: Long, fin: LocalDateTime, debut: LocalDateTime
    ): Boolean
}