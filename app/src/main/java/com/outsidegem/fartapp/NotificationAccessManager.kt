package com.outsidegem.fartapp
import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.text.TextUtils
object NotificationAccessManager {
    fun hasNotificationAccess(context: Context): Boolean {
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        if (!TextUtils.isEmpty(flat)) {
            return flat.split(":").any { ComponentName.unflattenFromString(it)?.packageName == context.packageName }
        }
        return false
    }
}
