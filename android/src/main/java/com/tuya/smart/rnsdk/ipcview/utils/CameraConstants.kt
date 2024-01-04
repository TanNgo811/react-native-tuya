package com.tuya.smart.rnsdk.ipcview.utils

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.app.Activity
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.lang.Exception

object CameraConstants {
    const val ARG1_OPERATE_SUCCESS = 0
    const val ARG1_OPERATE_FAIL = 1
    const val MSG_CONNECT = 2033
    const val MSG_MUTE = 2024

    //Data type: enum
    const val PTZ_CONTROL = "119"

    //Data type: boolean
    const val PTZ_STOP = "116"
    const val PTZ_UP = "0"
    const val PTZ_LEFT = "2"
    const val PTZ_DOWN = "4"
    const val PTZ_RIGHT = "6"
    @Synchronized
    fun requestPermission(
        context: Context?,
        permission: String,
        requestCode: Int,
        tip: String?
    ): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }
        return if (ContextCompat.checkSelfPermission(
                context!!,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    (context as Activity?)!!,
                    permission
                )
            ) {
                ToastUtil.shortToast(context, tip)
            } else {
                ActivityCompat.requestPermissions(
                    (context as Activity?)!!, arrayOf(permission),
                    requestCode
                )
                return false
            }
            false
        } else {
            true
        }
    }

    @SuppressLint("all")
    fun hasRecordPermission(): Boolean {
        val minBufferSize = AudioRecord.getMinBufferSize(
            8000,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSizeInBytes = 640
        val audioData = ByteArray(bufferSizeInBytes)
        val readSize: Int
        var audioRecord: AudioRecord? = null
        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.DEFAULT, 8000,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, minBufferSize
            )
            // start recording
            audioRecord.startRecording()
        } catch (e: Exception) {
            audioRecord?.release()
            return false
        }
        return if (audioRecord.recordingState != AudioRecord.RECORDSTATE_RECORDING) {
            audioRecord.stop()
            audioRecord.release()
            false
        } else {
            readSize = audioRecord.read(audioData, 0, bufferSizeInBytes)
            // Check whether the recording result can be obtained
            if (readSize <= 0) {
                audioRecord.stop()
                audioRecord.release()
                Log.e("ss", "No recording permission")
                false
            } else {
                //Have permission, start recording normally and have data
                audioRecord.stop()
                audioRecord.release()
                true
            }
        }
    }
}