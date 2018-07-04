package com.bytebuilder.quemmeucandidato.api.responsemodel

import com.google.gson.annotations.SerializedName

/**
 * Created by guilherme on 03/07/18.
 */
data class ImageUploadResponse(
        @SerializedName("face_found_in_image")
        val faceFoundInImage: Boolean,
        @SerializedName("is_picture_one_of_registered")
        val isPictureOneOfRegistered: Boolean,
        @SerializedName("politics_name")
        val politicsName: String?
)