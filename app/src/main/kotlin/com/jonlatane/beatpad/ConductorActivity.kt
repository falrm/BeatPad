package com.jonlatane.beatpad


import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import android.view.MenuItem
import com.jonlatane.beatpad.instrument.DeviceOrientationInstrument
import com.jonlatane.beatpad.instrument.MIDIInstrument
import com.jonlatane.beatpad.sensors.Orientation
import com.jonlatane.beatpad.view.topology.*
import kotlinx.android.synthetic.main.activity_conductor.*
import org.jetbrains.anko.nsdManager
import java.net.ServerSocket

class ConductorActivity : BaseActivity() {
    override val menuResource: Int = R.menu.conduct_menu
    private val conductorInstrument = MIDIInstrument()
    private var serviceName = "TopologicaConductor"
    private val socket = ServerSocket(0)
    private val registrationListener = object: NsdManager.RegistrationListener {
        override fun onServiceRegistered(serviceInfo: NsdServiceInfo) {
            // Save the service name.  Android may have changed it in order to
            // resolve a conflict, so update the name you initially requested
            // with the name Android actually used.
            serviceName = serviceInfo.serviceName;
        }
        override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            // Registration failed!  Put debugging code here to determine why.
        }
        override fun onServiceUnregistered(arg0: NsdServiceInfo) {
            // Service has been unregistered.  This only happens when you call
            // NsdManager.unregisterService() and pass in this listener.
        }
        override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
            // Unregistration failed.  Put debugging code here to determine why.
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conductor)
        Orientation.initialize(this)
        val harmonyController = DeviceOrientationInstrument(conductorInstrument)
        RhythmAnimations.wireMelodicControl(topology, harmonyController)
        topology.onChordChangedListener = { chord ->
            harmonyController.setTones(chord.getTones(-60, 28))
        }
        topology.intermediateMode()
        registerService()
    }

    fun registerService() {
        // Create the NsdServiceInfo object, and populate it.
        val serviceInfo = NsdServiceInfo()

        // The name is subject to change based on conflicts
        // with other services advertised on the same network.
        serviceInfo.serviceName = serviceName
        serviceInfo.serviceType = "topologica.conductor"
        serviceInfo.port = socket.localPort

        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.conductorInstrument -> showInstrumentPicker(this, conductorInstrument)
            R.id.basic_mode -> topology.basicMode()
            R.id.intermediate_mode -> topology.intermediateMode()
            R.id.advanced_mode -> topology.advancedMode()
            R.id.chainsmokers_mode -> topology.chainsmokersMode()
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        topology.onResume()
    }

    override fun onPause() {
        super.onPause()
        nsdManager.unregisterService(registrationListener)
    }

    override fun updateInstrumentNames() {
        menu.findItem(R.id.conductorInstrument).title = "Instrument: ${conductorInstrument.instrumentName}"
    }
}
