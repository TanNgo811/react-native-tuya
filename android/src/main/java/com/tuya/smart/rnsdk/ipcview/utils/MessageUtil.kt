package com.tuya.smart.rnsdk.ipcview.utils

import android.os.Message

/**
 * Created by lee on 16/5/12.
 */
class MessageUtil {

    companion object {
        @JvmStatic
        fun getMessage(msgWhat: Int, arg: Int): Message {
            val msg = Message()
            msg.what = msgWhat
            msg.arg1 = arg
            return msg
        }
    }
}
