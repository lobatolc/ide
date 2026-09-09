package br.com.ide.domain.usecase

import br.com.ide.domain.model.District
import br.com.ide.domain.repository.DistrictRepository
import javax.inject.Inject

class GetDistrictsUseCase @Inject constructor(
    private val districtRepository:
    DistrictRepository
) {

    suspend operator fun invoke():
            Result<List<District>> {

        return districtRepository
            .getDistricts()
    }
}