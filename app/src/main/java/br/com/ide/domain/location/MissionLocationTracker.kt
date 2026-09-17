package br.com.ide.domain.location

import br.com.ide.domain.model.MissionLocationUpdate
import kotlinx.coroutines.flow.Flow

interface MissionLocationTracker {

    fun observeLocation():
            Flow<MissionLocationUpdate>
}