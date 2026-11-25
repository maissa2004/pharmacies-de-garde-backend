package tn.pharmaciesdegarde.api.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

data class GeocodeRequest(
    val adresse: String,
    val ville: String,
    val gouvernorat: String
)

data class GeocodeResponse(
    val latitude: Double,
    val longitude: Double,
    val precise: Boolean   // false = position approximative (centre de la ville)
)

// Nominatim renvoie beaucoup de champs, on ne garde que lat/lon
@JsonIgnoreProperties(ignoreUnknown = true)
data class NominatimResult(val lat: String, val lon: String)

data class ReverseGeocodeResponse(
    val adresse: String,
    val ville: String,
    val gouvernorat: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class NominatimReverseResult(val address: NominatimAddress? = null)

@JsonIgnoreProperties(ignoreUnknown = true)
data class NominatimAddress(
    @JsonProperty("house_number") val houseNumber: String? = null,
    val road: String? = null,
    val pedestrian: String? = null,
    val neighbourhood: String? = null,
    val suburb: String? = null,
    val city: String? = null,
    val town: String? = null,
    val village: String? = null,
    val municipality: String? = null,
    val county: String? = null,
    val state: String? = null
)