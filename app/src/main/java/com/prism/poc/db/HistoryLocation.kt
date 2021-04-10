package com.prism.poc.db

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
/**
 * This data base entity is used to store the reward details of the current user.
 */
@Entity(tableName = "location_history")
@Parcelize
data class HistoryLocation (
    @PrimaryKey()
    val id: Long,
    var lastUpdated: Long = System.currentTimeMillis(),
    @field:SerializedName("latitude")
    val latitude: Double,
    @field:SerializedName("longitude")
    val longitude: Double,
    @field:SerializedName("time")
    val time: String,
    @field:SerializedName("address")
    var address: String=""
) : Parcelable