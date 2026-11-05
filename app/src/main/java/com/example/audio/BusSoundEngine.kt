package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.SoundPool
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sin

/**
 * High-performance audio engine for Philippine Bus Simulator matching ETS2:
 * 1. SoundPool for zero-latency Pinoy dual-tone air horn, air brake "pssht" release, turn signals, footstep SFX, and thunder crack.
 * 2. Real-time synthesized PCM AudioTrack for continuous diesel engine sound with dynamic RPM pitch.
 * 3. Terminal ambient crowd chatter loop.
 */
class BusSoundEngine(private val context: Context) {

    private val audioScope = CoroutineScope(Dispatchers.Default)

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(10)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private var hornSoundId: Int = 0
    private var airBrakeSoundId: Int = 0
    private var clickerSoundId: Int = 0
    private var pttSoundId: Int = 0
    private var footstepSoundId: Int = 0
    private var thunderSoundId: Int = 0
    private var crowdChatterSoundId: Int = 0
    private var crowdStreamId: Int = 0

    // Diesel Engine AudioTrack Synthesizer
    private var engineAudioTrack: AudioTrack? = null
    private var engineLoopJob: Job? = null
    @Volatile private var isEngineAudioRunning = false
    @Volatile private var currentRpm: Int = 850
    @Volatile private var isAccelerating: Boolean = false
    @Volatile private var engineVolume: Float = 0.65f

    init {
        generateAndLoadSounds()
        initializeEngineAudioTrack()
    }

    private fun generateAndLoadSounds() {
        try {
            val cacheDir = context.cacheDir

            // 1. Dual-tone Philippine Pneumatic Air Horn WAV
            val hornFile = File(cacheDir, "bus_air_horn.wav")
            createPneumaticHornWav(hornFile)
            hornSoundId = soundPool.load(hornFile.absolutePath, 1)

            // 2. Air Brake Pressure Release "Pssht" WAV
            val airBrakeFile = File(cacheDir, "air_brake_pssht.wav")
            createAirBrakeWav(airBrakeFile)
            airBrakeSoundId = soundPool.load(airBrakeFile.absolutePath, 1)

            // 3. Relay Clicker for Turn Signal
            val clickerFile = File(cacheDir, "turn_clicker.wav")
            createRelayClickWav(clickerFile)
            clickerSoundId = soundPool.load(clickerFile.absolutePath, 1)

            // 4. HAM Radio PTT chirp
            val pttFile = File(cacheDir, "ptt_chirp.wav")
            createPttChirpWav(pttFile)
            pttSoundId = soundPool.load(pttFile.absolutePath, 1)

            // 5. Pedestrian Walking Footstep SFX
            val footstepFile = File(cacheDir, "pedestrian_footstep.wav")
            createFootstepWav(footstepFile)
            footstepSoundId = soundPool.load(footstepFile.absolutePath, 1)

            // 6. Thunder Crack for monsoon storms
            val thunderFile = File(cacheDir, "thunder_crack.wav")
            createThunderWav(thunderFile)
            thunderSoundId = soundPool.load(thunderFile.absolutePath, 1)

            // 7. Looping Terminal Ambient Crowd Chatter
            val crowdFile = File(cacheDir, "terminal_crowd_loop.wav")
            createCrowdChatterWav(crowdFile)
            crowdChatterSoundId = soundPool.load(crowdFile.absolutePath, 1)
        } catch (_: Exception) {}
    }

    private fun initializeEngineAudioTrack() {
        val sampleRate = 22050
        val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val bufferSize = (minBufferSize * 2).coerceAtLeast(4096)

        try {
            engineAudioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
        } catch (_: Exception) {}
    }

    fun startEngineAudio() {
        if (isEngineAudioRunning) return
        isEngineAudioRunning = true
        engineAudioTrack?.play()

        engineLoopJob = audioScope.launch {
            val sampleRate = 22050
            val buffer = ShortArray(1024)
            var phase = 0.0

            while (isActive && isEngineAudioRunning) {
                val baseFreq = 32.0 + (currentRpm / 2500.0) * 100.0
                val loadHarmonic = if (isAccelerating) 1.6 else 1.0

                for (i in buffer.indices) {
                    phase += (2.0 * Math.PI * baseFreq) / sampleRate
                    if (phase > 2.0 * Math.PI) phase -= 2.0 * Math.PI

                    val sample = (
                        0.55 * sin(phase) +
                        0.30 * sin(phase * 2.0 * loadHarmonic) +
                        0.15 * sin(phase * 4.0)
                    ) * engineVolume * 0.45 * Short.MAX_VALUE

                    buffer[i] = sample.toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                engineAudioTrack?.write(buffer, 0, buffer.size)
            }
        }
    }

    fun stopEngineAudio() {
        isEngineAudioRunning = false
        engineLoopJob?.cancel()
        try {
            engineAudioTrack?.pause()
            engineAudioTrack?.flush()
        } catch (_: Exception) {}
    }

    fun updateEngineState(rpm: Int, isRunning: Boolean, accelerating: Boolean) {
        currentRpm = rpm.coerceIn(600, 2600)
        isAccelerating = accelerating
        if (isRunning && !isEngineAudioRunning) {
            startEngineAudio()
        } else if (!isRunning && isEngineAudioRunning) {
            stopEngineAudio()
        }
    }

    fun updateTerminalCrowdAudio(isInTerminal: Boolean, volumeFactor: Float) {
        if (isInTerminal && crowdChatterSoundId != 0) {
            val vol = (volumeFactor * 0.7f).coerceIn(0.1f, 0.8f)
            if (crowdStreamId == 0) {
                crowdStreamId = soundPool.play(crowdChatterSoundId, vol, vol, 1, -1, 1.0f)
            } else {
                soundPool.setVolume(crowdStreamId, vol, vol)
            }
        } else if (crowdStreamId != 0) {
            soundPool.stop(crowdStreamId)
            crowdStreamId = 0
        }
    }

    fun playAirHorn() {
        if (hornSoundId != 0) {
            soundPool.play(hornSoundId, 1.0f, 1.0f, 1, 0, 1.0f)
        }
    }

    fun playAirBrakeRelease() {
        if (airBrakeSoundId != 0) {
            soundPool.play(airBrakeSoundId, 0.75f, 0.75f, 1, 0, 1.0f)
        }
    }

    fun playTurnSignalClick() {
        if (clickerSoundId != 0) {
            soundPool.play(clickerSoundId, 0.4f, 0.4f, 1, 0, 1.0f)
        }
    }

    fun playFootstep() {
        if (footstepSoundId != 0) {
            // Slight pitch randomization (0.92x to 1.08x)
            val pitch = 0.92f + (Math.random().toFloat() * 0.16f)
            soundPool.play(footstepSoundId, 0.35f, 0.35f, 1, 0, pitch)
        }
    }

    fun playThunderCrack() {
        if (thunderSoundId != 0) {
            soundPool.play(thunderSoundId, 0.85f, 0.85f, 2, 0, 1.0f)
        }
    }

    fun playPttChirp() {
        if (pttSoundId != 0) {
            soundPool.play(pttSoundId, 0.6f, 0.6f, 1, 0, 1.0f)
        }
    }

    fun release() {
        stopEngineAudio()
        if (crowdStreamId != 0) {
            soundPool.stop(crowdStreamId)
        }
        try {
            engineAudioTrack?.release()
            soundPool.release()
        } catch (_: Exception) {}
    }

    // Sound generation helpers
    private fun createPneumaticHornWav(file: File) {
        val sampleRate = 22050
        val durationSec = 0.55
        val numSamples = (sampleRate * durationSec).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val envelope = if (t < 0.05) t / 0.05 else if (t > 0.45) (0.55 - t) / 0.1 else 1.0
            val wave = 0.5 * sin(2.0 * Math.PI * 370.0 * t) + 0.5 * sin(2.0 * Math.PI * 440.0 * t)
            samples[i] = (wave * envelope * 0.8 * Short.MAX_VALUE).toInt().toShort()
        }
        writeWav(file, samples, sampleRate)
    }

    private fun createAirBrakeWav(file: File) {
        val sampleRate = 22050
        val durationSec = 0.40
        val numSamples = (sampleRate * durationSec).toInt()
        val samples = ShortArray(numSamples)
        var lastVal = 0.0
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val envelope = Math.exp(-t * 8.0)
            val whiteNoise = (Math.random() * 2.0 - 1.0)
            val filtered = (whiteNoise - lastVal) * 0.7
            lastVal = whiteNoise
            samples[i] = (filtered * envelope * 0.75 * Short.MAX_VALUE).toInt().toShort()
        }
        writeWav(file, samples, sampleRate)
    }

    private fun createFootstepWav(file: File) {
        val sampleRate = 22050
        val durationSec = 0.08
        val numSamples = (sampleRate * durationSec).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val decay = Math.exp(-t * 80.0)
            val step = sin(2.0 * Math.PI * 180.0 * t) * decay * (Math.random() * 0.4 + 0.8)
            samples[i] = (step * 0.45 * Short.MAX_VALUE).toInt().toShort()
        }
        writeWav(file, samples, sampleRate)
    }

    private fun createThunderWav(file: File) {
        val sampleRate = 22050
        val durationSec = 1.2
        val numSamples = (sampleRate * durationSec).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val decay = Math.exp(-t * 2.5)
            val rumble = sin(2.0 * Math.PI * (60.0 - t * 25.0) * t) * decay * (Math.random() * 0.5 + 0.5)
            samples[i] = (rumble * 0.75 * Short.MAX_VALUE).toInt().toShort()
        }
        writeWav(file, samples, sampleRate)
    }

    private fun createCrowdChatterWav(file: File) {
        val sampleRate = 22050
        val durationSec = 2.0
        val numSamples = (sampleRate * durationSec).toInt()
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val chatter = (
                sin(2.0 * Math.PI * 220.0 * t) * 0.2 +
                sin(2.0 * Math.PI * 340.0 * t) * 0.2 +
                (Math.random() * 0.3)
            )
            samples[i] = (chatter * 0.35 * Short.MAX_VALUE).toInt().toShort()
        }
        writeWav(file, samples, sampleRate)
    }

    private fun createRelayClickWav(file: File) {
        val sampleRate = 22050
        val numSamples = 800
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val decay = Math.exp(-t * 200.0)
            val click = sin(2.0 * Math.PI * 1800.0 * t) * decay
            samples[i] = (click * 0.6 * Short.MAX_VALUE).toInt().toShort()
        }
        writeWav(file, samples, sampleRate)
    }

    private fun createPttChirpWav(file: File) {
        val sampleRate = 22050
        val numSamples = 1200
        val samples = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val freq = 800.0 + (t / 0.05) * 600.0
            val wave = sin(2.0 * Math.PI * freq * t)
            samples[i] = (wave * 0.5 * Short.MAX_VALUE).toInt().toShort()
        }
        writeWav(file, samples, sampleRate)
    }

    private fun writeWav(file: File, samples: ShortArray, sampleRate: Int) {
        val byteData = ByteBuffer.allocate(samples.size * 2).order(ByteOrder.LITTLE_ENDIAN)
        for (s in samples) byteData.putShort(s)
        val audioBytes = byteData.array()

        val totalDataLen = audioBytes.size + 36
        val totalAudioLen = audioBytes.size
        val channels = 1
        val byteRate = sampleRate * channels * 2

        FileOutputStream(file).use { out ->
            val header = ByteArray(44)
            header[0] = 'R'.code.toByte(); header[1] = 'I'.code.toByte(); header[2] = 'F'.code.toByte(); header[3] = 'F'.code.toByte()
            header[4] = (totalDataLen and 0xff).toByte()
            header[5] = ((totalDataLen shr 8) and 0xff).toByte()
            header[6] = ((totalDataLen shr 16) and 0xff).toByte()
            header[7] = ((totalDataLen shr 24) and 0xff).toByte()
            header[8] = 'W'.code.toByte(); header[9] = 'A'.code.toByte(); header[10] = 'V'.code.toByte(); header[11] = 'E'.code.toByte()
            header[12] = 'f'.code.toByte(); header[13] = 'm'.code.toByte(); header[14] = 't'.code.toByte(); header[15] = ' '.code.toByte()
            header[16] = 16; header[17] = 0; header[18] = 0; header[19] = 0
            header[20] = 1; header[21] = 0
            header[22] = channels.toByte(); header[23] = 0
            header[24] = (sampleRate and 0xff).toByte()
            header[25] = ((sampleRate shr 8) and 0xff).toByte()
            header[26] = ((sampleRate shr 16) and 0xff).toByte()
            header[27] = ((sampleRate shr 24) and 0xff).toByte()
            header[28] = (byteRate and 0xff).toByte()
            header[29] = ((byteRate shr 8) and 0xff).toByte()
            header[30] = ((byteRate shr 16) and 0xff).toByte()
            header[31] = ((byteRate shr 24) and 0xff).toByte()
            header[32] = (channels * 2).toByte(); header[33] = 0
            header[34] = 16; header[35] = 0
            header[36] = 'd'.code.toByte(); header[37] = 'a'.code.toByte(); header[38] = 't'.code.toByte(); header[39] = 'a'.code.toByte()
            header[40] = (totalAudioLen and 0xff).toByte()
            header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
            header[42] = ((totalAudioLen shr 16) and 0xff).toByte()
            header[43] = ((totalAudioLen shr 24) and 0xff).toByte()

            out.write(header, 0, 44)
            out.write(audioBytes)
        }
    }
}
