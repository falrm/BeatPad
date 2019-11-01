package fluidsynth

import android.content.Context
import android.media.midi.MidiReceiver
import okio.Okio
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import java.io.File
import android.media.AudioManager




class FluidSynthMidiReceiver(
  val context: Context,
  val sf2FileName: String = defaultSf2FileName
) : MidiReceiver(), AnkoLogger {
  val nativeLibJNI: FluidSynthJNI = FluidSynthJNI()
  val sf2file = File(context.soundfontDir, sf2FileName)

  companion object {
    val defaultSf2FileName = "FluidR3Mono_GM (included).sf3"
    val baseSoundfontDir = "soundfonts"
    val Context.soundfontDir: String get() = "$filesDir${File.separator}$baseSoundfontDir"
    internal fun Byte.toUnsigned() = if (this < 0) 256 + this else this.toInt()
  }

  init {
    copySF2IfNecessary()
    info("Starting FluidSynth")
    val myAudioMgr = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    val sampleRateStr = myAudioMgr.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE)
    val sampleRate = Integer.parseInt(sampleRateStr)
    val framesPerBurstStr = myAudioMgr.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER)
    val periodSize = Integer.parseInt(framesPerBurstStr)

    nativeLibJNI.init(sampleRate, periodSize)
    nativeLibJNI.addSoundFont(sf2file.absolutePath)
  }

  private fun copySF2IfNecessary() {
    if (sf2file.exists() && sf2file.length() > 0) return
    File(context.soundfontDir).mkdirs()
    Okio.source(context.assets.open("soundfont/$sf2FileName")).use { a ->
      Okio.buffer(Okio.sink(sf2file)).use { b ->
        b.writeAll(a)
      }
    }
  }

  override fun onSend(msg: ByteArray?, offset: Int, count: Int, timestamp: Long) {
    // FIXME: consider timestamp
    val startTime = System.nanoTime()
    if (msg == null)
      throw IllegalArgumentException("null msg")
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
        0xB0 -> nativeLibJNI.controlChange(ch, msg[off].toUnsigned(), msg[off + 1].toUnsigned())
        0xC0 -> nativeLibJNI.programChange(ch, msg[off].toUnsigned())
//                0xD0 -> syn.channelPressure(ch, msg[off].toUnsigned())
//                0xE0 -> syn.pitchBend(ch, msg[off].toUnsigned() + msg[off + 1].toUnsigned() * 0x80)
//                0xF0 -> syn.sysex(msg.copyOfRange(off, off + c - 1), null)
      }
      when (stat and 0xF0) {
        0xC0, 0xD0 -> {
          off++
          c--
        }
        0xF0       -> {
          off += c - 1
          c = 0
        }
        else       -> {
          off += 2
          c -= 2
        }
      }
    }
  }
}