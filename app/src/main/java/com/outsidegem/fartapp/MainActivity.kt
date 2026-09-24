package com.outsidegem.fartapp

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var prefs: AppPreferences
    private lateinit var audioEngine: FartAudioEngine
    private lateinit var listenerSwitch: Switch
    private lateinit var serviceStatusText: TextView
    private lateinit var testButton: Button
    private lateinit var settingsButton: Button
    private lateinit var selectedSoundText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = AppPreferences(this)
        audioEngine = FartAudioEngine(this)

        listenerSwitch = findViewById(R.id.listenerSwitch)
        serviceStatusText = findViewById(R.id.serviceStatusText)
        testButton = findViewById(R.id.testButton)
        settingsButton = findViewById(R.id.settingsButton)
        selectedSoundText = findViewById(R.id.selectedSoundText)

        listenerSwitch.isChecked = prefs.isEnabled
        listenerSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.isEnabled = isChecked
        }

        testButton.setOnClickListener {
            val soundId = SoundRepository.getRandomSound(prefs.lastSoundResId)
            if (soundId != -1) {
                prefs.lastSoundResId = soundId
                val resName = resources.getResourceEntryName(soundId)
                selectedSoundText.text = getString(R.string.selected_sound, "$resName.mp3")
                audioEngine.play(soundId)
            }
        }

        settingsButton.setOnClickListener {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        val hasAccess = NotificationAccessManager.hasNotificationAccess(this)
        val status = if (hasAccess) getString(R.string.status_connected) else getString(R.string.status_disconnected)
        serviceStatusText.text = getString(R.string.service_status, status)
        
        if (!hasAccess && listenerSwitch.isChecked) {
            listenerSwitch.isChecked = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioEngine.stop()
    }
}
