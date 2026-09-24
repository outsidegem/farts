package com.outsidegem.fartapp

import kotlin.math.cos
import kotlin.math.sin

class LowPassFilter(private val sampleRate: Int, private val cutoffFrequency: Double = 400.0, private val qFactor: Double = 0.707) {
    private var b0=0.0; private var b1=0.0; private var b2=0.0; private var a1=0.0; private var a2=0.0
    private val x1 = DoubleArray(2); private val x2 = DoubleArray(2)
    private val y1 = DoubleArray(2); private val y2 = DoubleArray(2)

    init {
        val w0 = 2.0 * Math.PI * cutoffFrequency / sampleRate
        val alpha = sin(w0) / (2.0 * qFactor)
        val cosW0 = cos(w0)
        val a0 = 1.0 + alpha
        b0 = ((1.0 - cosW0) / 2.0) / a0; b1 = (1.0 - cosW0) / a0; b2 = ((1.0 - cosW0) / 2.0) / a0
        a1 = (-2.0 * cosW0) / a0; a2 = (1.0 - alpha) / a0
    }

    private fun processSample(input: Double, channel: Int): Double {
        val output = b0 * input + b1 * x1[channel] + b2 * x2[channel] - a1 * y1[channel] - a2 * y2[channel]
        x2[channel] = x1[channel]; x1[channel] = input
        y2[channel] = y1[channel]; y1[channel] = output
        return output
    }
    
    fun processBuffer(buffer: ShortArray, channels: Int) {
        var i = 0
        while (i < buffer.size) {
            var c = 0
            while (c < channels) {
                if (i + c < buffer.size) {
                    val input = buffer[i + c].toDouble() / 32768.0
                    var output = processSample(input, c) * 0.8 // 0.8x gain for muffling
                    if (output > 1.0) output = 1.0 else if (output < -1.0) output = -1.0
                    buffer[i + c] = (output * 32767.0).toInt().toShort()
                }
                c++
            }
            i += channels
        }
    }
}
