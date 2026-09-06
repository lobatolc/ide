package br.com.ide.data.repository

import br.com.ide.domain.model.Mission
import br.com.ide.domain.repository.MissionRepository
import javax.inject.Inject

class FakeMissionRepository @Inject constructor() : MissionRepository {

    override suspend fun getActiveMission(): Mission {
        return Mission(
            id = 1L,
            name = "Missão Centro",
            active = true
        )
    }
}