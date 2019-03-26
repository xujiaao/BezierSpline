package com.xujiaao.android.bezier.spline.sample.wave

import android.content.res.Resources
import android.graphics.*
import com.xujiaao.android.bezier.spline.BezierSplineDrawable

class WaveDrawable(resources: Resources) : BezierSplineDrawable(KNOTS) {

    companion object {
        private const val WAVES = 2
        private const val KNOTS = WAVES * 4 * 8 + 1 // 8 segments per 90 degrees
        private const val SPLINES = 5
        private const val MIN_ALPHA = 0x55
        private const val MIN_STOKE_WIDTH = 1
        private const val MIN_AMPLITUDE = .03F
        private const val MAX_AMPLITUDE = 1F
        private const val SPLINES_DECREMENT = .3F
        private const val DEFAULT_COLOR_START = Color.MAGENTA
        private const val DEFAULT_COLOR_END = Color.CYAN
        private const val DEFAULT_STOKE_WIDTH_DIP = 1F
    }

    private val extrasPath = Path()
    private val extrasPaint = Paint()
    private val extrasMatrix = Matrix()

    init {
        setStrokeColors(DEFAULT_COLOR_START, DEFAULT_COLOR_END)
        setStrokeWidth(DEFAULT_STOKE_WIDTH_DIP * resources.displayMetrics.density)
    }

    var amplitude = 0F
        set(value) {
            field = value
            invalidateSelf()
        }

    override fun draw(canvas: Canvas, mainPath: Path, mainPaint: Paint) {
        // draw extra waves
        val mainAlpha = mainPaint.alpha
        val mainStrokeWidth = mainPaint.strokeWidth
        extrasPaint.set(mainPaint)

        for (index in 1 until SPLINES) {
            val scale = 1f - SPLINES_DECREMENT * index

            extrasMatrix.setScale(1f, scale)
            mainPath.transform(extrasMatrix, extrasPath)

            extrasPaint.alpha = Math.max((scale * mainAlpha).toInt(), MIN_ALPHA)
            extrasPaint.strokeWidth = Math.max(scale * mainStrokeWidth, MIN_STOKE_WIDTH.toFloat())

            canvas.drawPath(extrasPath, extrasPaint)
        }

        // draw main wave
        super.draw(canvas, mainPath, mainPaint)
    }

    override fun getInterpolation(progress: Float, offset: Float): Float {
        // y = amplitude * (1 - (2x - 1) ^ 2) * sin(WAVES * 360 * (x - delta))
        val normalizedAmplitude = MIN_AMPLITUDE + (MAX_AMPLITUDE - MIN_AMPLITUDE) * amplitude
        val angle = WAVES * 360.0 * (progress - offset)
        val sine = Math.sin(Math.toRadians(angle)).toFloat()
        val parabola = 1f - (2F * progress - 1F) * (2F * progress - 1F)
        return normalizedAmplitude * parabola * sine
    }
}