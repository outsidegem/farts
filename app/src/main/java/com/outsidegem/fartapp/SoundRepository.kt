package com.outsidegem.fartapp
import kotlin.random.Random
object SoundRepository {
    private val sounds = listOf(R.raw.fart_01, R.raw.fart_02, R.raw.fart_03, R.raw.fart_04, R.raw.fart_05)
    fun getRandomSound(lastSoundId: Int): Int {
        val available = sounds.filter { it != lastSoundId }
        return available[Random.nextInt(available.size)]
    }
}
