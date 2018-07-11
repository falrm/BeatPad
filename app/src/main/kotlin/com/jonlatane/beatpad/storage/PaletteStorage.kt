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
import com.jonlatane.beatpad.model.Palette
import com.jonlatane.beatpad.model.Part
import com.jonlatane.beatpad.model.Rest
import com.jonlatane.beatpad.model.melody.RationalMelody
import org.jetbrains.anko.AnkoLogger
import java.util.*


object PaletteStorage : AnkoLogger {

	val basePalette
		get() = Palette().apply {
			parts.add(Part())
		}

	val baseMelody
		get() = RationalMelody(
			elements = (1..16).map { Rest() }.toMutableList(),
			subdivisionsPerBeat = 4
		)


	object Serializer : StdSerializer<Palette>(Palette::class.java) {
		override fun serialize(value: Palette, jgen: JsonGenerator, provider: SerializerProvider) {
			jgen.writeStartObject()
			jgen.writeObjectField("id", value.id)
			jgen.writeObjectField("chords", value.chords)
			jgen.writeObjectField("parts", value.parts)
      jgen.writeObjectField("keyboardPart", value.keyboardPart?.id)
      jgen.writeObjectField("colorboardPart", value.colorboardPart?.id)
      jgen.writeObjectField("splatPart", value.splatPart?.id)
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
      if(parts.isEmpty()) {
        parts.add(Part())
      }

      val keyboardPart = parts.firstOrNull { it.id == UUID.fromString(mapper.treeToValue(root["keyboardPart"])) }
        ?: parts[0]
      val colorboardPart = parts.firstOrNull { it.id == UUID.fromString(mapper.treeToValue(root["colorboardPart"])) }
        ?: parts[0]
      val splatPart = parts.firstOrNull { it.id == UUID.fromString(mapper.treeToValue(root["splatPart"])) }
        ?: parts[0]

			return Palette(
				id = mapper.treeToValue(root["id"]),
				chords = mapper.treeToValue(root["chords"]),
        parts = parts,
        keyboardPart = keyboardPart,
        colorboardPart = colorboardPart,
        splatPart = splatPart
			)
		}
	}
}
