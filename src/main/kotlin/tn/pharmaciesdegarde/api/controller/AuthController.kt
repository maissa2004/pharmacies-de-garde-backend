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

data class LoginRequest(val email: String, val password: String)

data class LoginResponse(val token: String, val email: String, val nom: String, val role: String)
data class RegisterRequest(val nom: String, val email: String, val password: String)

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
        val email = r.email.trim().lowercase()
        if (r.nom.isBlank() || r.password.length < 8)
            return ResponseEntity.badRequest().body(mapOf("message" to "Nom requis et mot de passe de 8 caractères minimum"))
        if (utilisateurRepository.findByEmail(email) != null)
            return ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("message" to "Cet email est déjà utilisé"))
        val user = utilisateurRepository.save(
            Utilisateur(email = email, passwordHash = passwordEncoder.encode(r.password)!!, nom = r.nom.trim())
        )
        val token = jwtService.generateToken(user.email, user.role.name)
        return ResponseEntity.status(HttpStatus.CREATED).body(LoginResponse(token, user.email, user.nom, user.role.name))
    }
}