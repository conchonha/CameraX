package com.example.myapplication.viewmodel

import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.extensions.ExtensionMode
import androidx.camera.extensions.ExtensionsManager
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.example.myapplication.R
import com.example.myapplication.app.MyApplication
import com.example.myapplication.base.BaseViewModel
import com.example.myapplication.data.EventSender
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.lang.Exception
import kotlin.random.Random

class CameraViewModel(application: Application) : BaseViewModel(application),
    ImageCapture.OnImageSavedCallback {
    override val listPermission: MutableList<String>
        get() = mutableListOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
        )
    private val _modeHDROptionIcon = MutableLiveData<Boolean>(false)
    override val modeHDROptionIcon: LiveData<Boolean> = _modeHDROptionIcon

    //check permission camera, read and write storage
    private var checkPermission = true

    //default camera selected = false => camera sau
    private var cameraSelectedDefault = false

    //check device co support camera khong
    private var checkCamera = true

    //create instance camera provide feature
    private var cameraProvider: ProcessCameraProvider? = null

    // Create an camera extensions manager
    private var cameraExtension: ExtensionsManager? = null

    /* sử dụng chụp ảnh được thiết kế để chụp ảnh có độ phân giải cao, chất lượng cao và cung
     cấp chức năng tự động cân bằng trắng, tự động phơi sáng và lấy nét tự động (3A) */
    //image capture use case
    private var imageCapture: ImageCapture =
        ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()

    //lua chon camera sau hay camera truoc vd: LENS_FACING_BACK camera sau
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    //gan lien vs previewView dung de display camera
    val preview = Preview
        .Builder()
        .build()


    private fun initCamera() {
        ProcessCameraProvider
            .getInstance(getApplication()).let {
                it.addListener({
                    try {
                        cameraProvider = it.get()

                        if (cameraProvider?.hasCamera(cameraSelector) == false) {
                            Log.d(TAG, "initCamera: camera no avaliable")
                            showToast(R.string.camera_not_avaliable)
                            checkCamera = false
                            return@addListener
                        }

                        ExtensionsManager
                            .getInstanceAsync(getApplication(), cameraProvider!!)
                            .let { extensionManager ->
                                extensionManager
                                    .addListener({
                                        cameraExtension = extensionManager.get()
                                    }, ContextCompat.getMainExecutor(getApplication()))
                            }

                        starCamera()
                    } catch (e: Exception) {
                        //camera khong co san
                        Log.d(TAG, "initCamera: ${e.message}")
                        showToast(R.string.camera_not_avaliable)
                    }
                }, ContextCompat.getMainExecutor(getApplication()))
            }
    }

    override fun starCamera() {
        super.starCamera()
        if (!checkCamera || !checkPermission) {
            showToast(R.string.camera_not_avaliable)
            return
        }

        //bind camera with lifecycle activity
        cameraProvider?.apply {
            unbindAll()

            lifecycle?.let {
                bindToLifecycle(it, cameraSelector, preview, imageCapture)
            }
        }
    }

    override fun onClickAutoMode() {
        if (!checkCamera || !checkPermission) {
            showToast(R.string.camera_not_avaliable)
            return
        }

        //check xem camera selector hien tai co support mode auto
        if (cameraExtension?.isExtensionAvailable(cameraSelector, ExtensionMode.AUTO) == true) {
            cameraSelector = cameraExtension!!.getExtensionEnabledCameraSelector(
                cameraSelector,
                ExtensionMode.AUTO
            )
            super.onClickAutoMode()
            return
        }

        showToast(R.string.camera_extension_not_support_auto_mode)
    }

    override fun onClickCapture() {
        super.onClickCapture()
        if (!checkCamera || !checkPermission) {
            showToast(R.string.camera_not_avaliable)
            return
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, Random.nextLong(10000000000L).toString())
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        }

        imageCapture.takePicture(
            ImageCapture.OutputFileOptions.Builder(
                getApplication<Application>().contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            ).build(),
            ContextCompat.getMainExecutor(getApplication()),
            this
        )
    }


    override fun onClickSwapCamera() {
        if (!checkCamera || !checkPermission) {
            showToast(R.string.camera_not_avaliable)
            return
        }

        cameraSelectedDefault = !cameraSelectedDefault
        cameraSelector =
            if (cameraSelectedDefault)
                CameraSelector.DEFAULT_FRONT_CAMERA
            else
                CameraSelector.DEFAULT_BACK_CAMERA

        super.onClickSwapCamera()
    }

    override fun onClickBokeh() {
        reloadModeCameraOption(
            ExtensionMode.BOKEH,
            R.string.camera_extension_not_support_auto_bokeh
        )
    }

    override fun onClickFaceRetouch() {
        reloadModeCameraOption(
            ExtensionMode.FACE_RETOUCH,
            R.string.camera_extension_not_support_auto_face_retouch
        )
    }

    override fun onClickNight() {
        reloadModeCameraOption(ExtensionMode.NIGHT, R.string.camera_extension_not_support_night)
    }

    override fun onClickHDR() {
        reloadModeCameraOption(ExtensionMode.HDR, R.string.camera_extension_not_support_hdr) {
            _modeHDROptionIcon.value = !_modeHDROptionIcon.value!!
        }
    }

    private fun reloadModeCameraOption(mode: Int, toast: Int, action: (() -> Unit)? = null) {
        if (!checkCamera || !checkPermission) {
            showToast(R.string.camera_not_avaliable)
            return
        }

        if (cameraExtension?.isExtensionAvailable(cameraSelector, mode) == true) {
            cameraSelector =
                cameraExtension!!.getExtensionEnabledCameraSelector(cameraSelector, mode)
            action?.invoke()
            starCamera()
            return
        }

        showToast(toast)
    }

    override fun onPermissionStatusChange(boolean: Boolean) {
        super.onPermissionStatusChange(boolean)
        checkPermission = boolean

        if (!boolean) {
            showToast(R.string.please_provide_permission)
            return
        }

        initCamera()
    }

    //image capture result
    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
        showToast(R.string.capture_image_success)
    }

    override fun onError(exception: ImageCaptureException) {

    }
}