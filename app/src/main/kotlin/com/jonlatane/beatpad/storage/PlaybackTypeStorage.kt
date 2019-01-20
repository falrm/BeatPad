package com.jonlatane.beatpad.storage

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.jonlatane.beatpad.model.Section
import org.jetbrains.anko.AnkoLogger


object PlaybackTypeStorage : AnkoLogger {
  object Serializer: StdSerializer<Section.PlaybackType>(Section.PlaybackType::class.java) {
    override fun serialize(value: Section.PlaybackType, jgen: JsonGenerator, provider: SerializerProvider) {
      jgen.writeStartObject()
      jgen.writeObjectField("type", when(value) {
        Section.PlaybackType.Indefinite -> "indefinite"
        is Section.PlaybackType.Repeat -> "repeat"
        Section.PlaybackType.Disabled      -> "disabled"
      })
      when(value) {
        is Section.PlaybackType.Repeat ->
          jgen.writeNumberField("repetitions", value.repetitions)
      }
      jgen.writeEndObject()
    }
  }
  object Deserializer: StdDeserializer<Section.PlaybackType>(Section.PlaybackType::class.java) {
    override fun deserialize(jp: JsonParser, context: DeserializationContext): Section.PlaybackType {
      val mapper = jp.codec as ObjectMapper
      val root = mapper.readTree<ObjectNode>(jp)
      /*send you own condition*/
      val type = root.get("type").asText()
      root.remove("type")
      return when(type) {
        "indefinite" -> Section.PlaybackType.Indefinite
        "disabled" -> Section.PlaybackType.Disabled
        "repeat" -> mapper.readValue(root.toString(), Section.PlaybackType.Repeat::class.java)
        else -> TODO()
      }
    }
  }
}
