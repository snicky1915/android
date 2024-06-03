package com.example.loginvianaver.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.iamport.sdk.domain.core.Iamport
import com.iamport.sdk.domain.utils.CONST

class MerchantReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        when (intent.action) {
            CONST.BROADCAST_FOREGROUND_SERVICE -> {

            }
            CONST.BROADCAST_FOREGROUND_SERVICE_STOP -> {
                Iamport.failFinish()
            }
        }
    }
}