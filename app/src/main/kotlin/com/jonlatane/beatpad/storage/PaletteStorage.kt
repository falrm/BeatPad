package com.jonlatane.beatpad.storage

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.jonlatane.beatpad.model.*
import com.jonlatane.beatpad.model.orbifold.Orbifold
import com.jonlatane.beatpad.model.chord.*
import com.jonlatane.beatpad.model.melody.RationalMelody
import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import org.jetbrains.anko.AnkoLogger
import java.util.*
import java.util.concurrent.atomic.AtomicInteger


object PaletteStorage : AnkoLogger {
  const val paletteModelVersion = 5

  /**
   * Generates and returns a new [Palette] with a drum [Part] and a tonal [Part].
   */
  val basePalette: Palette
    get() {
      val drumPart = Part(instrument = MIDIInstrument(channel = 9, drumTrack = true))
      val tonalPart = Part()
      return Palette(
        sections = mutableListOf(Section()),
        parts = mutableListOf(drumPart, tonalPart),
        keyboardPart = drumPart,
        colorboardPart = tonalPart,
        splatPart = tonalPart
      )
    }

  /**
   * Generates and returns a new base melody of 4 empty bars in 16th notes.
   */
  val baseMelody
    get() = RationalMelody(
      changes = TreeMap((0..63).map { it to RationalMelody.Element() }.toMap()),
      length = 64,
      subdivisionsPerBeat = 4
    )

  val baseHarmony: Harmony
    get() {
      return Harmony(
        changes = TreeMap(
          mapOf(
            0  to Chord(0, Maj),
            //13 to Chord(0, Aug),
            16 to Chord(11, min7flat5),
            24 to Chord(4, MajAddFlat9),
            32 to Chord(9, min7),
            40 to Chord(2, Dom7),
            48 to Chord(2, min9),
            56 to Chord(7, sus11)
          )
        ),
        length = 64,
        subdivisionsPerBeat = 4
      )
    }

  val blankHarmony: Harmony
    get() {
      return Harmony(
        changes = TreeMap(
          mapOf(
            0  to Chord(0, (0..12).toList().toIntArray())
          )
        ),
        length = 64,
        subdivisionsPerBeat = 4
      )
    }


  object Serializer : StdSerializer<Palette>(Palette::class.java) {
    override fun serialize(value: Palette, jgen: JsonGenerator, provider: SerializerProvider) {
      jgen.writeStartObject()
      jgen.writeObjectField("id", value.id)
      jgen.writeObjectField("sections", value.sections)
      jgen.writeObjectField("parts", value.parts)
      jgen.writeObjectField("keyboardPart", value.keyboardPart?.id)
      jgen.writeObjectField("colorboardPart", value.colorboardPart?.id)
      jgen.writeObjectField("splatPart", value.splatPart?.id)
      jgen.writeObjectField("bpm", value.bpm)
      jgen.writeObjectField("orbifold", value.orbifold.name)
      jgen.writeObjectField("chord", value.chord)
      jgen.writeObjectField("modelVersion", paletteModelVersion)
      jgen.writeEndObject()
    }
  }

  object Deserializer : StdDeserializer<Palette>(Palette::class.java) {
    override fun deserialize(jp: JsonParser, context: DeserializationContext): Palette {
      val mapper = jp.codec as ObjectMapper
      val root = mapper.readTree<ObjectNode>(jp)


      val parts: MutableList<Part> = root["parts"].asIterable()
        .map { mapper.treeToValue<Part>(it) }
        .toMutableList()
      if (parts.isEmpty() || parts.all { (it.instrument as? MIDIInstrument)?.drumTrack == true }) {
        parts.add(Part())
      }

      // Re-initialize MIDI channels for any parts
      val channel = AtomicInteger(0)
      parts.forEach {
        (it.instrument as? MIDIInstrument)?.let { instrument ->
          instrument.channel = if(instrument.drumTrack) 9.toByte()
          else channel.getAndIncrement().toByte()
        }
      }

      val sections: MutableList<Section> = root["sections"].asIterable()
        .map { mapper.treeToValue<Section>(it) }
        .toMutableList()
      if (sections.isEmpty()) {
        sections.add(Section())
      }

      val keyboardPart = parts.firstOrNull { it.id == UUID.fromString(mapper.treeToValue(root["keyboardPart"])) }
        ?: parts[0]
      val colorboardPart = parts.firstOrNull { it.id == UUID.fromString(mapper.treeToValue(root["colorboardPart"])) }
        ?: parts.first { (it.instrument as? MIDIInstrument)?.drumTrack == false } ?: parts[0]
      val splatPart = parts.firstOrNull { it.id == UUID.fromString(mapper.treeToValue(root["splatPart"])) }
        ?: parts.first { (it.instrument as? MIDIInstrument)?.drumTrack == false } ?: parts[0]


      return Palette(
        id = mapper.treeToValue(root["id"]),
        sections = sections,
        parts = parts,

        keyboardPart = keyboardPart,
        colorboardPart = colorboardPart,
        splatPart = splatPart,

        bpm = mapper.treeToValue(root["bpm"]),
        orbifold = Orbifold.valueOf(mapper.treeToValue(root["orbifold"])),
        chord = mapper.treeToValue(root["chord"])

      ).apply {
        sections.forEach { section ->
          val invalidReferences = mutableListOf<Section.MelodyReference>()
          section.melodies.forEach { melodyReference ->
            melodyReference.melody = parts.flatMap { it.melodies }.firstOrNull { it.id == melodyReference.melody.id }
              ?: melodyReference.also { invalidReferences.add(it) }.melody
          }
          section.melodies.removeAll(invalidReferences)
          section.melodies.removeAll(
            section.melodies -
              section.melodies.groupBy { it.melody.id }.map { it.value.first() }.toMutableSet()
          )
          if(section.harmony == null) {
            section.harmony = blankHarmony
          }
        }

        // Deduplicate melody ids
        val melodies: List<Melody<*>> = parts.flatMap { it.melodies }
        melodies.forEach { melody: Melody<*> ->
          val others: List<Melody<*>> = melodies.toMutableList().also{ it.remove(melody) }
          while(others.map { it.id }.contains(melody.id)) {
            melody.relatedMelodies.add(melody.id)
            melody.id = UUID.randomUUID()
          }
        }

        parts.forEach { part ->
          part.melodies.forEach {
            it.drumPart = (part.instrument as? MIDIInstrument)?.drumTrack == true
          }
        }
      }
    }
  }
}
