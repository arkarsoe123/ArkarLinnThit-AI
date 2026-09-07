package com.example.service

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.util.Log

class SystemHardwareManager(private val context: Context) {

    private var isTorchOn = false
    private var cameraId: String? = null

    private val cameraManager: CameraManager? by lazy {
        try {
            context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
        } catch (e: Exception) {
            null
        }
    }

    init {
        findCameraWithFlash()
    }

    private fun findCameraWithFlash() {
        try {
            val cm = cameraManager ?: return
            for (id in cm.cameraIdList) {
                val characteristics = cm.getCameraCharacteristics(id)
                val hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (hasFlash && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    cameraId = id
                    break
                }
            }
            if (cameraId == null && cm.cameraIdList.isNotEmpty()) {
                cameraId = cm.cameraIdList[0]
            }
        } catch (e: Exception) {
            Log.e("SystemHardwareManager", "Error finding camera: ${e.message}")
        }
    }

    fun setTorch(enable: Boolean): Boolean {
        return try {
            val cm = cameraManager ?: return false
            val id = cameraId ?: return false
            cm.setTorchMode(id, enable)
            isTorchOn = enable
            vibrate(50)
            true
        } catch (e: Exception) {
            Log.w("SystemHardwareManager", "Failed to set torch mode: ${e.message}")
            isTorchOn = enable
            false
        }
    }

    fun toggleTorch(): Boolean {
        return setTorch(!isTorchOn)
    }

    fun getTorchState(): Boolean = isTorchOn

    fun openWifiSettings() {
        try {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("SystemHardwareManager", "Error opening WiFi settings: ${e.message}")
        }
    }

    fun openQuickShare(shareText: String = "အာကာလင်းသစ် AI ဖြင့် မျှဝေခြင်း") {
        try {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareText)
                type = "text/plain"
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val shareIntent = Intent.createChooser(sendIntent, "Quick Share / မျှဝေရန်").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(shareIntent)
        } catch (e: Exception) {
            Log.e("SystemHardwareManager", "Error opening share sheet: ${e.message}")
        }
    }

    fun dialPhoneNumber(phoneNumber: String) {
        try {
            val cleanNumber = phoneNumber.replace(Regex("[^0-9+]"), "")
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$cleanNumber")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("SystemHardwareManager", "Error dialing phone: ${e.message}")
        }
    }

    fun vibrate(milliseconds: Long = 60) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val vibrator = vibratorManager?.defaultVibrator
                vibrator?.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(milliseconds)
            }
        } catch (e: Exception) {
            Log.w("SystemHardwareManager", "Vibrate error: ${e.message}")
        }
    }
}
