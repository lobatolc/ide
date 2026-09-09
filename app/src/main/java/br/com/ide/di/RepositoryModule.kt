package br.com.ide.di

import br.com.ide.data.repository.FirebaseAuthRepository
import br.com.ide.data.repository.FirestoreChurchRepository
import br.com.ide.data.repository.FirestoreDistrictRepository
import br.com.ide.data.repository.FirestoreMissionRepository
import br.com.ide.data.repository.FirestoreUserRepository
import br.com.ide.domain.repository.AuthRepository
import br.com.ide.domain.repository.ChurchRepository
import br.com.ide.domain.repository.DistrictRepository
import br.com.ide.domain.repository.MissionRepository
import br.com.ide.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMissionRepository(
        repository: FirestoreMissionRepository
    ): MissionRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        repository: FirebaseAuthRepository
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        repository: FirestoreUserRepository
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindDistrictRepository(
        implementation:
        FirestoreDistrictRepository
    ): DistrictRepository

    @Binds
    @Singleton
    abstract fun bindChurchRepository(
        implementation:
        FirestoreChurchRepository
    ): ChurchRepository
}