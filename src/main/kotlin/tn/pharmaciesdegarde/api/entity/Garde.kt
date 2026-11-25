package tn.pharmaciesdegarde.api.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "garde")
class Garde(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pharmacie_id", nullable = false)
    var pharmacie: Pharmacie,
    @Column(nullable = false) var dateDebut: LocalDateTime,
    @Column(nullable = false) var dateFin: LocalDateTime,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false) var type: TypeGarde
)