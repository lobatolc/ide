package br.com.ide.domain.usecase

import br.com.ide.domain.model.Mission
import br.com.ide.domain.repository.MissionRepository
import javax.inject.Inject

class GetMissionsUseCase @Inject constructor(
    private val missionRepository: MissionRepository
) {

    suspend operator fun invoke(): List<Mission> {
        return missionRepository.getMissions()
    }
}