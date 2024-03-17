package com.example.switchermidi

import android.content.Intent
import android.content.pm.PackageManager
import android.media.midi.MidiDeviceInfo
import android.media.midi.MidiManager
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.SwitcherMIDI.MIDIProgramController
import com.example.switchermidi.databinding.ActivityEntryBinding


class EntryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEntryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val switcherSpinner: Spinner = binding.switcherTypeSpinner
        ArrayAdapter.createFromResource(
            this,
            R.array.switcherTypes,
            android.R.layout.simple_spinner_item
        ).also {adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            switcherSpinner.adapter = adapter
        }

        val nextActivity:Button = binding.nextActivityButton

        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_MIDI)) {
            nextActivity.setOnClickListener {
                Toast.makeText(this, "MIDI feature is not supported.", Toast.LENGTH_LONG).show()
            }
            return
        }

        Log.d("MIDI", "Has MIDI feature")

        val midiManager = getSystemService(MIDI_SERVICE) as MidiManager
        val deviceInfos: Set<MidiDeviceInfo> = midiManager.getDevicesForTransport(MidiManager.TRANSPORT_MIDI_BYTE_STREAM)

        if (deviceInfos.isEmpty()) {
            nextActivity.setOnClickListener {
                Toast.makeText(this, "There\'re no MIDI devices connected.", Toast.LENGTH_LONG).show()
            }
            return
        }

        val devicesSpinner = binding.deviceSpinner
        ArrayAdapter<String>(
            this,
            android.R.layout.simple_spinner_item,
            deviceInfos.map() { x -> x.properties.getString(MidiDeviceInfo.PROPERTY_NAME)!! }
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            devicesSpinner.adapter = adapter
        }

        nextActivity.setOnClickListener {
            val nextActivities: Array<String> = resources.getStringArray(R.array.switcherTypes)
            val intent = when (switcherSpinner.selectedItem.toString()) {
                nextActivities[0] -> Intent(this, TwoPedalsActivity::class.java)
                nextActivities[1] -> Intent(this, OnePedalActivity::class.java)
                else -> Intent(this, TwoPedalsActivity::class.java)
            }

            MIDIProgramController.init(midiManager, devicesSpinner.selectedItemPosition)
            if (!MIDIProgramController.getInstance()!!.checkReady()) {
                Toast.makeText(this, "Could not open chosen device.", Toast.LENGTH_LONG).show()
                MIDIProgramController.destroy()
                return@setOnClickListener
            }
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MIDIProgramController.getInstance()?.close()
    }
}