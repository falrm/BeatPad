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
import com.jonlatane.beatpad.model.melody.RationalMelody
import org.jetbrains.anko.AnkoLogger
import java.util.*


object MelodyReferenceStorage : AnkoLogger {
  object Serializer: StdSerializer<Section.MelodyReference>(Section.MelodyReference::class.java) {
    override fun serialize(value: Section.MelodyReference, jgen: JsonGenerator, provider: SerializerProvider) {
      jgen.writeStartObject()
      jgen.writeObjectField("melody", value.melody.id.toString())
      jgen.writeNumberField("volume", value.volume)
      jgen.writeObjectField("playbackType", value.playbackType)
      jgen.writeEndObject()
    }
  }
  object Deserializer: StdDeserializer<Section.MelodyReference>(Section.MelodyReference::class.java) {
    override fun deserialize(jp: JsonParser, context: DeserializationContext): Section.MelodyReference {
      val mapper = jp.codec as ObjectMapper
      val root = mapper.readTree<ObjectNode>(jp)
      return Section.MelodyReference(
        // This melody is really just a stub and should be corrected by [PaletteStorage.Deserializer]
        melody = RationalMelody(id = UUID.fromString(root.get("melody").asText())),
        volume =  root.get("volume").floatValue(),
        playbackType = mapper.readValue(root.get("playbackType").toString(), Section.PlaybackType::class.java)
      )
    }
  }
}
