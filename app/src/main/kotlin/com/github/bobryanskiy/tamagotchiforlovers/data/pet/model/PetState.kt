package com.github.bobryanskiy.tamagotchiforlovers.data.pet.model

import android.os.Parcel
import android.os.Parcelable

data class PetState(
    var hunger: Int = 0,
    var happiness: Int = 0,
    var health: Int = 100,
    var tiredness: Int = 0,
    var cleanliness: Int = 100,
    var age: Int = 0,
    var isSleeping: Boolean = false,
    var lastUpdateTime: Long = System.currentTimeMillis(),
    var startSleepTime: Long = System.currentTimeMillis()
) : Parcelable {
    enum class LifePhase {
        BABY, ADULT, ELDERLY
    }

    fun getLifePhase(): LifePhase {
        return when {
            age <= 20 -> LifePhase.BABY
            age <= 60 -> LifePhase.ADULT
            else -> LifePhase.ELDERLY
        }
    }

    // Модификаторы скорости изменения параметров
    fun getRateMultiplier(): Float {
        return when (getLifePhase()) {
            LifePhase.BABY -> 1.5f  // Щенки требуют больше внимания
            LifePhase.ADULT -> 1.0f
            LifePhase.ELDERLY -> 1.3f  // Старые питомцы устают быстрее
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(hunger)
        dest.writeInt(happiness)
        dest.writeInt(health)
        dest.writeInt(tiredness)
        dest.writeInt(cleanliness)
        dest.writeInt(age)
        dest.writeInt(if (isSleeping) 1 else 0)
        dest.writeLong(lastUpdateTime)
        dest.writeLong(startSleepTime)
    }

    companion object CREATOR : Parcelable.Creator<PetState> {
        override fun createFromParcel(parcel: Parcel): PetState {
            return PetState(
                parcel.readInt(),
                parcel.readInt(),
                parcel.readInt(),
                parcel.readInt(),
                parcel.readInt(),
                parcel.readInt(),
                parcel.readInt() == 1,
                parcel.readLong(),
                parcel.readLong(),
            )
        }

        override fun newArray(size: Int): Array<out PetState?>? {
            return arrayOfNulls(size)
        }
    }
}