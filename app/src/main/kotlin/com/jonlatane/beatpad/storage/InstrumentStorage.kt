package com.jonlatane.beatpad.storage

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.fasterxml.jackson.databind.node.ObjectNode
import com.jonlatane.beatpad.model.Instrument
import com.jonlatane.beatpad.output.instrument.MIDIInstrument

object InstrumentStorage {
	object Deserializer: StdDeserializer<Instrument>(Instrument::class.java) {
		override fun deserialize(jp: JsonParser, context: DeserializationContext): Instrument {
			val mapper = jp.codec as ObjectMapper
			val root = mapper.readTree<ObjectNode>(jp)
			/*send you own condition*/
			val type = root.get("type").asText()
			root.remove("type")
			return when(type) {
				"midi" -> mapper.readValue(root.toString(), MIDIInstrument::class.java)
				else -> TODO()
			}
		}
	}
}