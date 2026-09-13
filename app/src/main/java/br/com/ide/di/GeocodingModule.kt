package br.com.ide.di

import br.com.ide.data.repository.AndroidGeocodingRepository
import br.com.ide.domain.repository.GeocodingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(
    SingletonComponent::class
)
abstract class GeocodingModule {

    @Binds
    @Singleton
    abstract fun bindGeocodingRepository(
        implementation:
        AndroidGeocodingRepository
    ): GeocodingRepository
}