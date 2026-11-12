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
}