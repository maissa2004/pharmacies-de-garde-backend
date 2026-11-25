package tn.pharmaciesdegarde.api.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import tn.pharmaciesdegarde.api.dto.GeocodeRequest
import tn.pharmaciesdegarde.api.service.GeocodingService

@RestController
@RequestMapping("/api/admin/pharmacies")
class GeocodingController(private val geocodingService: GeocodingService) {

    @PostMapping("/geocode")
    fun geocode(@RequestBody r: GeocodeRequest): ResponseEntity<Any> {
        val adresse = r.adresse.trim()
        val ville = r.ville.trim()
        val gouvernorat = r.gouvernorat.trim()

        if (adresse.isBlank() || ville.isBlank() || gouvernorat.isBlank())
            return ResponseEntity.badRequest()
                .body(mapOf("message" to "Adresse, ville et gouvernorat sont obligatoires"))

        val result = geocodingService.geocode(adresse, ville, gouvernorat)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("message" to "Adresse introuvable, placez le marqueur manuellement"))

        return ResponseEntity.ok(result)
    }


    @GetMapping("/reverse-geocode")
    fun reverseGeocode(@RequestParam lat: Double, @RequestParam lng: Double): ResponseEntity<Any> {
        if (lat !in 30.0..38.0 || lng !in 7.0..12.0)
            return ResponseEntity.badRequest()
                .body(mapOf("message" to "Position hors de Tunisie"))

        val result = geocodingService.reverse(lat, lng)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(mapOf("message" to "Adresse introuvable pour cette position"))

        return ResponseEntity.ok(result)
    }
}