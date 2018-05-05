package com.jonlatane.beatpad.storage

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.jonlatane.beatpad.model.harmony.chord.Chord

object ChordStorage {
	object Serializer : StdSerializer<Chord>(Chord::class.java) {
		override fun serialize(value: Chord, jgen: JsonGenerator, provider: SerializerProvider) {
			jgen.writeStartObject()
			jgen.writeObjectField("root", value.root)
			jgen.writeObjectField("extension", value.extension)
			jgen.writeEndObject()
		}
	}
	object Deserializer : StdDeserializer<Chord>(Chord::class.java) {
		override fun deserialize(jp: JsonParser, context: DeserializationContext): Chord {
			val mapper = jp.codec as ObjectMapper
			val root = mapper.readTree<ObjectNode>(jp)
			/*send you own condition*/
			return Chord(
				root = root["root"].asInt(),
				extension =  root["extension"].asIterable().map { it.asInt() }.toIntArray()
			)
		}
	}
}