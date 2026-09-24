package com.outsidegem.fartapp

import android.content.Context
import android.media.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

class FartAudioEngine(private val context: Context) {
    private var isPlaying = AtomicBoolean(false)
    private var playThread: Thread? = null

    fun play(soundResId: Int) {
        if (!isPlaying.compareAndSet(false, true)) return
        playThread = thread {
            try { playInternal(soundResId) } finally { isPlaying.set(false) }
        }
    }

    private fun playInternal(soundResId: Int) {
        val extractor = MediaExtractor()
        val afd = context.resources.openRawResourceFd(soundResId)
        extractor.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
        afd.close()

        var format: MediaFormat? = null
        for (i in 0 until extractor.trackCount) {
            val f = extractor.getTrackFormat(i)
            if (f.getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true) {
                extractor.selectTrack(i)
                format = f
                break
            }
        }
        if (format == null) { extractor.release(); return }

        val sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
        val channels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
        val mime = format.getString(MediaFormat.KEY_MIME) ?: return
        val dsp = LowPassFilter(sampleRate, 400.0, 0.707)
        val channelConfig = if (channels == 1) AudioFormat.CHANNEL_OUT_MONO else AudioFormat.CHANNEL_OUT_STEREO
        val minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, AudioFormat.ENCODING_PCM_16BIT)

        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
            .setAudioFormat(AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(sampleRate).setChannelMask(channelConfig).build())
            .setBufferSizeInBytes(minBufferSize * 4)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
            .setAcceptsDelayedFocusGain(false).setOnAudioFocusChangeListener { }.build()
            
        audioManager.requestAudioFocus(audioFocusRequest)
        audioTrack.play()

        val codec = MediaCodec.createDecoderByType(mime)
        codec.configure(format, null, null, 0)
        codec.start()

        val info = MediaCodec.BufferInfo()
        var isEOS = false
        
        while (!isEOS && isPlaying.get()) {
            val inIndex = codec.dequeueInputBuffer(10000L)
            if (inIndex >= 0) {
                val buffer = codec.getInputBuffer(inIndex)
                val sampleSize = buffer?.let { extractor.readSampleData(it, 0) } ?: -1
                if (sampleSize < 0 || extractor.sampleTime > 3_000_000L) {
                    codec.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
                } else {
                    codec.queueInputBuffer(inIndex, 0, sampleSize, extractor.sampleTime, 0)
                    extractor.advance()
                }
            }
            val outIndex = codec.dequeueOutputBuffer(info, 10000L)
            if (outIndex >= 0) {
                if ((info.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) isEOS = true
                val outBuffer = codec.getOutputBuffer(outIndex)
                if (outBuffer != null && info.size > 0) {
                    outBuffer.position(info.offset)
                    outBuffer.limit(info.offset + info.size)
                    val shortBuffer = outBuffer.asShortBuffer()
                    val pcmData = ShortArray(shortBuffer.remaining())
                    shortBuffer.get(pcmData)
                    dsp.processBuffer(pcmData, channels)
                    audioTrack.write(pcmData, 0, pcmData.size)
                }
                codec.releaseOutputBuffer(outIndex, false)
            }
        }
        codec.stop(); codec.release(); extractor.release(); audioTrack.stop(); audioTrack.release()
        audioManager.abandonAudioFocusRequest(audioFocusRequest)
    }
    
    fun stop() { isPlaying.set(false); playThread?.interrupt(); playThread = null }
}
