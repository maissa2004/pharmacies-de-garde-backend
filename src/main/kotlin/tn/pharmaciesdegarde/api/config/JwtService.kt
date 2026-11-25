package tn.pharmaciesdegarde.api.config

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date

@Service
class JwtService(
    @Value("\${jwt.secret}") secret: String,
    @Value("\${jwt.expiration-ms}") private val expirationMs: Long
) {
    private val key = Keys.hmacShaKeyFor(secret.toByteArray())

    fun generateToken(email: String, role: String): String {
        val now = System.currentTimeMillis()
        return Jwts.builder()
            .subject(email)
            .claim("role", role)
            .issuedAt(Date(now))
            .expiration(Date(now + expirationMs))
            .signWith(key)
            .compact()
    }

    fun parse(token: String): Claims? = try {
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload
    } catch (e: Exception) {
        null // token invalide ou expiré
    }
}