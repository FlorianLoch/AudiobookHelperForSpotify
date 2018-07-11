package ch.fdlo.audiobookhelperforspotify

import android.content.Context
import android.util.Log
import com.google.gson.Gson

class PlayerStatePersistence {
    companion object {
        private val gson = Gson()
        private val CONFIG_KEY = "ch.fdlo.audiobookhelperforspotify"
        private val CONFIG_SUB_KEY = "JSON"

        fun load(ctx: Context): PlayerStatePersistence {
            with (ctx.getSharedPreferences(CONFIG_KEY, Context.MODE_PRIVATE)) {
                if (contains(CONFIG_SUB_KEY).not()) {
                    android.util.Log.d("ABHfS", "No configuration stored yet! First run of the app?")
                    return createEmptyConfig()
                }

                Log.d("ABHfS", "Configuration retrieved from shared preferences API!")

                val json = getString(CONFIG_SUB_KEY, "")
                val deserialized = try {
                    gson.fromJson(json, PlayerStatePersistence::class.java)
                } catch (e: Throwable) {
                    // If an error occurs during deserialization we should reset the stored settings right-away
                    createEmptyConfig().save(ctx)
                }

                return deserialized
            }
        }

        fun createEmptyConfig(): PlayerStatePersistence {
            return with(PlayerStatePersistence()) {
                playerStateList = ArrayList<PlayerState>()
                configVersion = 1
                this
            }
        }
    }

    var configVersion: Int? = null
    var playerStateList: ArrayList<PlayerState>? = null

    fun save(ctx: Context): PlayerStatePersistence {
        val json = gson.toJson(this)

        with (ctx.getSharedPreferences(CONFIG_KEY, Context.MODE_PRIVATE).edit()) {
            putString(CONFIG_SUB_KEY, json)
            commit()
        }

        Log.d("ABHfS", "Writing updated configuration to shared preferences API!")

        return this
    }
}