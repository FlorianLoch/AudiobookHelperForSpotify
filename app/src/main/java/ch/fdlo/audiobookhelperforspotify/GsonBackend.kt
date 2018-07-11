package ch.fdlo.audiobookhelperforspotify

import com.google.gson.Gson
import kotlin.jvm.javaClass

class GsonBackend {
    companion object {
        val gson = Gson()
        var _instance: SerializerDeserializer? = null

        fun getInstance(): SerializerDeserializer {
            if (_instance == null) {
                // TODO Move to GsonBackend but with private constructor
                _instance = object : SerializerDeserializer {
                    override fun serialize(obj: Any): String {
                        return gson.toJson(obj)
                    }

                    override fun <T> deserialize(s: String, classOfT: Class<T>): T {
                        return gson.fromJson(s, classOfT)
                    }
                }
            }

            return _instance!!
        }
    }
}
