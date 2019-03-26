package com.xujiaao.android.bezier.spline.sample.wave

import android.os.Bundle
import android.support.v4.view.ViewCompat
import android.support.v7.app.AppCompatActivity
import android.widget.SeekBar
import com.xujiaao.android.bezier.spline.sample.R
import kotlinx.android.synthetic.main.wave_act.*

class WaveActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.wave_act)
        initComponents()
    }

    private fun initComponents() {
        val waveDrawable = WaveDrawable(resources)
        waveDrawable.amplitude = seekBar.amplitude
        waveDrawable.start()

        ViewCompat.setBackground(wave, waveDrawable)
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(seekBar: SeekBar) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar) = Unit
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                waveDrawable.amplitude = progress.toFloat() / seekBar.max
            }
        })
    }

    private val SeekBar.amplitude get() = progress.toFloat() / max
}