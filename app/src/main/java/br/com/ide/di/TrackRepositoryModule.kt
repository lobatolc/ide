package br.com.ide.di

import br.com.ide.data.repository.FirestoreMissionTrackRemoteRepository
import br.com.ide.data.repository.RoomMissionTrackLocalRepository
import br.com.ide.domain.repository.MissionTrackLocalRepository
import br.com.ide.domain.repository.MissionTrackRemoteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TrackRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMissionTrackLocalRepository(
        repository: RoomMissionTrackLocalRepository
    ): MissionTrackLocalRepository

    @Binds
    @Singleton
    abstract fun bindMissionTrackRemoteRepository(
        repository: FirestoreMissionTrackRemoteRepository
    ): MissionTrackRemoteRepository
}
