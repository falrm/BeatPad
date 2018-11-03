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
import com.jonlatane.beatpad.model.Harmony
import com.jonlatane.beatpad.model.Palette
import com.jonlatane.beatpad.model.Part
import com.jonlatane.beatpad.model.Section
import com.jonlatane.beatpad.model.harmony.Orbifold
import com.jonlatane.beatpad.model.harmony.chord.*
import com.jonlatane.beatpad.model.melody.RationalMelody
import com.jonlatane.beatpad.output.instrument.MIDIInstrument
import org.jetbrains.anko.AnkoLogger
import java.util.*
import java.util.concurrent.atomic.AtomicInteger


object PaletteStorage : AnkoLogger {
  const val paletteModelVersion = 5

  val basePalette
    get() = Palette(
      sections = mutableListOf(Section()),
      parts = mutableListOf(Part())
    )

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
      if (parts.isEmpty()) {
        parts.add(Part())
      }

      // Re-initialize MIDI channels for any parts
      val channel = AtomicInteger(0)
      parts.forEach { (it.instrument as? MIDIInstrument)?.channel = channel.getAndIncrement().toByte() }

      val sections: MutableList<Section> = root["sections"].asIterable()
        .map { mapper.treeToValue<Section>(it) }
        .toMutableList()
      if (sections.isEmpty()) {
        sections.add(Section())
      }

      val keyboardPart = parts.firstOrNull { it.id == UUID.fromString(mapper.treeToValue(root["keyboardPart"])) }
        ?: parts[0]
      val colorboardPart = parts.firstOrNull { it.id == UUID.fromString(mapper.treeToValue(root["colorboardPart"])) }
        ?: parts[0]
      val splatPart = parts.firstOrNull { it.id == UUID.fromString(mapper.treeToValue(root["splatPart"])) }
        ?: parts[0]


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

      )
    }
  }
}
