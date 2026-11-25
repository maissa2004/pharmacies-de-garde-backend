package tn.pharmaciesdegarde.api.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import tn.pharmaciesdegarde.api.dto.GeocodeResponse
import tn.pharmaciesdegarde.api.dto.NominatimResult
import tn.pharmaciesdegarde.api.dto.NominatimReverseResult
import tn.pharmaciesdegarde.api.dto.ReverseGeocodeResponse
import java.text.Normalizer

@Service
class GeocodingService(
    private val restTemplate: RestTemplate,
    @Value("\${nominatim.user-agent:PharmaciesDeGarde/1.0 (contact@exemple.tn)}")
    private val userAgent: String
) {

    fun geocode(adresse: String, ville: String, gouvernorat: String): GeocodeResponse? {
        // 1) Tentative précise : adresse complète
        search("$adresse, $ville, $gouvernorat")?.let {
            return GeocodeResponse(it.first, it.second, precise = true)
        }
        // 2) Repli : centre de la ville (Nominatim limite à 1 requête/seconde)
        Thread.sleep(1100)
        return search("$ville, $gouvernorat")?.let {
            GeocodeResponse(it.first, it.second, precise = false)
        }
    }

    private fun search(query: String): Pair<Double, Double>? = try {
        val uri = UriComponentsBuilder
            .fromUriString("https://nominatim.openstreetmap.org/search")
            .queryParam("q", query)
            .queryParam("format", "jsonv2")
            .queryParam("limit", 1)
            .queryParam("countrycodes", "tn")
            .build().encode().toUri()

        val headers = HttpHeaders().apply { set("User-Agent", userAgent) }
        val results = restTemplate.exchange(
            uri, HttpMethod.GET, HttpEntity<Void>(headers),
            Array<NominatimResult>::class.java
        ).body

        results?.firstOrNull()?.let { it.lat.toDouble() to it.lon.toDouble() }
    } catch (e: Exception) {
        println("Erreur géocodage pour « $query » : ${e.message}")
        null
    }


    // Mêmes noms que la liste déroulante d'Android
    private val gouvernorats = listOf(
        "Ariana", "Béja", "Ben Arous", "Bizerte", "Gabès", "Gafsa", "Jendouba", "Kairouan",
        "Kasserine", "Kébili", "Le Kef", "Mahdia", "Manouba", "Médenine", "Monastir", "Nabeul",
        "Sfax", "Sidi Bouzid", "Siliana", "Sousse", "Tataouine", "Tozeur", "Tunis", "Zaghouan"
    )

    private fun normalize(s: String) =
        Normalizer.normalize(s, Normalizer.Form.NFD).replace(Regex("\\p{M}+"), "").lowercase()

    // Nominatim renvoie par ex. « Gouvernorat de Ben Arous » : on retrouve le nom de notre liste
    private fun matchGouvernorat(state: String?): String? {
        if (state.isNullOrBlank()) return null
        val n = normalize(state)
        return gouvernorats.firstOrNull { n.contains(normalize(it).removePrefix("le ")) }
    }

    fun reverse(lat: Double, lng: Double): ReverseGeocodeResponse? {
        try {
            val uri = UriComponentsBuilder
                .fromUriString("https://nominatim.openstreetmap.org/reverse")
                .queryParam("lat", lat)
                .queryParam("lon", lng)
                .queryParam("format", "jsonv2")
                .queryParam("addressdetails", 1)
                .queryParam("zoom", 18)
                .queryParam("accept-language", "fr")
                .build().encode().toUri()

            val headers = HttpHeaders().apply { set("User-Agent", userAgent) }
            val a = restTemplate.exchange(
                uri, HttpMethod.GET, HttpEntity<Void>(headers),
                NominatimReverseResult::class.java
            ).body?.address ?: return null

            val rue = listOfNotNull(a.houseNumber, a.road ?: a.pedestrian).joinToString(" ")
            val adresse = rue.ifBlank { a.neighbourhood ?: a.suburb ?: "" }
            val ville = a.city ?: a.town ?: a.village ?: a.municipality ?: a.county ?: ""
            val gouvernorat = matchGouvernorat(a.state) ?: ""

            return ReverseGeocodeResponse(adresse, ville, gouvernorat)
        } catch (e: Exception) {
            println("Erreur géocodage inverse ($lat, $lng) : ${e.message}")
            return null
        }
    }
}