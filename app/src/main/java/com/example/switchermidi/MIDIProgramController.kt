package com.example.switchermidi

import android.media.midi.MidiDevice
import android.media.midi.MidiDeviceInfo
import android.media.midi.MidiInputPort
import android.media.midi.MidiManager
import android.os.ConditionVariable
import android.util.Log


class MIDIProgramController(private val mManager: MidiManager, deviceCount: Int) {
    private var mDevice : MidiDevice? = null
    private var mInputPort: MidiInputPort? = null

    private val mByteBuffer = ByteArray(8)
    private val mChannel // ranges from 0 to 15
            = 0
    public var mProgram = 0

    private val STATUS_PROGRAM_CHANGE = 0xC0.toByte()

    init {
        val deviceInfos = mManager.getDevicesForTransport(MidiManager.TRANSPORT_MIDI_BYTE_STREAM)
        val targetMidiDeviceInfo = deviceInfos.elementAt(deviceCount)
        Log.d("MIDI", targetMidiDeviceInfo.properties.getString(MidiDeviceInfo.PROPERTY_NAME)!!)

        var cv = ConditionVariable()
        mManager.openDevice(targetMidiDeviceInfo, { midiDevice ->
            if (midiDevice == null) {
                Log.d("MIDI", "Could not open $targetMidiDeviceInfo.")
                return@openDevice
            }
            mDevice = midiDevice
            mInputPort = mDevice?.openInputPort(0)
            if (mInputPort == null) {
                Log.d("MIDI", "Could not open input port #0.")
                return@openDevice
            }
            cv.open()
        }, null)
        cv.block(3000)

    }

    public fun close() {
        Log.d("MIDI", "On close.")
        mInputPort?.close()
        mDevice?.close()
    }

    public fun checkReady(): Boolean {
        return (mDevice != null && mInputPort != null)
    }

    public fun changeProgramUp() {
        changeProgram(+1)
    }
    public fun changeProgramDown() {
        changeProgram(-1)
    }

    public fun changeProgram(delta: Int) {
        var program = mProgram
        program += delta
        if (program < 0) {
            program = 0
        } else if (program > 127) {
            program = 127
        }

        setProgram(program)
    }

    public fun setProgram(program: Int) {
        midiCommand(STATUS_PROGRAM_CHANGE + mChannel, program)
        mProgram = program
    }
    public fun getProgram() = mProgram

    private fun midiCommand(status: Int, data1: Int) {
        mByteBuffer[0] = status.toByte()
        mByteBuffer[1] = data1.toByte()
        val now = System.nanoTime()
        mInputPort?.send(mByteBuffer, 0,2, now)
    }

    companion object {
        @Volatile
        private var instance : MIDIProgramController? = null

        public fun getInstance() =
            if (instance != null) {
                synchronized(this) { instance }
            } else {
                null
            }

        public fun init(midiManager: MidiManager,deviceCount: Int) =
            instance ?: synchronized(this) {
                instance ?: MIDIProgramController(
                    midiManager, deviceCount
                ).also { instance = it }
            }

        public fun destroy() {
            Log.d("MIDI", "On destroy.")
            instance = synchronized(this) {
                instance?.close()
                null
            }
        }
    }
}