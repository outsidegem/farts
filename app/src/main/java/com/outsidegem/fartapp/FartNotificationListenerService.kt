package com.outsidegem.fartapp

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class FartNotificationListenerService : NotificationListenerService() {
    private lateinit var audioEngine: FartAudioEngine
    private lateinit var prefs: AppPreferences
    private var lastTriggerTime = 0L
    private val cooldownMs = 1000L

    override fun onCreate() {
        super.onCreate()
        audioEngine = FartAudioEngine(this)
        prefs = AppPreferences(this)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null || !prefs.isEnabled || sbn.isOngoing) return
        val packageName = sbn.packageName ?: return

        if (packageName == this.packageName || packageName == "com.android.systemui") return

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastTriggerTime < cooldownMs) return
        lastTriggerTime = currentTime

        val soundId = SoundRepository.getRandomSound(prefs.lastSoundResId)
        if (soundId != -1) {
            prefs.lastSoundResId = soundId
            audioEngine.play(soundId)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioEngine.stop()
    }
}
