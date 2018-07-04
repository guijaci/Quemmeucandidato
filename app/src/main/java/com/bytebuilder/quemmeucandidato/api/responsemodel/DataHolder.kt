package com.bytebuilder.quemmeucandidato.api.responsemodel

import com.google.gson.annotations.SerializedName

/**
 * Created by guilherme on 03/07/18.
 */
data class DataHolder<T>(
        @SerializedName("dados")
        val data: T,
        val links: List<Link>
)