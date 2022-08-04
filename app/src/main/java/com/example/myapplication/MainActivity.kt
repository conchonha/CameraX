package com.example.myapplication

import com.example.myapplication.base.BaseActivity
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.viewmodel.CameraViewModel


class MainActivity : BaseActivity<ActivityMainBinding,CameraViewModel>(CameraViewModel::class.java) {
    override val layout: Int
        get() = R.layout.activity_main

    override fun initView() {
        binding.action = viewModel
        viewModel.preview.setSurfaceProvider(binding.previewCamera.surfaceProvider)
    }
}