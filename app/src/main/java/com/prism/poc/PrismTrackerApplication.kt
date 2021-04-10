package com.prism.poc

import android.app.Application
import com.prism.poc.db.AppDatabase
import com.prism.poc.db.LocationRepository

/**
 * Created by Krishna Upadhya on 19/03/20.
 */
class PrismTrackerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        @JvmStatic
        var instance: PrismTrackerApplication? = null
            private set
    }

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { LocationRepository(database.locationDao()) }
}