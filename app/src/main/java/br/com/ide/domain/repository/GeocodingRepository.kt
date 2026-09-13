package br.com.ide.domain.repository

import br.com.ide.domain.model.GeocodedAddress

interface GeocodingRepository {

    suspend fun reverseGeocode(
        latitude: Double,
        longitude: Double
    ): Result<GeocodedAddress>
}