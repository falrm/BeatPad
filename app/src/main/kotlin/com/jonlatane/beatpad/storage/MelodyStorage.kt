package com.jonlatane.beatpad.storage

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
import com.jonlatane.beatpad.model.*
import com.jonlatane.beatpad.model.melody.RationalMelody
import org.jetbrains.anko.AnkoLogger

object MelodyStorage : AnkoLogger {
	object Serializer: StdSerializer<Melody<*>>(Melody::class.java) {
		override fun serialize(value: Melody<*>, jgen: JsonGenerator, provider: SerializerProvider) {
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
	object Deserializer: StdDeserializer<Melody<*>>(Melody::class.java) {
		override fun deserialize(jp: JsonParser, context: DeserializationContext): Melody<*> {
			val mapper = jp.codec as ObjectMapper
			val root = mapper.readTree<ObjectNode>(jp)
			/*send you own condition*/
			val type = root.get("type").asText()
			root.remove("type")
			return when(type) {
				"rational" -> mapper.readValue(root.toString(), RationalMelody::class.java)
				//"audio" -> mapper.readValue(root.toString(), RecordedAudioMelody::class.java)
				else -> TODO()
			}
		}
	}
}
