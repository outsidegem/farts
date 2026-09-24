package com.outsidegem.fartapp
import android.content.Context
class AppPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("fart_prefs", Context.MODE_PRIVATE)
    var isEnabled: Boolean
        get() = prefs.getBoolean("is_enabled", true)
        set(value) = prefs.edit().putBoolean("is_enabled", value).apply()
    var lastSoundResId: Int
        get() = prefs.getInt("last_sound_res_id", -1)
        set(value) = prefs.edit().putInt("last_sound_res_id", value).apply()
}
