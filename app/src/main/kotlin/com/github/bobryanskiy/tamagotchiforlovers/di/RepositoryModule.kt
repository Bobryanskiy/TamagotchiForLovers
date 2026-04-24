package com.github.bobryanskiy.tamagotchiforlovers.di
import com.github.bobryanskiy.tamagotchiforlovers.data.repository.DefaultPairRepository
import com.github.bobryanskiy.tamagotchiforlovers.data.repository.DefaultPetRepository
import com.github.bobryanskiy.tamagotchiforlovers.data.repository.DefaultUserRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PairRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.PetRepository
import com.github.bobryanskiy.tamagotchiforlovers.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindPetRepository(impl: DefaultPetRepository): PetRepository
    @Binds @Singleton abstract fun bindPairRepository(impl: DefaultPairRepository): PairRepository
    @Binds @Singleton abstract fun bindUserRepository(impl: DefaultUserRepository): UserRepository
}