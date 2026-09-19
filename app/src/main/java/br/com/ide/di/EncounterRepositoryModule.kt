package br.com.ide.di

import br.com.ide.data.repository.FirestoreMissionEncounterRepository
import br.com.ide.domain.repository.MissionEncounterRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class EncounterRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMissionEncounterRepository(
        implementation: FirestoreMissionEncounterRepository
    ): MissionEncounterRepository
}
