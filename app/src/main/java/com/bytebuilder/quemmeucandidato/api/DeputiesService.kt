package com.bytebuilder.quemmeucandidato.api

import com.bytebuilder.quemmeucandidato.api.responsemodel.DataHolder
import com.bytebuilder.quemmeucandidato.domain.model.Deputy
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by guilherme on 03/07/18.
 */
interface DeputiesService {
    @GET("deputados")
    fun listDeputies(@Query("nome") name: String? = null): Single<DataHolder<List<Deputy>>>
}