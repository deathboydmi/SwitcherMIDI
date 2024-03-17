package com.example.switchermidi

import android.media.midi.MidiDeviceInfo
import android.media.midi.MidiManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.SwitcherMIDI.MIDIProgramController
import com.example.switchermidi.databinding.ActivityTwoPedalsBinding


class TwoPedalsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTwoPedalsBinding
    private lateinit var mMidiProgramController: MIDIProgramController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTwoPedalsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val programPicker = binding.ProgramPicker
        programPicker.maxValue = 128
        programPicker.minValue = 1

        mMidiProgramController = MIDIProgramController.getInstance()!!

        programPicker.value = mMidiProgramController.getProgram() + 1

        programPicker.setOnValueChangedListener{_, _, newVal ->
            mMidiProgramController.setProgram(newVal-1)
        }

        binding.buttonNext.setOnClickListener {
            mMidiProgramController.changeProgramUp()
        }

        binding.buttonPrevious.setOnClickListener {
            mMidiProgramController.changeProgramDown()
        }
    }
}