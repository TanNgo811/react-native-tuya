package com.tuya.smart.rnsdk.ipcview.utils

import android.content.Context
import android.widget.Toast

object ToastUtil {
    fun shortToast(context: Context?, tips: String?) {
        Toast.makeText(context, tips, Toast.LENGTH_SHORT).show()
    }
}