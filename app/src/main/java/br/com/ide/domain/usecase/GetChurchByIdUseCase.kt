package br.com.ide.domain.usecase

import br.com.ide.domain.model.Church
import br.com.ide.domain.repository.ChurchRepository
import javax.inject.Inject

class GetChurchByIdUseCase @Inject constructor(
    private val churchRepository:
    ChurchRepository
) {

    suspend operator fun invoke(
        churchId: String
    ): Result<Church> {

        return churchRepository
            .getChurchById(
                churchId
            )
    }
}