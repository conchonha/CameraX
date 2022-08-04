package com.example.myapplication.base

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.app.MyApplication
import com.example.myapplication.data.EventSender
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

abstract class BaseActivity<BD : ViewDataBinding, VM : BaseViewModel>(private val clazz: Class<VM>) :
    AppCompatActivity() {
    val TAG = "SangTB"

    lateinit var viewModel: VM
        private set

    @get:LayoutRes
    protected abstract val layout: Int

    protected lateinit var binding: BD
        private set

    private var jog: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, layout)
        binding.lifecycleOwner = this

        viewModel = ViewModelProvider(this, MyApplication.factory)[clazz]
        viewModel.setLifeCycle(this)

        initView()

        if (!checkPermission()) {
            requestPermission()
            return
        }

        jog = lifecycleScope.launchWhenStarted {
            viewModel.eventReceive.collectLatest {
                when (it) {
                    is EventSender.ShowToast -> showToast(it.message)
                }
            }
        }
    }

    abstract fun initView()

    protected open fun requestPermission() {
        if (getListPermissionDenied().isEmpty()) return
        ActivityCompat.requestPermissions(
            this,
            getListPermissionDenied(), viewModel.REQUEST_CODE
        )
    }

    private fun getListPermissionDenied() =
        viewModel.listPermission.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        checkPermission()
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    protected fun checkPermission(): Boolean {
        var boolean = true
        viewModel.listPermission.forEach {
            if (ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED) {
                boolean = false
            }
        }
        return boolean.also { viewModel.onPermissionStatusChange(it) }
    }

    protected fun showToast(message: Int) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: ")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: ")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: ")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: ")
    }

    override fun onDestroy() {
        jog?.cancel()
        jog = null
        super.onDestroy()
    }
}