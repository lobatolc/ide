package br.com.ide.domain.usecase

import br.com.ide.domain.model.District
import br.com.ide.domain.repository.DistrictRepository
import javax.inject.Inject

class GetDistrictByIdUseCase @Inject constructor(
    private val districtRepository:
    DistrictRepository
) {

    suspend operator fun invoke(
        districtId: String
    ): Result<District> {

        return districtRepository
            .getDistrictById(
                districtId
            )
    }
}