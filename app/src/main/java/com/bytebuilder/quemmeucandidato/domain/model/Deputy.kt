package com.bytebuilder.quemmeucandidato.domain.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

/**
 * Created by guilherme on 03/07/18.
 */

data class Deputy(
        val id: Long,
        @SerializedName("idLegislatura")
        val legislatureId: Long,
        @SerializedName("nome")
        val name: String,
        @SerializedName("siglaPartido")
        val partyInitials: String,
        @SerializedName("siglaUf")
        val stateInitials: String,
        @SerializedName("uri")
        val link: String,
        @SerializedName("uriPartido")
        val partyLink: String,
        @SerializedName("urlFoto")
        val photoLink: String) : Parcelable {

        constructor(parcel: Parcel) : this(
                parcel.readLong(),
                parcel.readLong(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString(),
                parcel.readString())

        override fun writeToParcel(parcel: Parcel, flags: Int) {
                parcel.writeLong(id)
                parcel.writeLong(legislatureId)
                parcel.writeString(name)
                parcel.writeString(partyInitials)
                parcel.writeString(stateInitials)
                parcel.writeString(link)
                parcel.writeString(partyLink)
                parcel.writeString(photoLink)
        }

        override fun describeContents(): Int {
                return 0
        }

        companion object CREATOR : Parcelable.Creator<Deputy> {
                override fun createFromParcel(parcel: Parcel): Deputy {
                        return Deputy(parcel)
                }

                override fun newArray(size: Int): Array<Deputy?> {
                        return arrayOfNulls(size)
                }
        }
}