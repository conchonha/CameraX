package com.example.myapplication.data

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData

private const val TAG = "IActionViewModel"

interface IActionViewModel {
    val modeHDROptionIcon : LiveData<Boolean>

    fun onPermissionResult(requestCode: Int,
                            permissions: Array<out String>,
                            grantResults: IntArray){
        Log.d(TAG, "onPermissionResult: requestCode: $requestCode -- permissions: $permissions --- grantResults: $grantResults")
    }

    fun onPermissionStatusChange(boolean: Boolean){
        Log.d(TAG, "onPermissionStatusChange: $boolean")
    }

    fun starCamera(){
        Log.d(TAG, "starCamera: ")
    }

    fun onClickAutoMode(){
        Log.d(TAG, "onClickAutoMode: ")
        starCamera()
    }

    fun setLifeCycle(lifecycleOwner: LifecycleOwner){
        Log.d(TAG, "setLifeCycle: ")
    }

    fun onClickCapture(){
        Log.d(TAG, "onClickCapture: ")
    }

    fun onClickSwapCamera(){
        Log.d(TAG, "onClickSwapCamera: ")
        starCamera()
    }
    
    fun onClickBokeh(){
        Log.d(TAG, "onClickBokeh: ")
        starCamera()
    }

    fun onClickFaceRetouch(){
        Log.d(TAG, "onClickFaceRetouch: ")
        starCamera()
    }

    fun onClickNight(){
        Log.d(TAG, "onClickNight: ")
        starCamera()
    }

    fun onClickHDR(){
        Log.d(TAG, "onClickHDR: ")
        starCamera()
    }
}