package com.jonlatane.beatpad.storage

/*
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.BeanDescription
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory
import com.fasterxml.jackson.databind.ser.FilterProvider
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.jonlatane.beatpad.model.*
import com.jonlatane.beatpad.model.chord.Chord
import com.jonlatane.beatpad.model.chord.Maj7
import com.jonlatane.beatpad.model.melody.RationalMelody
import com.jonlatane.beatpad.model.melody.RecordedMIDIMelody
import org.jetbrains.anko.AnkoLogger

object HarmonyStorage : AnkoLogger {
	object Serializer: StdSerializer<Harmony>(Harmony::class.java) {
		override fun serialize(value: Harmony, jgen: JsonGenerator, provider: SerializerProvider) {
			jgen.writeStartObject()
			val javaType = provider.constructType(Harmony::class.java)
			val beanDesc: BeanDescription = provider.config.introspect(javaType)
			val serializer = BeanSerializerFactory.instance.findBeanSerializer(provider,
				javaType,
				beanDesc)
			serializer.unwrappingSerializer(null).serialize(value, jgen, provider)
			jgen.writeEndObject()
		}
	}
	object Deserializer: StdDeserializer<Harmony>(Harmony::class.java) {
		override fun deserialize(jp: JsonParser, storageContext: DeserializationContext): Harmony {
			val mapper = jp.codec as ObjectMapper
			val root = mapper.readTree<ObjectNode>(jp)
			/*send you own condition*/
//			val type = root.get("type").asText()
//			root.remove("type")
			return mapper.readValue(root.toString(), Harmony::class.java).apply {
				var lastNote: Harmony.Element.Change? = null
				elements.indices.forEach { index ->
					elements[index] = elements[index].let {
						when(it) {
							is Harmony.Element.Change -> {
								lastNote = it
								it
							}
							is Harmony.Element.NoChange -> {
								if (lastNote == null) Harmony.Element.Change(Chord(0, Maj7)) // Invalid Sustain location
								else Harmony.Element.NoChange(lastNote!!)
							}
						}
					}
				}
			}
		}
	}
}*/
