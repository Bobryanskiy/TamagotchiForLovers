package com.github.bobryanskiy.tamagotchiforlovers.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.github.bobryanskiy.tamagotchiforlovers.data.local.dao.PetDao
import com.github.bobryanskiy.tamagotchiforlovers.data.local.entity.PetEntity

@Database(
    entities = [PetEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun petDao(): PetDao
}