package com.bytebuilder.quemmeucandidato.providers

import android.net.Uri
import retrofit2.Retrofit

/**
 * Created by guilherme on 03/07/18.
 */
object RetrofitProvider {
    private var retrofit = mutableMapOf<Uri, Retrofit>()

    fun create(uri: Uri, init: Retrofit.Builder.() -> Unit)
            : Retrofit {
        val builder = Retrofit.Builder()
        builder.baseUrl(uri.toString())
                .init()
        val r = builder.build()
        retrofit[uri] = r
        return r!!
    }

    fun createIfNotExists(uri: Uri, init: Retrofit.Builder.() -> Unit)
            : Retrofit = retrofit[uri] ?: create(uri, init)
}