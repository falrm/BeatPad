package com.jonlatane.beatpad.midi

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

class BluetoothStateMonitor(private val appContext: Context): BroadcastReceiver() {
  var isHeadsetConnected = false
    @Synchronized
    get
    @Synchronized
    private set

  /** Start monitoring */
  fun start() {
    val bluetoothManager = appContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    bluetoothManager.adapter.getProfileProxy(appContext, object: BluetoothProfile.ServiceListener {
      /** */
      override fun onServiceDisconnected(profile: Int) {
        isHeadsetConnected = false
      }

      /** */
      override fun onServiceConnected(profile: Int, proxy: BluetoothProfile?) {
        isHeadsetConnected = proxy!!.connectedDevices.size > 0
      }

    }, BluetoothProfile.HEADSET)

    appContext.registerReceiver(this, IntentFilter(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED))
  }

  /** Stop monitoring */
  fun stop() {
    appContext.unregisterReceiver(this)
  }

  /** For broadcast receiver */
  override fun onReceive(context: Context?, intent: Intent?) {
    when(intent!!.extras!!.getInt(BluetoothAdapter.EXTRA_CONNECTION_STATE)) {
      BluetoothAdapter.STATE_CONNECTED -> isHeadsetConnected = true
      BluetoothAdapter.STATE_DISCONNECTED -> isHeadsetConnected = false
      else -> {}
    }
  }
}
