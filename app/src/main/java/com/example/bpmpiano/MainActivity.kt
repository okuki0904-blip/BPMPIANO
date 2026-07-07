package com.example.bpmpiano

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var bpmEditText: EditText
    private val handler = Handler(Looper.getMainLooper())

    // 五个按钮对应的音高（C大调五声音阶），可自行修改为想要的音符频率（单位 Hz）
    private val noteFrequencies = floatArrayOf(261.63f, 293.66f, 329.63f, 392.00f, 440.00f) // C4 D4 E4 G4 A4
    private val noteNames = arrayOf("C4", "D4", "E4", "G4", "A4")
    private val buttonCount = noteFrequencies.size

    private lateinit var voices: Array<PianoVoice>
    private val runningState = BooleanArray(buttonCount)
    private val runnables = arrayOfNulls<Runnable>(buttonCount)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bpmEditText = findViewById(R.id.editTextBpm)
        bpmEditText.setText("120")

        voices = Array(buttonCount) { i -> PianoVoice(noteFrequencies[i]) }

        val buttonIds = intArrayOf(R.id.button1, R.id.button2, R.id.button3, R.id.button4, R.id.button5)
        for (i in 0 until buttonCount) {
            val btn = findViewById<Button>(buttonIds[i])
            btn.text = noteNames[i]
            btn.setOnClickListener {
                toggle(i, btn)
            }
        }
    }

    /** 读取当前 BPM 输入框的值，非法输入时回退到 120，并限制在合理范围 */
    private fun currentBpm(): Double {
        val text = bpmEditText.text.toString()
        val v = text.toDoubleOrNull() ?: 120.0
        return v.coerceIn(20.0, 300.0)
    }

    /** 点击按钮：开始/停止 对应音符的连续八分音符播放 */
    private fun toggle(index: Int, btn: Button) {
        if (runningState[index]) {
            // 停止播放
            runningState[index] = false
            runnables[index]?.let { handler.removeCallbacks(it) }
            btn.alpha = 1.0f
        } else {
            // 开始播放
            runningState[index] = true
            btn.alpha = 0.55f
            val runnable = object : Runnable {
                override fun run() {
                    if (!runningState[index]) return
                    voices[index].trigger()
                    val bpm = currentBpm()
                    // 八分音符时值 = (60000 / bpm) / 2
                    val intervalMs = (30000.0 / bpm).toLong()
                    handler.postDelayed(this, intervalMs)
                }
            }
            runnables[index] = runnable
            handler.post(runnable)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        for (r in runnables) r?.let { handler.removeCallbacks(it) }
        for (v in voices) v.release()
    }
}
