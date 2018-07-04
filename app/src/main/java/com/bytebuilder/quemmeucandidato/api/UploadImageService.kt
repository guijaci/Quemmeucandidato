package com.bytebuilder.quemmeucandidato.api

import com.bytebuilder.quemmeucandidato.api.responsemodel.ImageUploadResponse
import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * Created by guilherme on 26/06/18.
 */
interface UploadImageService {
    @Multipart
    @POST("/")
    fun postImage(@Part image: MultipartBody.Part): Single<ImageUploadResponse?>
}