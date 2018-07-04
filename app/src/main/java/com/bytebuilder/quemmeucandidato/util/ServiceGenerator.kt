package com.bytebuilder.quemmeucandidato.util

import android.net.Uri
import com.bytebuilder.quemmeucandidato.providers.OkHttpProvider
import com.bytebuilder.quemmeucandidato.providers.RetrofitProvider
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Created by guilherme on 26/06/18.
 */

inline fun <reified T> createService(uri: Uri,
                  readTimeout: Long = 60L,
                  readTimeOutUnit: TimeUnit = TimeUnit.SECONDS,
                  writeTimeout: Long = 120L,
                  writeTimeOutUnit: TimeUnit = TimeUnit.SECONDS) =
        RetrofitProvider.createIfNotExists(uri) {
            client(OkHttpProvider.createIfNotExists {
                readTimeout(readTimeout, readTimeOutUnit)
                writeTimeout(writeTimeout, writeTimeOutUnit)
            })
            addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            addConverterFactory(GsonConverterFactory.create())
        }.create(T::class.java)!!

