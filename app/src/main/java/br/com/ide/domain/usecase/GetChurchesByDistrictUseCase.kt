package br.com.ide.domain.usecase

import br.com.ide.domain.model.Church
import br.com.ide.domain.repository.ChurchRepository
import javax.inject.Inject

class GetChurchesByDistrictUseCase @Inject constructor(
    private val churchRepository:
    ChurchRepository
) {

    suspend operator fun invoke(
        districtId: String
    ): Result<List<Church>> {

        return churchRepository
            .getChurchesByDistrict(
                districtId
            )
    }
}