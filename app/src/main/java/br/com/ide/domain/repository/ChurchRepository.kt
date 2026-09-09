package br.com.ide.domain.repository

import br.com.ide.domain.model.Church

interface ChurchRepository {

    suspend fun getChurches():
            Result<List<Church>>

    suspend fun getChurchesByDistrict(
        districtId: String
    ): Result<List<Church>>

    suspend fun getChurchById(
        churchId: String
    ): Result<Church>
}