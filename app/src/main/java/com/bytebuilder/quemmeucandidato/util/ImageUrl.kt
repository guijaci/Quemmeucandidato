package com.bytebuilder.quemmeucandidato.util

import android.graphics.drawable.Drawable
import java.io.InputStream
import java.net.URL


/**
 * Created by guilherme on 03/07/18.
 */

fun loadImageFromUrl(url: String, name: String? = null): Drawable? =
    try {
        val `is` = URL(url).content as InputStream
        Drawable.createFromStream(`is`, name)
    } catch (e: Exception) { null }
