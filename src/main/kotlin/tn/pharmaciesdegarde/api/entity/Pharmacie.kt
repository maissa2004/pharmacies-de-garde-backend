package tn.pharmaciesdegarde.api.entity

import jakarta.persistence.*

@Entity
@Table(name = "pharmacie")
class Pharmacie(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false) var nom: String,
    @Column(nullable = false) var adresse: String,
    @Column(nullable = false) var ville: String,
    @Column(nullable = false) var gouvernorat: String,
    @Column(nullable = false) var latitude: Double,
    @Column(nullable = false) var longitude: Double,
    @Column(nullable = false) var telephone: String,
    var photo: String? = null
)