package com.jonlatane.beatpad.midi

import android.content.Context
import android.content.pm.PackageManager
import android.media.midi.*
import android.os.Build
import android.os.Handler
import android.support.annotation.RequiresApi
import com.jonlatane.beatpad.MainApplication
import android.os.HandlerThread
import android.view.InputDevice
import org.jetbrains.anko.toast


object MidiDevices {
  val inputs get() = inputDevices.keys
  val outputs get() = outputDevices.keys

  /**
   * Basically, skip everything in the Google guide required to reach the
   * "Sending Play ON" section. Send away! Your signals will go to all
   * [inputs] or you can specify the one it should go to.
   */
  fun send(data: ByteArray, device: MidiDeviceInfo? = null) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if(device == null) {
        inputDevices.values.forEach {
          it.send(data, 0, data.size)
        }
      } else {
        inputDevices[device]?.send(data, 0, data.size)
      }
    }
  }
  val receivers: MutableList<(InputDevice, ByteArray) -> Unit>
    = mutableListOf()

  private val manager: MidiManager by lazy {
    MainApplication.instance.getSystemService(Context.MIDI_SERVICE) as MidiManager
  }
  private val inputDevices = mutableMapOf<MidiDeviceInfo, MidiInputPort>()
  private val outputDevices = mutableMapOf<MidiDeviceInfo, MidiOutputPort>()
  private val handler: Handler by lazy {
    val handlerThread = HandlerThread("MIDIDeviceHandlerThread")
    handlerThread.start()
    val looper = handlerThread.looper
    Handler(looper)
  }
  fun initialize(context: Context) {
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
          context.toast(
            "Connecting to ${info.properties[MidiDeviceInfo.PROPERTY_NAME]}..."
          )
          setupDevice(info)
        }
        @RequiresApi(Build.VERSION_CODES.M)
        override fun onDeviceRemoved(info: MidiDeviceInfo) {
          context.toast("Disconnected from ${info.name}.")
          inputDevices.remove(info)
        }
        override fun onDeviceStatusChanged(status: MidiDeviceStatus) {}
      }, handler)
    }
  }

  @RequiresApi(Build.VERSION_CODES.M)
  private fun setupDevice(info: MidiDeviceInfo) {
    if(info.inputPortCount > 0) {
      val portNumber = info.ports.find {
        it.type == MidiDeviceInfo.PortInfo.TYPE_INPUT
      }!!.portNumber
      manager.openDevice(info, { device ->
        val inputPort = device?.openInputPort(portNumber)
        if(inputPort != null) {
          MainApplication.instance.toast("Connected to ${info.name}!")
          inputDevices[info] = inputPort
        }
      }, handler)
    } else {
      MainApplication.instance.toast(
        "${info.name} doesn't support MIDI input :("
      )
    }
    if(info.outputPortCount > 0) {
      val portNumber = info.ports.find {
        it.type == MidiDeviceInfo.PortInfo.TYPE_OUTPUT
      }!!.portNumber
      manager.openDevice(info, { device ->
        device?.info
        val inputPort = device?.openOutputPort(portNumber)
        if(inputPort != null) {
          MainApplication.instance.toast("Connected to ${info.name}!")
          outputDevices[info] = inputPort
        }
      }, handler)
    } else {
      MainApplication.instance.toast(
        "${info.name} doesn't support MIDI output :("
      )
    }
  }

  @get:RequiresApi(Build.VERSION_CODES.M)
  private val MidiDeviceInfo.name: String get() {
    return properties[MidiDeviceInfo.PROPERTY_NAME].toString()
  }
}