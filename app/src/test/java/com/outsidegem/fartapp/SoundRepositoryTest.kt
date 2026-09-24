package com.outsidegem.fartapp

import org.junit.Assert.*
import org.junit.Test

class SoundRepositoryTest {

    @Test
    fun testRandomization() {
        // Ensure the list is not empty
        assertTrue(SoundRepository.sounds.isNotEmpty())
        
        // Ensure random selection does not return the same ID if more than 1 exist
        val lastId = SoundRepository.sounds.first()
        val nextId = SoundRepository.getRandomSound(lastId)
        
        if (SoundRepository.sounds.size > 1) {
            assertNotEquals(lastId, nextId)
        }
        
        assertTrue(SoundRepository.sounds.contains(nextId))
    }
}
