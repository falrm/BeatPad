package opensles.android.fluidsynth.fluidsynth_android_opensles

import android.content.Context
import android.media.midi.MidiReceiver
import android.util.Log


class FluidsynthMidiReceiver(
  val nativeLibJNI: NativeLibJNI
) : MidiReceiver()
{
    companion object {
        internal fun Byte.toUnsigned() = if (this < 0) 256 + this else this.toInt()
    }
    override fun onSend(msg: ByteArray?, offset: Int, count: Int, timestamp: Long) {
        // FIXME: consider timestamp
        if (msg == null)
            throw IllegalArgumentException ("null msg")
        var off = offset
        var c = count
        var runningStatus = 0
        while (c > 0) {
            var stat = msg[off].toUnsigned()
            if (stat < 0x80) {
                stat = runningStatus
            } else {
                off++
                c--
            }
            runningStatus = stat
            val ch = stat and 0x0F
            when (stat and 0xF0) {
                0x80 -> nativeLibJNI.noteOff(ch, msg[off].toUnsigned())
                0x90 -> {
                    if (msg[off + 1].toInt() == 0)
                        nativeLibJNI.noteOff(ch, msg[off].toUnsigned())
                    else
                        nativeLibJNI.noteOn(ch, msg[off].toUnsigned(), msg[off + 1].toUnsigned())
                }
                0xA0 -> {
                    // No PAf in fluidsynth?
                }
//                0xB0 -> syn.cc(ch, msg[off].toUnsigned(), msg[off + 1].toUnsigned())
                0xC0 -> nativeLibJNI.programChange(ch, msg[off].toUnsigned())
//                0xD0 -> syn.channelPressure(ch, msg[off].toUnsigned())
//                0xE0 -> syn.pitchBend(ch, msg[off].toUnsigned() + msg[off + 1].toUnsigned() * 0x80)
//                0xF0 -> syn.sysex(msg.copyOfRange(off, off + c - 1), null)
            }
            when (stat and 0xF0) {
                0xC0,0xD0 -> {
                    off++
                    c--
                }
                0xF0 -> {
                    off += c - 1
                    c = 0
                }
                else -> {
                    off += 2
                    c -= 2
                }
            }
        }
    }
}