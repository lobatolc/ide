package br.com.ide.domain.usecase

import br.com.ide.domain.model.Church
import br.com.ide.domain.repository.ChurchRepository
import javax.inject.Inject

class GetChurchesUseCase @Inject constructor(
    private val churchRepository:
    ChurchRepository
) {

    suspend operator fun invoke():
            Result<List<Church>> {

        return churchRepository
            .getChurches()
    }
}