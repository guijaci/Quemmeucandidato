package com.bytebuilder.quemmeucandidato.api.responsemodel

import com.google.gson.annotations.SerializedName

/**
 * Created by guilherme on 03/07/18.
 */
data class Link (
        @SerializedName("rel")
        val direction: String,
        @SerializedName("href")
        val linkReference: String){
    companion object {
        const val DIRECTION_SELF = "self"
        const val DIRECTION_FIRST = "first"
        const val DIRECTION_LAST = "last"
        const val DIRECTION_NEXT = "next"
        const val DIRECTION_PREV = "previous"
    }
}