package com.example.ochataku.data.local.contact

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "contact")
data class ContactEntity(
    @SerializedName("contact_id")
    @ColumnInfo(name = "contact_id") @PrimaryKey val contactId: Long,

    @SerializedName("user_id")
    @ColumnInfo(name = "user_id") val userId: Long,

    @SerializedName("peer_id")
    @ColumnInfo(name = "peer_id") val peerId: Long,

    @SerializedName("remark_name")
    @ColumnInfo(name = "remark_name") val remarkName: String?
)
