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
import com.jonlatane.beatpad.model.harmony.chord.Chord
import com.jonlatane.beatpad.model.harmony.chord.Dom7
import com.jonlatane.beatpad.model.harmony.chord.Maj
import com.jonlatane.beatpad.model.melody.RationalMelody
import org.jetbrains.anko.AnkoLogger
import java.util.*


object PaletteStorage : AnkoLogger {
  const val paletteModelVersion = 5

  val basePalette
    get() = Palette(
      sections = mutableListOf(Section(harmony = baseHarmony)),
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
      val change1 = Chord(0, Maj)
      val change2 = Chord(7, Dom7)
      return Harmony(
        changes = TreeMap(mapOf(0 to change1, 32 to change2)),
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

      val sections: MutableList<Section> = root["sections"].asIterable()
        .map { mapper.treeToValue<Section>(it) }
        .toMutableList()
      if (sections.isEmpty()) {
        sections.add(Section(harmony = baseHarmony))
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
