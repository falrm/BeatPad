package com.jonlatane.beatpad

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.NumberPicker
import android.widget.TextView
import com.jonlatane.beatpad.ConductorActivity.Companion.SERVICE_NAME
import com.jonlatane.beatpad.ConductorActivity.Companion.SERVICE_TYPE
import com.jonlatane.beatpad.midi.GM1Effects
import com.jonlatane.beatpad.model.harmony.Orbifold
import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import com.jonlatane.beatpad.storage.Storage
import com.jonlatane.beatpad.view.orbifold.OrbifoldView
import org.jetbrains.anko.contentView
import org.jetbrains.anko.nsdManager


fun showOrbifoldPicker(orbifold: OrbifoldView) {
	val builder = AlertDialog.Builder(orbifold.context)
	builder.setTitle("Choose an Orbifold")
	builder.setItems(Orbifold.values().map { it.title }.toTypedArray()) { _, which ->
		orbifold.orbifold = Orbifold.values()[which]
	}
	builder.show()
}

fun showInstrumentPicker(
	instrument: MIDIInstrument,
	context: Context,
	sortRecents: Boolean = true,
	onChosen: () -> Unit = {}
) {
	val recents = Storage.loadGM1InstrumentRecents(context)
	val builder = AlertDialog.Builder(context)
	builder.setTitle("Choose an instrument")
	val items = if (sortRecents) {
		recents.map { GM1Effects.MIDI_INSTRUMENT_NAMES[it] }.toTypedArray()
	} else {
		GM1Effects.MIDI_INSTRUMENT_NAMES.toTypedArray()
	}
	builder.setItems(items) { _, which ->
		val selection = if (sortRecents) recents[which] else which
		Storage.storeGM1InstrumentSelection(selection, context)
		instrument.instrument = selection.toByte()
		(context as? OldBaseActivity)?.updateMenuOptions()
		onChosen()
	}
	val toggleText = if (sortRecents) {
		"Sort by MIDI GM1"
	} else {
		"Sort by Recently Used"
	}
	builder.setNeutralButton(toggleText) { dialog: DialogInterface?, which: Int ->
		dialog?.cancel()
		showInstrumentPicker(
			instrument,
			context,
			!sortRecents,
			onChosen
		)
	}
	builder.show()
}

fun showConfirmDialog(
	context: Context,
	promptText: String = "Are you sure?",
	yesText: String = "Yes",
	noText: String = "No",
	noAction: () -> Unit = {},
	yesAction: () -> Unit
) {
	val dialogClickListener = DialogInterface.OnClickListener { _, which ->
		when (which) {
			DialogInterface.BUTTON_POSITIVE -> {
				yesAction.invoke()
			}

			DialogInterface.BUTTON_NEGATIVE -> {
				noAction.invoke()
			}
		}
	}

	val builder = AlertDialog.Builder(context)
	builder.setMessage(promptText).setPositiveButton(yesText, dialogClickListener)
		.setNegativeButton(noText, dialogClickListener).show()
}

fun showConductorPicker(activity: InstrumentActivity, onClose: () -> Unit = {}) {
	val nsdManager = activity.nsdManager
	val conductorList = mutableListOf<NsdServiceInfo>()
	val adapter = object : BaseAdapter() {
		override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
			val result = convertView
				?: activity.layoutInflater.inflate(R.layout.list_item_conductor, null)
			val txt = result.findViewById<TextView>(R.id.conductor)
			txt.text = conductorList[position].serviceName
			return result
		}

		override fun getItem(position: Int): Any {
			return conductorList[position]
		}

		override fun getItemId(position: Int): Long {
			return conductorList[position].hashCode().toLong()
		}

		override fun getCount(): Int {
			return conductorList.size
		}

	}
	val discoveryListener = object : NsdManager.DiscoveryListener {

		//  Called as soon as service discovery begins.
		override fun onDiscoveryStarted(regType: String) {
			Log.d("showConductorPicker", "Service discovery started")
		}

		override fun onServiceFound(service: NsdServiceInfo) {
			// A service was found!  Do something with it.
			Log.d("showConductorPicker", "Service discovery success" + service)
			if (service.serviceType != SERVICE_TYPE) {
				// Service type is the string containing the protocol and
				// transport layer for this service.
				Log.d("showConductorPicker", "Unknown Service Type: " + service.serviceType)
			} else if (service.serviceName.contains(SERVICE_NAME)) {
				conductorList.add(service)
				activity.contentView?.post {
					adapter.notifyDataSetChanged()
				}
			}
		}

		override fun onServiceLost(service: NsdServiceInfo) {
			// When the network service is no longer available.
			// Internal bookkeeping code goes here.
			Log.e("showConductorPicker", "service lost" + service)
			conductorList.remove(service)
			activity.contentView?.post {
				adapter.notifyDataSetChanged()
			}
		}

		override fun onDiscoveryStopped(serviceType: String) {
			Log.i("showConductorPicker", "Discovery stopped: " + serviceType)
			conductorList.clear()
		}

		override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
			Log.e("onStartDiscoveryFailed", "Discovery failed: Error code:" + errorCode)
			//nsdManager.stopServiceDiscovery(this)
		}

		override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
			Log.e("onStopDiscoveryFailed", "Discovery failed: Error code:" + errorCode)
			nsdManager.stopServiceDiscovery(this)
		}
	}

	nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)

	activity.contentView?.post {
		val builder = AlertDialog.Builder(activity)
		builder.setTitle("Choose Conductor")
		builder.setAdapter(adapter) { _, which: Int ->
			val conductor = conductorList[which]
			nsdManager.resolveService(conductor, object : NsdManager.ResolveListener {
				override fun onResolveFailed(serviceInfo: NsdServiceInfo?, errorCode: Int) {}

				override fun onServiceResolved(serviceInfo: NsdServiceInfo?) {
					activity.conductor = serviceInfo
				}

			})
			println(which)
		}
		builder.setOnDismissListener {
			onClose()
		}
		builder.setOnCancelListener {
			try {
				nsdManager.stopServiceDiscovery(discoveryListener)
				onClose()
			} catch (ignored: Throwable) {
			}
		}
		builder.show()
	}
}