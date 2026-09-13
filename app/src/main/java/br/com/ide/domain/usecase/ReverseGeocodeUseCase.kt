package br.com.ide.domain.usecase

import br.com.ide.domain.model.GeocodedAddress
import br.com.ide.domain.repository.GeocodingRepository
import javax.inject.Inject

class ReverseGeocodeUseCase @Inject constructor(
    private val geocodingRepository:
    GeocodingRepository
) {

    suspend operator fun invoke(
        latitude: Double,
        longitude: Double
    ): Result<GeocodedAddress> {

        return geocodingRepository
            .reverseGeocode(
                latitude =
                    latitude,
                longitude =
                    longitude
            )
    }
}