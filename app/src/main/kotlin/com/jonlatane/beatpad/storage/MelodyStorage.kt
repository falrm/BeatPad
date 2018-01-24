package com.jonlatane.beatpad.storage

import android.content.Context
import android.util.Log
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.google.gson.*
import com.jonlatane.beatpad.model.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import java.io.*
import com.jonlatane.beatpad.model.Melody
import com.jonlatane.beatpad.model.melody.RationalMelody
import com.jonlatane.beatpad.model.melody.RecordedAudioMelody
import org.jetbrains.anko.info

object MelodyStorage : AnkoLogger {
	object Serializer: StdSerializer<Melody>(Melody::class.java) {
		override fun serialize(value: Melody, jgen: JsonGenerator, provider: SerializerProvider) {
			jgen.writeStartObject()
			val javaType = provider.constructType(Melody::class.java)
			val beanDesc: BeanDescription = provider.config.introspect(javaType)
			val serializer = BeanSerializerFactory.instance.findBeanSerializer(provider,
				javaType,
				beanDesc)
			serializer.unwrappingSerializer(null).serialize(value, jgen, provider)
			jgen.writeObjectField("type", value.type)
			jgen.writeEndObject()
		}
	}
	object Deserializer: StdDeserializer<Melody>(Melody::class.java) {
		override fun deserialize(jp: JsonParser, context: DeserializationContext): Melody {
			val mapper = jp.codec as ObjectMapper
			val root = mapper.readTree<ObjectNode>(jp)
			/*write you own condition*/
			val type = root.get("type").asText()
			root.remove("type")
			return when(type) {
				"rational" -> mapper.readValue(root.toString(), RationalMelody::class.java)
				"audio" -> mapper.readValue(root.toString(), RecordedAudioMelody::class.java)
				else -> TODO()
			}.apply {
				var lastNote: Note? = null
				elements.indices.forEach { index ->
					val melement = elements[index]
					when (melement) {
						is Note -> lastNote = melement
						is Sustain -> {
							if (lastNote == null) elements[index] = Rest() // Invalid Sustain location
							else melement.note = lastNote ?: melement.note
						}
					}
				}
			}
		}
	}

	object ElementSerializer: StdDeserializer<Melement>(Melement::class.java) {
		override fun deserialize(jp: JsonParser, context: DeserializationContext): Melement {
			val mapper = jp.codec as ObjectMapper
			val root = mapper.readTree<ObjectNode>(jp)
			/*write you own condition*/
			val klass = if(root.has("tones")) Note::class.java else Sustain::class.java
			return mapper.readValue(root.toString(), klass)
		}

	}


	fun storeSequence(melody: Melody, context: Context) = try {
		val outputStreamWriter = OutputStreamWriter(context.openFileOutput("sequence.json", Context.MODE_PRIVATE))
		val json = PaletteStorage.mapper.writeValueAsString(melody)
		info("Stored Melody: $json")
		outputStreamWriter.write(json)
		outputStreamWriter.close()
	} catch (e: IOException) {
		Log.e("Exception", "File write failed: " + e.toString());
	}

	fun loadSequence(context: Context): Melody = try {
		val json: String = InputStreamReader(context.openFileInput("sequence.json")).use { it.readText() }
		info("Loaded Melody: $json")
		PaletteStorage.mapper.readValue(json, Melody::class.java)
	} catch (t: Throwable) {
		error("Failed to load stored sequence", t)
		PaletteStorage.baseMelody
	}
}
