package tn.pharmaciesdegarde.api.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import tn.pharmaciesdegarde.api.entity.Pharmacie
import tn.pharmaciesdegarde.api.repository.PharmacieRepository
import org.springframework.http.MediaType
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.RequestBody
import tn.pharmaciesdegarde.api.dto.PharmacieRequest
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import tn.pharmaciesdegarde.api.dto.PharmacieUpdateRequest


@RestController
@RequestMapping("/api")
class PharmacieController(
    private val pharmacieRepository: PharmacieRepository
) {
    @GetMapping("/pharmacies")
    fun getPharmacies(@RequestParam(required = false) ville: String?): List<Pharmacie> {
        return if (ville.isNullOrBlank()) {
            pharmacieRepository.findAll()
        } else {
            pharmacieRepository.findByVille(ville)
        }
    }

    @PostMapping("/admin/pharmacies/import", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun importCsv(@RequestParam("file") file: MultipartFile): ResponseEntity<Map<String, Int>> {
        val lines = file.inputStream.bufferedReader().readLines()
        require(lines.size > 1) { "Le fichier CSV est vide ou sans en-tête" }

        val header = lines.first().split(",")
        val idx = header.withIndex().associate { (i, h) -> h.trim() to i }

        var inserted = 0
        lines.drop(1).filter { it.isNotBlank() }.forEach { line ->
            val cols = line.split(",")
            try {
                pharmacieRepository.save(
                    Pharmacie(
                        nom = cols[idx["nom"]!!].trim(),
                        adresse = cols[idx["adresse"]!!].trim(),
                        ville = cols[idx["ville"]!!].trim(),
                        gouvernorat = cols[idx["gouvernorat"]!!].trim(),
                        latitude = cols[idx["latitude"]!!].trim().toDouble(),
                        longitude = cols[idx["longitude"]!!].trim().toDouble(),
                        telephone = cols[idx["telephone"]!!].trim()
                    )
                )
                inserted++
            } catch (e: Exception) {
                println("Erreur sur la ligne : $line - ${e.message}")
            }
        }
        return ResponseEntity.ok(mapOf("inserted" to inserted))
    }

    @PostMapping("/admin/pharmacies")
    fun create(@RequestBody r: PharmacieRequest): ResponseEntity<Any> {
        val nom = r.nom.trim()
        val adresse = r.adresse.trim()
        val ville = r.ville.trim()
        val gouvernorat = r.gouvernorat.trim()
        val telephone = r.telephone.trim()

        if (nom.isBlank() || adresse.isBlank() || ville.isBlank() ||
            gouvernorat.isBlank() || telephone.isBlank()
        ) return ResponseEntity.badRequest()
            .body(mapOf("message" to "Tous les champs sont obligatoires"))

        if (r.latitude !in -90.0..90.0 || r.longitude !in -180.0..180.0)
            return ResponseEntity.badRequest()
                .body(mapOf("message" to "Coordonnées GPS invalides"))

        if (!telephone.matches(Regex("^(\\+216)?[0-9 ]{8,12}$")))
            return ResponseEntity.badRequest()
                .body(mapOf("message" to "Numéro de téléphone invalide"))

        if (pharmacieRepository.existsByNomIgnoreCaseAndAdresseIgnoreCase(nom, adresse))
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(mapOf("message" to "Cette pharmacie existe déjà"))

        val saved = pharmacieRepository.save(
            Pharmacie(
                nom = nom,
                adresse = adresse,
                ville = ville,
                gouvernorat = gouvernorat,
                latitude = r.latitude,
                longitude = r.longitude,
                telephone = telephone,
                photo = r.photo?.trim()?.ifBlank { null }
            )
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(saved)
    }


    @PutMapping("/admin/pharmacies/{id}")
    fun update(@PathVariable id: Long, @RequestBody r: PharmacieUpdateRequest): ResponseEntity<Any> {
        val pharmacie = pharmacieRepository.findById(id).orElse(null)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("message" to "Pharmacie introuvable"))

        val nom = r.nom.trim()
        val adresse = r.adresse.trim()
        val telephone = r.telephone.trim()

        if (nom.isBlank() || adresse.isBlank() || telephone.isBlank())
            return ResponseEntity.badRequest()
                .body(mapOf("message" to "Tous les champs sont obligatoires"))

        if (!telephone.matches(Regex("^(\\+216)?[0-9 ]{8,12}$")))
            return ResponseEntity.badRequest()
                .body(mapOf("message" to "Numéro de téléphone invalide"))

        if (pharmacieRepository.existsByNomIgnoreCaseAndAdresseIgnoreCaseAndIdNot(nom, adresse, id))
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(mapOf("message" to "Cette pharmacie existe déjà"))

        pharmacie.nom = nom
        pharmacie.adresse = adresse
        pharmacie.telephone = telephone
        return ResponseEntity.ok(pharmacieRepository.save(pharmacie))
    }

    @DeleteMapping("/admin/pharmacies/{id}")
    fun delete(@PathVariable id: Long): ResponseEntity<Any> {
        if (!pharmacieRepository.existsById(id))
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("message" to "Pharmacie introuvable"))
        pharmacieRepository.deleteById(id)
        return ResponseEntity.noContent().build()
    }
}