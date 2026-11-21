package tn.pharmaciesdegarde.api.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import tn.pharmaciesdegarde.api.config.JwtService
import tn.pharmaciesdegarde.api.entity.Utilisateur
import tn.pharmaciesdegarde.api.repository.UtilisateurRepository
import org.springframework.dao.DataIntegrityViolationException
import tn.pharmaciesdegarde.api.entity.Role

data class LoginRequest(val email: String, val password: String)

data class LoginResponse(val token: String, val email: String, val nom: String, val role: String)
data class RegisterRequest(val nom: String, val email: String, val password: String)
private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val utilisateurRepository: UtilisateurRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService
) {
    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<Any> {
        val user = utilisateurRepository.findByEmail(request.email.trim().lowercase())
        if (user == null || !passwordEncoder.matches(request.password, user.passwordHash)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("message" to "Email ou mot de passe incorrect"))
        }
        val token = jwtService.generateToken(user.email, user.role.name)
        return ResponseEntity.ok(LoginResponse(token, user.email, user.nom,user.role.name))
    }

    @PostMapping("/register")
    fun register(@RequestBody r: RegisterRequest): ResponseEntity<Any> {
        val nom = r.nom.trim()
        val email = r.email.trim().lowercase()

        if (nom.length !in 2..100)
            return error(HttpStatus.BAD_REQUEST, "Le nom doit contenir entre 2 et 100 caractères")
        if (email.length > 254 || !EMAIL_REGEX.matches(email))
            return error(HttpStatus.BAD_REQUEST, "Adresse e-mail invalide")
        if (r.password.length < 8)
            return error(HttpStatus.BAD_REQUEST, "Le mot de passe doit contenir 8 caractères minimum")
        if (r.password.toByteArray().size > 72)
            return error(HttpStatus.BAD_REQUEST, "Le mot de passe est trop long (72 caractères maximum)")

        if (utilisateurRepository.findByEmail(email) != null)
            return error(HttpStatus.CONFLICT, "Cet email est déjà utilisé")

        val user = try {
            utilisateurRepository.save(
                Utilisateur(
                    email = email,
                    passwordHash = passwordEncoder.encode(r.password)!!,
                    nom = nom,
                    role = Role.UTILISATEUR      // jamais fourni par le client
                )
            )
        } catch (e: DataIntegrityViolationException) {
            // deux inscriptions simultanées avec le même e-mail
            return error(HttpStatus.CONFLICT, "Cet email est déjà utilisé")
        }

        val token = jwtService.generateToken(user.email, user.role.name)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(LoginResponse(token, user.email, user.nom, user.role.name))
    }

    private fun error(status: HttpStatus, message: String): ResponseEntity<Any> =
        ResponseEntity.status(status).body(mapOf("message" to message))
}