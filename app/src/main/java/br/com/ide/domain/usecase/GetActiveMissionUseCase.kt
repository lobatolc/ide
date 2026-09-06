package br.com.ide.domain.usecase

import br.com.ide.domain.model.Mission
import br.com.ide.domain.repository.MissionRepository
import javax.inject.Inject

class GetActiveMissionUseCase @Inject constructor(
    private val repository: MissionRepository
) {

    suspend operator fun invoke(): Mission? {
        return repository.getActiveMission()
    }
}