package com.example.tzd_cell

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class MyBroadcastReceiver : BroadcastReceiver() {
    var onBarcodeScanned: ((String) -> Unit)? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        android.util.Log.d(MainActivity.TAG, "RECEIVED INTENT: action=${intent?.action}, extras=${intent?.extras}")
        if (intent?.action == MainActivity.ACTION_DATA_CODE_RECEIVED) {
            val code = intent.getStringExtra(MainActivity.DATA)
            android.util.Log.d(MainActivity.TAG, "MyBroadcastReceiver received: $code")
            android.util.Log.d(MainActivity.TAG, "Intent extras: ${intent.extras?.keySet()?.joinToString { "$it: ${intent.extras?.getString(it)}" }}")
            code?.let {
                android.util.Log.d(MainActivity.TAG, "Calling onBarcodeScanned with: $it")
                onBarcodeScanned?.invoke(it)
            }
        }
    }
} 