package tn.pharmaciesdegarde.api.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter(private val jwtService: JwtService) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val header = request.getHeader("Authorization")
        if (header != null && header.startsWith("Bearer ")) {
            val claims = jwtService.parse(header.removePrefix("Bearer "))
            if (claims != null) {
                val role = claims["role"] as String
                SecurityContextHolder.getContext().authentication =
                    UsernamePasswordAuthenticationToken(
                        claims.subject, null, listOf(SimpleGrantedAuthority("ROLE_$role"))
                    )
            }
        }
        filterChain.doFilter(request, response)
    }
}