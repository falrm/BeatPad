package fluidsynth

class FluidSynthJNI {
    external fun init(sampleRate: Int, periodSize: Int)
    external fun noteOn(channel: Int, key: Int, velocity: Int)
    external fun noteOff(channel: Int, key: Int)
    external fun programChange(channel: Int, programNumber: Int) : Boolean
    external fun controlChange(channel: Int, control: Int, value: Int) : Boolean
    external fun destroy()
    external fun addSoundFont(path: String): Int
    external fun selectSoundFont(channel: Int, soundFontId: Int)
}