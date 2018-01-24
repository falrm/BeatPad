package com.jonlatane.beatpad.storage

import android.content.Context
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonSerializer
import com.jonlatane.beatpad.model.*
import com.jonlatane.beatpad.model.melody.RationalMelody
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.error
import org.jetbrains.anko.info
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory
import com.fasterxml.jackson.databind.BeanDescription
import com.jonlatane.beatpad.model.harmony.chord.Chord
import com.jonlatane.beatpad.output.instrument.MIDIInstrument


object PaletteStorage : AnkoLogger {
	private val mapper = ObjectMapper().apply {
		val module = SimpleModule().apply {
			addDeserializer(Melody::class.java, object: StdDeserializer<Melody>(Melody::class.java) {
				override fun deserialize(jp: JsonParser, context: DeserializationContext): Melody {
					val mapper = jp.codec as ObjectMapper
					val root = mapper.readTree<ObjectNode>(jp)
					/*write you own condition*/
					val type = root.get("type").asText()
					root.remove("type")
					return when(type) {
						"rational" -> mapper.readValue(root.toString(), RationalMelody::class.java)
						else -> TODO()
					}
				}
			})
			addDeserializer(Instrument::class.java, object: StdDeserializer<Instrument>(Instrument::class.java) {
				override fun deserialize(jp: JsonParser, context: DeserializationContext): Instrument {
					val mapper = jp.codec as ObjectMapper
					val root = mapper.readTree<ObjectNode>(jp)
					/*write you own condition*/
					val type = root.get("type").asText()
					root.remove("type")
					return when(type) {
						"midi" -> mapper.readValue(root.toString(), MIDIInstrument::class.java)
						else -> TODO()
					}
				}
			})
			addSerializer(Melody::class.java, object: StdSerializer<Melody>(Melody::class.java) {
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

			})
			addDeserializer(Melement::class.java, object: StdDeserializer<Melement>(Melement::class.java) {
				override fun deserialize(jp: JsonParser, context: DeserializationContext): Melement {
					val mapper = jp.codec as ObjectMapper
					val root = mapper.readTree<ObjectNode>(jp)
					/*write you own condition*/
					val klass = if(root.has("tones")) Note::class.java else Sustain::class.java
					return mapper.readValue(root.toString(), klass)
				}

			})

			addDeserializer(Chord::class.java, object: StdDeserializer<Chord>(Chord::class.java) {
				override fun deserialize(jp: JsonParser, context: DeserializationContext): Chord {
					val mapper = jp.codec as ObjectMapper
					val root = mapper.readTree<ObjectNode>(jp)
					/*write you own condition*/
					return Chord(
						root = root["root"].asInt(),
						extension =  root["extension"].asIterable().map { it.asInt() }.toIntArray()
					)
				}

			})
		}
		registerModule(module)
	}

	private val writer get() = mapper.writerWithDefaultPrettyPrinter()
	private val reader get() = mapper.reader()


	fun storePalette(palette: Palette, context: Context) = try {
		val outputStreamWriter = OutputStreamWriter(context.openFileOutput("palette.json", Context.MODE_PRIVATE))
		val json = stringify(palette)
		info("Stored palette: $json")
		outputStreamWriter.write(json)
		outputStreamWriter.close()
	} catch (e: IOException) {
		error("File write failed: " + e.toString())
	}

	fun loadPalette(context: Context): Palette = try {
		val json: String = InputStreamReader(context.openFileInput("palette.json")).use { it.readText() }
		info("Loaded palette: $json")
		val palette = parse(json)
		palette.normalizeDeserializedData()
		palette
	} catch (t: Throwable) {
		error("Failed to load stored palette", t)
		basePalette
	}

	fun stringify(palette: Palette) = writer.writeValueAsString(palette)
	fun parse(data: String) = mapper.readValue(data, Palette::class.java)

	private fun Palette.normalizeDeserializedData() {
		parts.forEach {  part ->
			part.melodies.forEach { melody ->
				melody.normalizeDeserializedData()
			}
		}
	}

	private fun Melody.normalizeDeserializedData() {
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

	val basePalette
		get() = Palette().apply {
			parts.add(Part().apply {
				melodies.add(baseMelody)
			})
		}

	val baseMelody
		get() = RationalMelody(
			elements = (1..16).map { Rest() }.toMutableList(),
			subdivisionsPerBeat = 4
		)
}
