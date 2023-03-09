package sh.christian.ozone.store

fun PersistentStorage.stringPreference(
  key: String,
  defaultValue: String,
): Preference<String> = preference(key, defaultValue, StringSerializer)

fun PersistentStorage.intPreference(
  key: String,
  defaultValue: Int,
): Preference<Int> = preference(key, defaultValue, IntSerializer)

fun PersistentStorage.longPreference(
  key: String,
  defaultValue: Long,
): Preference<Long> = preference(key, defaultValue, LongSerializer)

fun PersistentStorage.floatPreference(
  key: String,
  defaultValue: Float,
): Preference<Float> = preference(key, defaultValue, FloatSerializer)

fun PersistentStorage.doublePreference(
  key: String,
  defaultValue: Double,
): Preference<Double> = preference(key, defaultValue, DoubleSerializer)

fun PersistentStorage.bytePreference(
  key: String,
  defaultValue: Byte,
): Preference<Byte> = preference(key, defaultValue, ByteSerializer)

fun PersistentStorage.booleanPreference(
  key: String,
  defaultValue: Boolean,
): Preference<Boolean> = preference(key, defaultValue, BooleanSerializer)

private object StringSerializer : Serializer<String> {
  override fun serialize(value: String): String = value
  override fun deserialize(serialized: String): String = serialized
}

private object IntSerializer : Serializer<Int> {
  override fun serialize(value: Int): String = value.toString()
  override fun deserialize(serialized: String): Int = serialized.toInt()
}

private object LongSerializer : Serializer<Long> {
  override fun serialize(value: Long): String = value.toString()
  override fun deserialize(serialized: String): Long = serialized.toLong()
}

private object FloatSerializer : Serializer<Float> {
  override fun serialize(value: Float): String = value.toString()
  override fun deserialize(serialized: String): Float = serialized.toFloat()
}

private object DoubleSerializer : Serializer<Double> {
  override fun serialize(value: Double): String = value.toString()
  override fun deserialize(serialized: String): Double = serialized.toDouble()
}

private object ByteSerializer : Serializer<Byte> {
  override fun serialize(value: Byte): String = value.toString()
  override fun deserialize(serialized: String): Byte = serialized.toByte()
}

private object BooleanSerializer : Serializer<Boolean> {
  override fun serialize(value: Boolean): String = value.toString()
  override fun deserialize(serialized: String): Boolean = serialized.toBoolean()
}
