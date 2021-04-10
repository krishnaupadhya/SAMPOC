package com.prism.poc.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LocationDao {
    @Query("SELECT * FROM location_history")
    suspend fun getAll(): List<HistoryLocation>?

    @Insert
    fun insertAll(vararg historyLocation: HistoryLocation)

    @Delete
    fun delete(history: HistoryLocation)
}