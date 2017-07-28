package com.jonlatane.beatpad


import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Bundle
import android.view.MenuItem
import com.jonlatane.beatpad.harmony.Topology.*
import com.jonlatane.beatpad.output.controller.DeviceOrientationInstrument
import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import com.jonlatane.beatpad.sensors.Orientation
import com.jonlatane.beatpad.view.topology.RhythmAnimations
import kotlinx.android.synthetic.main.activity_conductor.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.contentView
import org.jetbrains.anko.info
import org.jetbrains.anko.nsdManager
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.util.concurrent.Executors

class ConductorActivity : BaseActivity(), AnkoLogger {
    override val menuResource: Int = R.menu.conduct_menu
    private val conductorInstrument = MIDIInstrument()
    private var serviceName = SERVICE_NAME
    private val serverSocket = ServerSocket(0)
    private val executorService = Executors.newScheduledThreadPool(2)
    private val registrationListener = object: NsdManager.RegistrationListener {
        override fun onServiceRegistered(serviceInfo: NsdServiceInfo) {
            // Save the service name.  Android may have changed it in order to
            // resolve a conflict, so update the name you initially requested
            // with the name Android actually used.
            serviceName = serviceInfo.serviceName
            contentView?.post {
                title = serviceName
            }
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
            harmonyController.tones = chord.getTones()
        }
        registerService()
    }

    fun registerService() {
        // Create the NsdServiceInfo object, and populate it.
        val serviceInfo = NsdServiceInfo()

        // The name is subject to change based on conflicts
        // with other services advertised on the same network.
        serviceInfo.serviceName = SERVICE_NAME
        serviceInfo.serviceType = SERVICE_TYPE
        serviceInfo.port = serverSocket.localPort

        nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
        executorService.execute {
            while(true) {
                info("Awaiting connection on $serverSocket")
                val socket = serverSocket.accept()
                val chord = topology.chord
                val chordString = "${chord.root}:${chord.extension.joinToString(",")}"
                info("Sending $chordString to $socket")
                try {
                    val output = PrintWriter(socket.getOutputStream(), true)
                    val input = BufferedReader(InputStreamReader(socket.getInputStream()))
                    output.println(chordString)
                    output.close()
                    input.close()
                } catch(ignored: Throwable) {} finally {
                    socket.close()
                }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.conductorInstrument -> showInstrumentPicker(this, conductorInstrument)
            R.id.basic_mode -> topology.topology = basic
            R.id.intermediate_mode -> topology.topology = intermediate
            R.id.advanced_mode -> topology.topology = advanced
            R.id.master_mode -> topology.topology = master
            R.id.chainsmokers_mode -> topology.topology = chainsmokers
            R.id.pop_mode -> topology.topology = pop
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        topology.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        serverSocket.close()
        nsdManager.unregisterService(registrationListener)
    }

    override fun updateMenuOptions() {
        menu.findItem(R.id.conductorInstrument).title = "Instrument: ${conductorInstrument.instrumentName}"
    }

    companion object {
        val SERVICE_NAME = "Topologica Conductor"
        val SERVICE_TYPE = "_topologica._tcp."
    }
}
