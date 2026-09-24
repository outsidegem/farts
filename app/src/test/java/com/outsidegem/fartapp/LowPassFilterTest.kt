package com.outsidegem.fartapp

import org.junit.Assert.*
import org.junit.Test

class LowPassFilterTest {

    @Test
    fun testLowPassFilter_Initialization() {
        val filter = LowPassFilter(44100, 400.0, 0.707)
        assertNotNull(filter)
    }
    
    @Test
    fun testLowPassFilter_ProcessSample() {
        val filter = LowPassFilter(44100, 400.0, 0.707)
        // 100 Hz wave should pass substantially
        val lowFreqSample = 0.8
        val out1 = filter.processSample(lowFreqSample, 0)
        
        // Output should not be completely attenuated (biquad takes a few samples to stabilize, 
        // but this verifies it processes without crashing)
        assertTrue(out1 != 0.0)
    }
}
