package com.example.switchermidi

import android.content.Intent
import android.content.pm.PackageManager
import android.media.midi.MidiDeviceInfo
import android.media.midi.MidiManager
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.switchermidi.MIDIProgramController
import com.example.switchermidi.databinding.ActivityEntryBinding


class EntryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEntryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var switcherRadioGroup : RadioGroup = binding.switcherGroup
        val nextActivities: Array<String> = resources.getStringArray(R.array.switcherTypes)
        for (nextActivity in nextActivities) {
            val newButton : RadioButton = RadioButton(this)
            newButton.layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            newButton.text = nextActivity
            newButton.textSize = 18f
            switcherRadioGroup.addView(newButton)
        }
        // + 1 (0 is textView)
        val firstRadioButton = switcherRadioGroup.getChildAt(0 + 1)
        switcherRadioGroup.check(firstRadioButton.id)

        val nextActivity:Button = binding.nextActivityButton
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_MIDI)) {
            nextActivity.setOnClickListener {
                Toast.makeText(this, "MIDI feature is not supported.", Toast.LENGTH_LONG).show()
            }
            return
        }

//        TODO("Set device listener")

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
            R.layout.spinner_item,
            deviceInfos.map() { x -> x.properties.getString(MidiDeviceInfo.PROPERTY_NAME)!! }
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
            devicesSpinner.adapter = adapter
        }

        nextActivity.setOnClickListener {
            val radioButton : RadioButton? = switcherRadioGroup.findViewById(
                    switcherRadioGroup.checkedRadioButtonId
            )
            val intent = when ( radioButton?.text) {
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