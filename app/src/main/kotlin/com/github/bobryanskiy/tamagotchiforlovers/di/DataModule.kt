package com.github.bobryanskiy.tamagotchiforlovers.di

import com.github.bobryanskiy.tamagotchiforlovers.core.logging.AppLogger
import com.github.bobryanskiy.tamagotchiforlovers.core.logging.impl.TimberAppLogger
import com.github.bobryanskiy.tamagotchiforlovers.core.string.ResourceStringProvider
import com.github.bobryanskiy.tamagotchiforlovers.data.local.dao.PairDao
import com.github.bobryanskiy.tamagotchiforlovers.data.local.dao.PetDao
import com.github.bobryanskiy.tamagotchiforlovers.data.local.datasource.LocalPairDataSource
import com.github.bobryanskiy.tamagotchiforlovers.data.local.datasource.LocalPetDataSource
import com.github.bobryanskiy.tamagotchiforlovers.data.local.datasource.RoomLocalPairDataSource
import com.github.bobryanskiy.tamagotchiforlovers.data.local.datasource.RoomLocalPetDataSource
import com.github.bobryanskiy.tamagotchiforlovers.data.remote.datasource.FirestoreRemoteDataSource
import com.github.bobryanskiy.tamagotchiforlovers.data.remote.datasource.RemoteDataSource
import com.github.bobryanskiy.tamagotchiforlovers.data.repository.AuthRepositoryImpl
import com.github.bobryanskiy.tamagotchiforlovers.data.repository.BalanceConfigRepositoryImpl
import com.github.bobryanskiy.tamagotchiforlovers.data.repository.DataStoreSessionRepository
import com.github.bobryanskiy.tamagotchiforlovers.data.repository.DataStoreSettingsRepository
import com.github.bobryanskiy.tamagotchiforlovers.data.repository.PairRepositoryImpl
import com.github.bobryanskiy.tamagotchiforlovers.data.repository.PetRepositoryImpl
import com.github.bobryanskiy.tamagotchiforlovers.data.repository.UserRepositoryImpl
import com.github.bobryanskiy.tamagotchiforlovers.data.sync.PetSyncManager
import com.github.bobryanskiy.tamagotchiforlovers.data.util.SystemClock
import com.github.bobryanskiy.tamagotchiforlovers.data.util.UuidIdGenerator
import com.github.bobryanskiy.tamagotchiforlovers.domain.provider.StringResourceProvider
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.AuthRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.BalanceConfigRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.SessionRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.SettingsRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.UserRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.util.Clock
import com.github.bobryanskiy.tamagotchiforlovers.domain.util.IdGenerator
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    // ─── Repository bindings ─────────────────────────────────────────
    @Binds @Singleton
    abstract fun bindAuthRepo(impl: AuthRepositoryImpl): AuthRepository

    @Binds @Singleton
    abstract fun bindPetRepo(impl: PetRepositoryImpl): PetRepository

    @Binds @Singleton
    abstract fun bindPairRepo(impl: PairRepositoryImpl): PairRepository

    @Binds @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository
    @Binds @Singleton
    abstract fun bindBalanceConfigRepository(impl: BalanceConfigRepositoryImpl): BalanceConfigRepository

    @Binds @Singleton
    abstract fun provideSessionRepository(impl: DataStoreSessionRepository): SessionRepository

    @Binds @Singleton
    abstract fun bindSettingsRepository(impl: DataStoreSettingsRepository): SettingsRepository

    // ─── DataSource bindings ─────────────────────────────────────────
    @Binds @Singleton
    abstract fun bindRemoteDataSource(impl: FirestoreRemoteDataSource): RemoteDataSource

    // ─── Utility bindings ────────────────────────────────────────────
    @Binds @Singleton
    abstract fun bindIdGenerator(impl: UuidIdGenerator): IdGenerator

    @Binds @Singleton
    abstract fun bindClock(impl: SystemClock): Clock

    @Binds @Singleton
    abstract fun bindAppLogger(impl: TimberAppLogger): AppLogger

    @Binds @Singleton
    abstract fun bindStringResourceProvider(impl: ResourceStringProvider): StringResourceProvider

    // ─── Providers (то что нельзя через @Binds) ─────────────────────
    companion object {
        @Provides @Singleton
        fun provideLocalPetDataSource(petDao: PetDao): LocalPetDataSource =
            RoomLocalPetDataSource(petDao)

        @Provides @Singleton
        fun provideLocalPairDataSource(pairDao: PairDao): LocalPairDataSource =
            RoomLocalPairDataSource(pairDao)

        @Provides @Singleton
        fun provideSyncManager(
            local: LocalPetDataSource,
            remote: RemoteDataSource,
            auth: AuthRepository,
            logger: AppLogger,
            @IoDispatcher io: CoroutineDispatcher
        ): PetSyncManager = PetSyncManager(local, remote, auth, logger, io)
    }
}
