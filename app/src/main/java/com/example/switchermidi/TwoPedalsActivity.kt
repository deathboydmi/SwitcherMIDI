package com.example.switchermidi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.switchermidi.databinding.ActivityTwoPedalsBinding


class TwoPedalsActivity : AppCompatActivity() {
//    TODO("Create base activity for pedals")

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
            programPicker.value += 1
        }

        binding.buttonPrevious.setOnClickListener {
            mMidiProgramController.changeProgramDown()
            programPicker.value -= 1
        }
    }
}