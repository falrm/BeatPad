package com.jonlatane.beatpad

import android.app.Dialog
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.NumberPicker
import com.jonlatane.beatpad.instrument.MIDIInstrument
import org.jetbrains.anko.nsdManager


/**
 * Utility class for things selected by dialog
 * Created by jonlatane on 5/8/17.
 */
fun showInstrumentPicker(activity: BaseActivity, instrument: MIDIInstrument) {
    val builder = AlertDialog.Builder(activity)
    builder.setTitle("Choose an instrument")
    builder.setItems(MIDIInstrument.MIDI_INSTRUMENT_NAMES.toTypedArray()) { _, which ->
        instrument.instrument = which.toByte()
        activity.updateInstrumentNames()
    }
    builder.show()
}

fun showTempoPicker(activity: MainActivity) {
    val dialog = Dialog(activity)
    dialog.setTitle("Select Tempo")
    dialog.setContentView(R.layout.dialog_choose_tempo)
    val picker = dialog.findViewById(R.id.numberPicker1) as NumberPicker
    picker.maxValue = 960
    picker.minValue = 15
    picker.value = activity.sequencerThread.beatsPerMinute
    picker.wrapSelectorWheel = false
    picker.setOnValueChangedListener { picker, _, _ ->
        val bpm = picker.value
        activity.sequencerThread.beatsPerMinute = bpm
        activity.updateTempoButton()
    }
    dialog.show()
}

fun showConductorPicker(activity: InstrumentActivity) {

    val discoveryListener = object : NsdManager.DiscoveryListener {

        //  Called as soon as service discovery begins.
        override fun onDiscoveryStarted(regType: String) {
            Log.d("showConductorPicker", "Service discovery started")
        }

        override fun onServiceFound(service: NsdServiceInfo) {
            // A service was found!  Do something with it.
            /*Log.d("showConductorPicker", "Service discovery success" + service)
            if (service.serviceType != SERVICE_TYPE) {
                // Service type is the string containing the protocol and
                // transport layer for this service.
                Log.d("showConductorPicker", "Unknown Service Type: " + service.serviceType)
            } else if (service.serviceName == mServiceName) {
                // The name of the service tells the user what they'd be
                // connecting to. It could be "Bob's Chat App".
                Log.d("showConductorPicker", "Same machine: " + mServiceName)
            } else if (service.serviceName.contains("NsdChat")) {
                activity.nsdManager.resolveService(service, mResolveListener)
            }*/
        }

        override fun onServiceLost(service: NsdServiceInfo) {
            // When the network service is no longer available.
            // Internal bookkeeping code goes here.
            Log.e("showConductorPicker", "service lost" + service)
        }

        override fun onDiscoveryStopped(serviceType: String) {
            Log.i("showConductorPicker", "Discovery stopped: " + serviceType)
        }

        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e("showConductorPicker", "Discovery failed: Error code:" + errorCode)
            activity.nsdManager.stopServiceDiscovery(this)
        }

        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
            Log.e("showConductorPicker", "Discovery failed: Error code:" + errorCode)
            activity.nsdManager.stopServiceDiscovery(this)
        }
    }

    activity.nsdManager.discoverServices("topologica.conductor", NsdManager.PROTOCOL_DNS_SD, discoveryListener)

    activity.nsdManager.stopServiceDiscovery(discoveryListener)
}