package tn.pharmaciesdegarde.api.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import tn.pharmaciesdegarde.api.dto.GardeRequest
import tn.pharmaciesdegarde.api.entity.Garde
import tn.pharmaciesdegarde.api.repository.GardeRepository
import tn.pharmaciesdegarde.api.repository.PharmacieRepository
import tn.pharmaciesdegarde.api.service.GardeRules

@RestController
@RequestMapping("/api")
class GardeController(
    private val gardeRepository: GardeRepository,
    private val pharmacieRepository: PharmacieRepository
) {
    // Planning d'une région
    @GetMapping("/gardes")
    fun getGardes(@RequestParam(required = false) gouvernorat: String?): List<Garde> =
        if (gouvernorat.isNullOrBlank()) gardeRepository.findAllByOrderByDateDebut()
        else gardeRepository.findByPharmacieGouvernoratIgnoreCaseOrderByDateDebut(gouvernorat.trim())

    @PostMapping("/admin/gardes")
    fun create(@RequestBody r: GardeRequest): ResponseEntity<Any> {
        val pharmacie = pharmacieRepository.findById(r.pharmacieId).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("message" to "Pharmacie introuvable"))

        if (!pharmacie.gouvernorat.equals(r.gouvernorat.trim(), ignoreCase = true))
            return ResponseEntity.badRequest()
                .body(mapOf("message" to "Cette pharmacie n'appartient pas à la région sélectionnée"))

        if (!r.dateFin.isAfter(r.dateDebut))
            return ResponseEntity.badRequest()
                .body(mapOf("message" to "La date de fin doit être postérieure à la date de début"))

        GardeRules.validate(r.type, r.dateDebut, r.dateFin)?.let { message ->
            return ResponseEntity.badRequest().body(mapOf("message" to message))
        }

        if (gardeRepository.existsByPharmacieIdAndDateDebutLessThanAndDateFinGreaterThan(
                pharmacie.id!!, r.dateFin, r.dateDebut))
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(mapOf("message" to "Cette pharmacie a déjà une garde sur cette période"))

        val saved = gardeRepository.save(
            Garde(
                pharmacie = pharmacie,
                dateDebut = r.dateDebut,
                dateFin = r.dateFin,
                type = r.type
            )
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(saved)
    }
}