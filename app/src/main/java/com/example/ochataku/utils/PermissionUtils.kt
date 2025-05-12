package com.example.ochataku.utils


import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionUtils {

    const val REQUEST_CODE_CAMERA = 101
    const val REQUEST_CODE_AUDIO = 102
    const val REQUEST_CODE_MEDIA = 103

    fun requestCameraPermission(activity: Activity, onGranted: () -> Unit) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.CAMERA), REQUEST_CODE_CAMERA)
        } else {
            onGranted()
        }
    }

    fun requestAudioPermission(activity: Activity, onGranted: () -> Unit) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.RECORD_AUDIO), REQUEST_CODE_AUDIO)
        } else {
            onGranted()
        }
    }

    fun requestMediaPermission(activity: Activity, onGranted: () -> Unit) {
        val permissions = if (Build.VERSION.SDK_INT >= 33) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            mutableListOf(Manifest.permission.READ_EXTERNAL_STORAGE).apply {
                if (Build.VERSION.SDK_INT <= 28) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
        }

        val notGranted = permissions.filter {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isNotEmpty()) {
            ActivityCompat.requestPermissions(activity, notGranted.toTypedArray(), REQUEST_CODE_MEDIA)
        } else {
            onGranted()
        }
    }

    fun handlePermissionResult(
        activity: Activity,
        requestCode: Int,
        grantResults: IntArray,
        onGranted: () -> Unit
    ) {
        if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            onGranted()
        } else {
            Toast.makeText(activity, "权限被拒绝，相关功能将无法使用", Toast.LENGTH_SHORT).show()
        }
    }
}