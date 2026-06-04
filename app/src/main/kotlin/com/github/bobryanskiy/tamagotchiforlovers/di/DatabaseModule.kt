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
        val migrations = allMigrations()
        return Room.databaseBuilder(context, AppDatabase::class.java, "tamagotchi_db")
            .addMigrations(*migrations)
            .build()
    }

    @Provides
    @Singleton
    fun providePetDao(database: AppDatabase): PetDao = database.petDao()

    @Provides
    @Singleton
    fun providePairDao(database: AppDatabase): PairDao = database.pairDao()

    private fun allMigrations(): Array<Migration> = arrayOf(
        MIGRATION_6_7
    )

    private val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
            CREATE TABLE IF NOT EXISTS `pets_new` (
                `id` TEXT NOT NULL PRIMARY KEY,
                `name` TEXT NOT NULL,
                `owner_user_id` TEXT,
                `current_pair_id` TEXT,
                `created_at` INTEGER NOT NULL,
                `abandoned_at` INTEGER,
                `life_status` TEXT NOT NULL,
                `death_cause` TEXT,
                `hunger` INTEGER NOT NULL,
                `energy` INTEGER NOT NULL,
                `cleanliness` INTEGER NOT NULL,
                `happiness` INTEGER NOT NULL,
                `updated_at` INTEGER NOT NULL,
                `sync_status` TEXT NOT NULL DEFAULT 'SYNCED'
            )
        """)

            db.execSQL("""
            INSERT INTO pets_new (
                id, name, owner_user_id, current_pair_id, 
                created_at, abandoned_at, life_status,
                hunger, energy, cleanliness, happiness,
                updated_at, sync_status
            )
            SELECT 
                id, name, owner_user_id, current_pair_id,
                created_at, abandoned_at,
                CASE 
                    WHEN life_status IN ('SICK', 'COLLAPSED', 'ESCAPED') THEN 'NORMAL'
                    ELSE life_status
                END,
                hunger, energy, cleanliness, happiness,
                updated_at, sync_status
            FROM pets
        """)

            db.execSQL("DROP TABLE pets")

            db.execSQL("ALTER TABLE pets_new RENAME TO pets")

            db.execSQL("CREATE INDEX IF NOT EXISTS `index_pets_owner_user_id` ON `pets` (`owner_user_id` ASC)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_pets_current_pair_id` ON `pets` (`current_pair_id` ASC)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_pets_sync_status` ON `pets` (`sync_status` ASC)")
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_pets_updated_at` ON `pets` (`updated_at` ASC)")
        }
    }
}
