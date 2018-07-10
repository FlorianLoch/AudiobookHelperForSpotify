package ch.fdlo.audiobookhelperforspotify

interface SerializerDeserializer {
    fun serialize(any: Any): String
    fun <T> deserialize(s: String, classOfT: Class<T>): T
}
