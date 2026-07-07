package com.example.bpmpiano

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * 简易钢琴音合成器：多次谐波叠加 + 指数衰减包络，用来模拟钢琴音色。
 * 每个实例代表一个固定音高，通过 MODE_STATIC 的 AudioTrack 预先渲染好波形，
 * 调用 trigger() 即可从头重新播放，从而实现快速重复触发（连续音符）。
 */
class PianoVoice(frequency: Float, sampleRate: Int = 44100, duration: Double = 1.6) {

    private val track: AudioTrack

    init {
        val totalSamples = (sampleRate * duration).toInt()
        val samples = ShortArray(totalSamples)

        // 谐波幅度权重，近似钢琴的泛音结构
        val harmonics = doubleArrayOf(1.0, 0.55, 0.30, 0.18, 0.10, 0.06, 0.04)
        val decayRate = 3.2 // 衰减速度，数值越大衰减越快

        for (i in 0 until totalSamples) {
            val t = i.toDouble() / sampleRate
            val envelope = exp(-t * decayRate)
            var sample = 0.0
            for ((h, amp) in harmonics.withIndex()) {
                val harmonicFreq = frequency * (h + 1)
                sample += amp * sin(2.0 * PI * harmonicFreq * t)
            }
            // 前 5ms 淡入，避免每次触发产生咔哒声
            val attack = if (t < 0.005) t / 0.005 else 1.0
            val value = sample * envelope * attack * 0.6 // 0.6 防止削波
            samples[i] = (value * Short.MAX_VALUE).toInt().coerceIn(-32768, 32767).toShort()
        }

        val bufferSizeBytes = totalSamples * 2

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        val audioFormat = AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(sampleRate)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()

        track = AudioTrack(
            audioAttributes,
            audioFormat,
            bufferSizeBytes,
            AudioTrack.MODE_STATIC,
            AudioManager.AUDIO_SESSION_ID_GENERATE
        )
        track.write(samples, 0, samples.size)
    }

    /** 从头重新触发播放，实现连续快速的音符 */
    fun trigger() {
        try {
            track.stop()
            track.setPlaybackHeadPosition(0)
            track.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun release() {
        track.release()
    }
}
