package com.jonlatane.beatpad

import BeatClockPaletteConsumer
import BeatClockPaletteConsumer.currentSectionDrawable
import android.content.Context
import android.content.DialogInterface
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.LinearLayout.VERTICAL
import android.widget.TextView
import com.jonlatane.beatpad.ConductorActivity.Companion.SERVICE_NAME
import com.jonlatane.beatpad.ConductorActivity.Companion.SERVICE_TYPE
import com.jonlatane.beatpad.MainApplication.Companion.chordTypeface
import com.jonlatane.beatpad.MainApplication.Companion.chordTypefaceBold
import com.jonlatane.beatpad.midi.GM1Effects
import com.jonlatane.beatpad.midi.GM1Effects.MIDI_INSTRUMENT_RANGE
import com.jonlatane.beatpad.model.orbifold.Orbifold
import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import com.jonlatane.beatpad.storage.InstrumentSelectionStorage
import com.jonlatane.beatpad.util.InstaRecycler
import com.jonlatane.beatpad.util.vibrate
import com.jonlatane.beatpad.view.nonDelayedRecyclerView
import com.jonlatane.beatpad.view.orbifold.OrbifoldView
import org.jetbrains.anko.*
import org.jetbrains.anko.sdk25.coroutines.onClick


fun Context.showRenameDialog(
  currentName: String,
  entityType: String,
  onChosen: (String) -> Unit
) {
  alert("Renaming \"$currentName\"", "Rename $entityType") {
    var name: EditText? = null
    customView {
      linearLayout {
        name = editText {
          text.clear()
          text.append(currentName)
          filters = arrayOf(
            InputFilter.LengthFilter(50)
          )
        }.lparams(matchParent, wrapContent) {
          marginStart = dip(20)
          marginEnd = dip(20)
        }
      }
    }
    positiveButton("Rename $entityType") {
      onChosen(name!!.text.toString())
    }
    negativeButton("Cancel") { }
  }.show()
}

fun showOrbifoldPicker(orbifoldView: OrbifoldView) {
  val builder = AlertDialog.Builder(orbifoldView.context)
  builder.setTitle("Choose an Orbifold")
  val orbifolds: List<Orbifold> = Orbifold.values().toList() - listOf(Orbifold.custom)
  builder.setItems(orbifolds.map { it.title }.toTypedArray()) { _, which ->
    val chosenOrbifold = orbifolds[which]
    orbifoldView.orbifold = chosenOrbifold
  }
  builder.show()
}


fun showInstrumentPicker2(
  instrument: MIDIInstrument,
  context: Context
) = with(context) {
  alert {
    var search: EditText? = null
    customView {
      linearLayout {
        orientation = VERTICAL
        textView {
          textSize = 20f
          typeface = chordTypefaceBold
          text = "Choose Instrument"
          padding = dip(20f)
        }
        lateinit var adapter: RecyclerView.Adapter<*>
        search = editText {
          hint = "Search instruments"
          text.clear()
          text.append("")
          typeface = chordTypeface
          filters = arrayOf(
            InputFilter.LengthFilter(50)
          )
          addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
              adapter.notifyDataSetChanged()
            }
          })
        }.lparams(matchParent, wrapContent) {
          marginStart = dip(20)
          marginEnd = dip(20)
        }


        val recents: List<Int> = InstrumentSelectionStorage.loadGM1InstrumentRecents(context)

        // Virtual data set
        lateinit var dataSet: List<Int>
        fun sortByRecent() {
          dataSet = recents + MIDI_INSTRUMENT_RANGE.filter { !recents.contains(it) }
          // Move selected instrument to top by default
          dataSet = listOf(instrument.instrument.toInt()) + dataSet.filter { it != instrument.instrument.toInt() }
        }
        fun sortByGM1() {
          dataSet = MIDI_INSTRUMENT_RANGE.toList()
        }
        sortByRecent()
        fun filteredDataSet(): List<Int> = (search as? EditText)
          ?.takeIf { it.text.toString().isNotBlank() }
          ?.let { search ->
            val searchText = search.text.toString()
            dataSet.filter {
              //it == instrument.instrument.toInt() ||
              GM1Effects.MIDI_INSTRUMENT_NAMES[it].toLowerCase().contains(searchText.toLowerCase())
            }
          } ?: dataSet
        val recycler = InstaRecycler.instaRecycler(
          context,
          factory = { nonDelayedRecyclerView() },
          itemCount = { filteredDataSet().count() },
          binder = { position ->
            findViewById<TextView>(InstaRecycler.example_id).apply {
              text = GM1Effects.MIDI_INSTRUMENT_NAMES[filteredDataSet()[position]]
              when(instrument.instrument.toInt()) {
                filteredDataSet()[position] -> { backgroundResource = currentSectionDrawable }
                else -> { background = null }
              }
              padding = dip(16)
              isClickable = true
              onClick {
                vibrate(10)
                val selection = filteredDataSet()[position]
                InstrumentSelectionStorage.storeGM1InstrumentSelection(selection, context)
                instrument.instrument = selection.toByte()
                instrument.drumTrack = false
                instrument.sendSelectInstrument()
                adapter.notifyDataSetChanged()
                BeatClockPaletteConsumer.viewModel?.partListAdapter?.notifyDataSetChanged()
              }
            }
          }
        ).lparams(matchParent, wrapContent)
        adapter = recycler.adapter
      }
    }
  }.show()
}

fun showInstrumentPicker(
  instrument: MIDIInstrument,
  context: Context,
  sortRecents: Boolean = true,
  onChosen: () -> Unit = {}
) {
  val recents = InstrumentSelectionStorage.loadGM1InstrumentRecents(context)
  val builder = AlertDialog.Builder(context)
  builder.setTitle("Choose an instrument")
  val items = if (sortRecents) {
    recents.map { GM1Effects.MIDI_INSTRUMENT_NAMES[it] }.toTypedArray()
  } else {
    GM1Effects.MIDI_INSTRUMENT_NAMES.toTypedArray()
  }
  builder.setItems(items) { _, which ->
    val selection: Int = if (sortRecents) recents[which] else which
    InstrumentSelectionStorage.storeGM1InstrumentSelection(selection, context)
    instrument.instrument = selection.toByte()
    instrument.sendSelectInstrument()
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