package tn.pharmaciesdegarde.api.entity

import jakarta.persistence.*

@Entity
@Table(name = "utilisateur")
class Utilisateur(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false, unique = true) var email: String,
    @Column(nullable = false) var passwordHash: String,
    @Column(nullable = false) var nom: String = "",
    @Enumerated(EnumType.STRING)
    @Column(nullable = false) var role: Role = Role.UTILISATEUR
)