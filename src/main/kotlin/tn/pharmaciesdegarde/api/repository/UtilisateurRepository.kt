package tn.pharmaciesdegarde.api.repository

import org.springframework.data.jpa.repository.JpaRepository
import tn.pharmaciesdegarde.api.entity.Utilisateur

interface UtilisateurRepository : JpaRepository<Utilisateur, Long> {
    fun findByEmail(email: String): Utilisateur?
}