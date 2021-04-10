package com.prism.poc.db

import androidx.annotation.WorkerThread
import androidx.room.Query

class LocationRepository(private val locationDao: LocationDao) {
    private var mAllWords: List<HistoryLocation>? = null

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    suspend fun getAll(): List<HistoryLocation>? {
        return locationDao.getAll()
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(location: HistoryLocation) {
        locationDao.insertAll(location)
    }
}