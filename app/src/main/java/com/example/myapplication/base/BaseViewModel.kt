package com.example.myapplication.base

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.EventSender
import com.example.myapplication.data.IActionViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel(application: Application) : AndroidViewModel(application),
    IActionViewModel {
    val TAG = this.javaClass.name
    val REQUEST_CODE = 1000

    protected var lifecycle: LifecycleOwner? = null

    protected val evenSender = Channel<EventSender>()
    val eventReceive = evenSender.receiveAsFlow().cancellable()


    protected fun showToast(message: Int) {
        viewModelScope.launch {
            evenSender.send(EventSender.ShowToast(message))
        }
    }

    abstract val listPermission: MutableList<String>

    override fun setLifeCycle(lifecycleOwner: LifecycleOwner) {
        super.setLifeCycle(lifecycleOwner)
        lifecycle = lifecycleOwner
    }

    override fun onCleared() {
        Log.d(TAG, "onCleared: ")
        lifecycle = null
        super.onCleared()
    }
}