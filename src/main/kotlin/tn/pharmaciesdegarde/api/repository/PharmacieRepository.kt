package tn.pharmaciesdegarde.api.repository

import org.springframework.data.jpa.repository.JpaRepository
import tn.pharmaciesdegarde.api.entity.Pharmacie

interface PharmacieRepository : JpaRepository<Pharmacie, Long> {
    fun findByVille(ville: String): List<Pharmacie>
}