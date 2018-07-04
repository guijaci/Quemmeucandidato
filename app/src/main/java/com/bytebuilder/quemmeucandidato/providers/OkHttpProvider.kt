package com.bytebuilder.quemmeucandidato.providers

import okhttp3.OkHttpClient

/**
 * Created by guilherme on 03/07/18.
 */

object OkHttpProvider {
    private var httpClient: OkHttpClient? = null

    fun create(init: OkHttpClient.Builder.() -> Unit)
            : OkHttpClient {
        val builder = OkHttpClient.Builder()
        builder.init()
        httpClient = builder.build()
        return httpClient!!
    }

    fun createIfNotExists(init: OkHttpClient.Builder.() -> Unit)
            : OkHttpClient = httpClient
            ?: create(init)
}