package br.com.ide.domain.repository

import br.com.ide.domain.model.Mission

interface MissionRepository {

    suspend fun getActiveMission(): Mission?
}