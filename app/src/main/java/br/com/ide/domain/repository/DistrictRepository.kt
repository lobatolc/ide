package br.com.ide.domain.repository

import br.com.ide.domain.model.District

interface DistrictRepository {

    suspend fun getDistricts():
            Result<List<District>>

    suspend fun getDistrictById(
        districtId: String
    ): Result<District>
}