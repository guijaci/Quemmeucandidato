package com.bytebuilder.quemmeucandidato.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.CursorLoader
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import com.bytebuilder.quemmeucandidato.R
import com.bytebuilder.quemmeucandidato.api.DeputiesService
import com.bytebuilder.quemmeucandidato.api.UploadImageService
import com.bytebuilder.quemmeucandidato.util.createService
import com.bytebuilder.quemmeucandidato.api.responsemodel.ImageUploadResponse
import com.bytebuilder.quemmeucandidato.domain.model.Deputy
import com.bytebuilder.quemmeucandidato.util.loadImageFromUrl
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_politics.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.Buffer
import okio.BufferedSink
import okio.ForwardingSink
import okio.Okio
import org.jetbrains.anko.*
import retrofit2.HttpException
import java.io.File
import java.io.IOException

class ActivityPoliticsOverview : AppCompatActivity() {
    private val TAG = this::class.simpleName
    private val REQUEST_IMAGE_CAPTURE = 1

    private var currentPhoto: Bitmap? = null
    private var currentUri: Uri? = null
    private var currentFile: File? = null
    private var currentProgressDialog: ProgressDialog? = null

    private var currentDeputy: Deputy? = null

    private val backendUri by lazy {
        Uri.Builder()
                .scheme(getString(R.string.backend_scheme))
                .encodedAuthority(getString(R.string.backend_host))
                .encodedPath(getString(R.string.backend_path))
                .build()
    }

    private val ptUri by lazy {
        Uri.Builder()
                .scheme(getString(R.string.pt_scheme))
                .encodedAuthority(getString(R.string.pt_host))
                .encodedPath(getString(R.string.pt_path))
                .build()
    }

    private val imageUploadService by lazy { createService<UploadImageService>(backendUri) }

    private val deputiesService by lazy { createService<DeputiesService>(ptUri) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_politics)
        initListeners()
    }


    override fun onStart() {
        super.onStart()
        setDeputyVisible()
    }


    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.apply {
            putParcelable(::currentPhoto.name, currentPhoto)
            putParcelable(::currentUri.name, currentUri)
            putParcelable(::currentDeputy.name, currentDeputy)
            putSerializable(::currentFile.name, currentFile)
        }
    }


    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState?.apply {
            currentPhoto = getParcelable(::currentPhoto.name)
            currentUri = getParcelable(::currentUri.name)
            currentDeputy = getParcelable(::currentDeputy.name)
            currentFile = getSerializable(::currentFile.name) as? File?
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val extras = data?.extras
                        val projection = arrayOf(MediaStore.Images.Media.DATA)
                        val cursor = CursorLoader(this@ActivityPoliticsOverview, currentUri, projection, null, null, null)
                                .loadInBackground()
                        val columnIndexData = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                        cursor.moveToFirst()
                        val capturedImageFilePath = cursor.getString(columnIndexData)
                        currentFile = File(capturedImageFilePath)
                        currentPhoto = ((extras?.get("data")
                                ?: extras?.get(MediaStore.EXTRA_OUTPUT)) as? Bitmap?)
                                ?: BitmapFactory.decodeFile(currentFile?.path)
                        promptSendPhoto()
                    }
                    Activity.RESULT_CANCELED -> {

                    }
                    Activity.RESULT_FIRST_USER -> {

                    }
                }
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_overview, menu)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.camera_menu_item -> {
                dispatchTakePictureIntent()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun initListeners() {
        cameraButton.setOnClickListener(::onCameraButtonClick)
    }


    private fun onCameraButtonClick(view: View) = dispatchTakePictureIntent()


    private fun dispatchTakePictureIntent() {
        val tempFile = File.createTempFile("upload_photo", ".jpg", externalCacheDir)
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, tempFile.path)
        currentUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, currentUri)
        }
        if (takePictureIntent.resolveActivity(packageManager) != null)
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
    }


    private fun promptSendPhoto() {
        alert("Deseja enviar esta imagem?", "Enviar fotografia") {
            customView {
                imageView {
                    setImageBitmap(currentPhoto)
                    rotation = 90f
                }
            }
            yesButton {
                Flowable.create<Double>({ emitter ->
                    imageUploadService
                            .postImage(multipartBody(currentFile!!, emitter))
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.computation())
                            .subscribe({ response ->
                                emitter.onComplete()
                                onImageUploadResponse(response)
                            }, ::onUserError)

                }, BackpressureStrategy.LATEST)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(::onUploadProgress, ::onUploadError, ::onUploadComplete)
            }
            noButton { }
        }.show()
    }

    private fun onImageUploadResponse(response: ImageUploadResponse?) {
        Log.d(TAG, "Request Complete")
        if (response != null) {
            Log.d(TAG, response.toString())
            if (response.faceFoundInImage &&
                    response.isPictureOneOfRegistered &&
                    !response.politicsName.isNullOrBlank())
                imageLoadingSuccess(response.politicsName!!)
            else
                errorAlert("Imagem não reconhecida",
                        "Não foi possível identificar nenhuma face na imagem passada.")
                        .show()
        }
    }

    private fun onUserError(e: Throwable) {
        Log.e(TAG, "Error in request", e)
        when (e) {
            is HttpException ->
                errorAlert(details = errorDetailsMessage(e))
                        .show()
            is Exception ->
                errorAlert(details = errorDetailsMessage(e))
                        .show()
        }
    }

    private fun errorDetailsMessage(e: Exception): String =
            """
                ${e.message}
                ${e.getStackTraceString()}
                """.trimIndent()


    private fun errorDetailsMessage(e: HttpException): String =
            """
                Message: ${e.message()}
                Response code: ${e.code()}
                ${e.message}
                ${e.getStackTraceString()}
                """.trimIndent()


    private fun imageLoadingSuccess(politicsName: String) {
        indeterminateProgressDialog(
                "Face encontrada, procurando informações", "Buscando informações") {
            deputiesService.listDeputies(name = politicsName)
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ response, error ->
                        dismiss()
                        if(error != null)
                            onUserError(error)
                        else
                            onSearchDeputyResponse(response.data)
                    })
        }
    }


    private fun onSearchDeputyResponse(response: List<Deputy>?) {
        Log.d(TAG, response?.map { it.id.toString() }?.reduce { acc, i -> "$acc, $i" })
        if(response?.isNotEmpty() == true){
            currentDeputy = response.first()
            setDeputyVisible()
        }
    }


    private fun setDeputyVisible(){
        currentDeputy?.apply {
            doAsync {
                val drawable = loadImageFromUrl(photoLink)
                mugshotImage.visibility = View.VISIBLE
                uiThread{ mugshotImage.setImageDrawable(drawable) }
            }
            nameTextView.text = name
            partyTextView.text = partyInitials
            stateTextView.text = stateInitials
            cameraLayout.visibility = View.GONE
            overviewLayout.visibility = View.VISIBLE
        }
    }


    private fun errorAlert(title: String? = null,
                           message: String? = null,
                           details: String? = null)
            : AlertBuilder<AlertDialog> {
        val sTitle = title
                ?: "Erro"
        val sMessage = message
                ?: "Ocorreu um erro durante o procedimento"
        return alert(sMessage, sTitle) {
            if (details != null) {
                customView {
                    linearLayout {
                        orientation = LinearLayout.VERTICAL
                        scrollView {
                            val detailsTextView = textView(details) {
                                visibility = View.GONE
                            }
                            button("Detalhes") {
                                gravity = Gravity.START
                                setOnClickListener {
                                    detailsTextView.visibility =
                                            if (detailsTextView.visibility == View.VISIBLE)
                                                View.GONE
                                            else
                                                View.VISIBLE
                                }
                            }
                        }.lparams {
                            width = matchParent
                            horizontalMargin = sp(16)
                        }
                    }
                }
            }
            okButton { }
        }
    }


    private fun requestBody(file: File) =
            RequestBody
                    .create(MediaType.parse("image/*"), file)


    private fun multipartBody(file: File) =
            MultipartBody.Part
                    .createFormData("file", file.name, requestBody(file))


    private fun multipartBody(file: File, emitter: FlowableEmitter<Double>) =
            MultipartBody.Part
                    .createFormData("file", file.name, countingRequest(file, emitter))


    private fun countingRequest(file: File, emitter: FlowableEmitter<Double>) =
            object : RequestBody() {
                val delegate by lazy { requestBody(file) }


                override fun contentType(): MediaType? =
                        delegate.contentType()


                override fun contentLength(): Long =
                        try {
                            delegate.contentLength()
                        } catch (e: IOException) {
                            -1
                        }


                override fun writeTo(sink: BufferedSink) {
                    val countingSink = object : ForwardingSink(sink) {
                        var bytesWritten = 0L
                        override fun write(source: Buffer, byteCount: Long) {
                            super.write(source, byteCount)
                            bytesWritten += byteCount
                            val progress = (bytesWritten * 1.0) / contentLength()
                            emitter.onNext(progress)
                        }
                    }
                    val bufferedSink = Okio.buffer(countingSink)
                    delegate.writeTo(bufferedSink)
                    bufferedSink.flush()
                }
            }


    private fun onUploadProgress(progress: Double) {
        currentProgressDialog = currentProgressDialog ?: progressDialog("Aguarde a finalização do carregamento",
                "Carregando foto") {
            isIndeterminate = false
            max = 10000
            setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        }.apply { show() }
        currentProgressDialog?.progress = (10000 * progress).toInt()
    }


    private fun onUploadError(error: Throwable) {
        throw error
    }


    private fun onUploadComplete() {
        currentProgressDialog?.apply { dismiss() }
        currentProgressDialog = null
    }
}
