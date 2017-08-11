package com.jonlatane.beatpad.output.instrument.midi

import android.content.Context
import android.content.pm.PackageManager
import android.media.midi.*
import android.os.Build
import android.os.Handler
import android.support.annotation.RequiresApi
import com.jonlatane.beatpad.MainApplication
import android.os.HandlerThread
import org.jetbrains.anko.toast


object MidiDevices {
  private val manager: MidiManager by lazy {
    MainApplication.instance.getSystemService(Context.MIDI_SERVICE) as MidiManager
  }
  private val inputDevices = mutableListOf<Pair<MidiDeviceInfo, MidiInputPort>>()
  private val handler: Handler by lazy {
    val handlerThread = HandlerThread("MIDIDeviceHandlerThread")
    handlerThread.start()
    val looper = handlerThread.looper
    Handler(looper)
  }

  fun send(data: ByteArray) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      inputDevices.forEach {
        it.second.send(data, 0, data.size)
      }
    }
  }

  fun initialize() {
    if(MainApplication.instance.packageManager.hasSystemFeature(PackageManager.FEATURE_MIDI)
      && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    ) {
      val infos = manager.devices
      for(info in infos) {
        setupDevice(info)
      }
      manager.registerDeviceCallback(object: MidiManager.DeviceCallback() {
        @RequiresApi(Build.VERSION_CODES.M)
        override fun onDeviceAdded(info: MidiDeviceInfo) {
          MainApplication.instance.toast(
            "Connecting to ${info.properties[MidiDeviceInfo.PROPERTY_NAME]}..."
          )
          setupDevice(info)
        }
        @RequiresApi(Build.VERSION_CODES.M)
        override fun onDeviceRemoved(info: MidiDeviceInfo) {
          MainApplication.instance.toast("Disconnected from ${info.name}.")
          inputDevices.removeAll {
            it.first == info
          }
        }
        override fun onDeviceStatusChanged(status: MidiDeviceStatus) {}
      }, handler)
    }
  }

  @RequiresApi(Build.VERSION_CODES.M)
  private fun setupDevice(info: MidiDeviceInfo) {
    if(info.inputPortCount > 0) {
      val portNumber = info.ports.find { it.type == MidiDeviceInfo.PortInfo.TYPE_INPUT }!!.portNumber
      manager.openDevice(info, { device ->
        val inputPort = device?.openInputPort(portNumber)
        if(inputPort != null) {
          MainApplication.instance.toast("Connected to ${info.name}!")
          inputDevices.add(info to inputPort)
        }
      }, handler)
    } else {
      MainApplication.instance.toast("${info.name} doesn't support MIDI input :(")
    }
  }

  @get:RequiresApi(Build.VERSION_CODES.M)
  private val MidiDeviceInfo.name: String get() {
    return properties[MidiDeviceInfo.PROPERTY_NAME].toString()
  }
}