package com.github.bobryanskiy.tamagotchiforlovers.di
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.github.bobryanskiy.tamagotchiforlovers.data.local.dao.PairDao
import com.github.bobryanskiy.tamagotchiforlovers.data.local.dao.PetDao
import com.github.bobryanskiy.tamagotchiforlovers.data.local.datasource.LocalPairDataSource
import com.github.bobryanskiy.tamagotchiforlovers.data.local.datasource.LocalPetDataSource
import com.github.bobryanskiy.tamagotchiforlovers.data.local.datasource.RoomLocalPairDataSource
import com.github.bobryanskiy.tamagotchiforlovers.data.local.datasource.RoomLocalPetDataSource
import com.github.bobryanskiy.tamagotchiforlovers.data.remote.datasource.FirestoreRemoteDataSource
import com.github.bobryanskiy.tamagotchiforlovers.data.remote.datasource.RemoteDataSource
import com.github.bobryanskiy.tamagotchiforlovers.data.repository.AuthRepositoryImpl
import com.github.bobryanskiy.tamagotchiforlovers.data.repository.DataStoreSessionRepository
import com.github.bobryanskiy.tamagotchiforlovers.data.repository.DefaultUserRepository
import com.github.bobryanskiy.tamagotchiforlovers.data.repository.PairRepositoryImpl
import com.github.bobryanskiy.tamagotchiforlovers.data.repository.PetRepositoryImpl
import com.github.bobryanskiy.tamagotchiforlovers.data.sync.PetSyncManager
import com.github.bobryanskiy.tamagotchiforlovers.data.util.UuidIdGenerator
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.AuthRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.SessionRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.UserRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.util.IdGenerator
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
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

    @Binds @Singleton abstract fun bindAuthRepo(impl: AuthRepositoryImpl): AuthRepository
    @Binds @Singleton abstract fun bindPetRepo(impl: PetRepositoryImpl): PetRepository
    @Binds @Singleton abstract fun bindPairRepo(impl: PairRepositoryImpl): PairRepository
    @Binds @Singleton abstract fun bindRemoteDataSource(impl: FirestoreRemoteDataSource): RemoteDataSource
    @Binds @Singleton abstract fun bindUserRepository(impl: DefaultUserRepository): UserRepository
    @Binds @Singleton abstract fun bindIdGenerator(impl: UuidIdGenerator): IdGenerator

    companion object {
        @Provides
        @Singleton
        fun provideSessionRepository(dataStore: DataStore<Preferences>): SessionRepository =
            DataStoreSessionRepository(dataStore)

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
            @IoDispatcher io: CoroutineDispatcher
        ): PetSyncManager = PetSyncManager(local, remote, auth, io)

        @Provides
        @Singleton
        fun provideFirestore(): FirebaseFirestore {
            return Firebase.firestore
        }

        @Provides
        @Singleton
        fun provideFirebaseAuth(): FirebaseAuth {
            return Firebase.auth
        }
    }
}