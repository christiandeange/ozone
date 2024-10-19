package sh.christian.ozone.api.runtime

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import sh.christian.ozone.api.model.AtpEnum

inline fun <reified T : AtpEnum> stringEnumSerializer(
	noinline safeValueOf: (String) -> T
): KSerializer<T> {
	return StringEnumSerializer(T::class.simpleName!!, safeValueOf)
}

open class StringEnumSerializer<T : AtpEnum>(
	val serialName: String,
	val safeValueOf: (String) -> T
) : KSerializer<T> {
	
	override val descriptor: SerialDescriptor
		get() = buildClassSerialDescriptor(serialName)
	
	override fun deserialize(decoder: Decoder): T {
		val raw = decoder.decodeString()
		return safeValueOf(raw)
	}
	
	override fun serialize(encoder: Encoder, value: T) {
		encoder.encodeString(value.value)
	}
}