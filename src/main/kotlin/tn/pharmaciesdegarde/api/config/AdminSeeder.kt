package tn.pharmaciesdegarde.api.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import tn.pharmaciesdegarde.api.repository.UtilisateurRepository
import tn.pharmaciesdegarde.api.entity.Role
import tn.pharmaciesdegarde.api.entity.Utilisateur
@Component
class AdminSeeder(
    private val utilisateurRepository: UtilisateurRepository,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${admin.email}") private val email: String,
    @Value("\${admin.password}") private val password: String
) : CommandLineRunner {

    override fun run(vararg args: String) {
        if (utilisateurRepository.findByEmail(email) == null) {
            utilisateurRepository.save(
                Utilisateur(
                    email = email,
                    passwordHash = passwordEncoder.encode(password)!!,
                    nom = "Administrateur",
                    role = Role.ADMIN
                )
            )
            println("Admin créé : $email")
        }
    }
}