package com.example.switchermidi

import android.R.attr.value
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.SwitcherMIDI.MIDIProgramController
import com.example.switchermidi.databinding.ActivityOnePedalBinding


class OnePedalActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOnePedalBinding
    private lateinit var mMidiProgramController: MIDIProgramController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOnePedalBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val programPicker = binding.ProgramPicker
        programPicker.maxValue = 128
        programPicker.minValue = 1

        mMidiProgramController = MIDIProgramController.getInstance()!!

        programPicker.value = mMidiProgramController.getProgram() + 1

        val pedalButton: Button = binding.Pedal
        pedalButton.setOnClickListener {
            if (pedalButton.text == getString(R.string.buttonNextText)) {
//                TODO("Create states for pedal")
                pedalButton.text = getString(R.string.buttonPreviousText)
                pedalButton.alpha = 0.75f
                programPicker.value += 1

                mMidiProgramController.changeProgramUp()
            }
            else {
                pedalButton.text = getString(R.string.buttonNextText)
                pedalButton.alpha = 1f
                programPicker.value -= 1

                mMidiProgramController.changeProgramDown()
            }
        }
        programPicker.setOnValueChangedListener{_, _, newVal ->
            mMidiProgramController.setProgram(newVal-1)
            pedalButton.text = getString(R.string.buttonNextText)
            pedalButton.alpha = 1f
        }
    }
}