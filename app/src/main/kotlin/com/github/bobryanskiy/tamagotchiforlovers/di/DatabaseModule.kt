package com.github.bobryanskiy.tamagotchiforlovers.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.github.bobryanskiy.tamagotchiforlovers.data.local.dao.PairDao
import com.github.bobryanskiy.tamagotchiforlovers.data.local.dao.PetDao
import com.github.bobryanskiy.tamagotchiforlovers.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "tamagotchi_db")
            .addMigrations(*allMigrations())
            // В RELEASE обязательно false! Иначе пользователи потеряют данные.
            .fallbackToDestructiveMigration(true)
            .build()
    }

    @Provides
    @Singleton
    fun providePetDao(database: AppDatabase): PetDao = database.petDao()

    @Provides
    @Singleton
    fun providePairDao(database: AppDatabase): PairDao = database.pairDao()

    /**
     * Массив всех миграций. Добавляй сюда новые при изменении схемы.
     *
     * Пример добавления:
     * 1. Увеличь version в AppDatabase
     * 2. Создай MIGRATION_X_Y
     * 3. Добавь в этот массив
     */
    private fun allMigrations(): Array<Migration> = arrayOf(
        MIGRATION_4_5
    )

    /** Пример миграции 4 → 5 */
    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Если ничего не менялось, оставляем пустым.
            // Если добавили колонку:
            // db.execSQL("ALTER TABLE pets ADD COLUMN new_column INTEGER NOT NULL DEFAULT 0")
        }
    }
}