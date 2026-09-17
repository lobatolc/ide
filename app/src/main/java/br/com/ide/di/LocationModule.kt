package br.com.ide.di

import br.com.ide.data.location.AndroidMissionLocationServiceController
import br.com.ide.data.location.AndroidMissionLocationTracker
import br.com.ide.domain.location.MissionLocationServiceController
import br.com.ide.domain.location.MissionLocationTracker
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LocationModule {

    @Binds
    @Singleton
    abstract fun bindMissionLocationTracker(
        implementation: AndroidMissionLocationTracker
    ): MissionLocationTracker

    @Binds
    @Singleton
    abstract fun bindMissionLocationServiceController(
        implementation:
        AndroidMissionLocationServiceController
    ): MissionLocationServiceController
}
