package com.xujiaao.android.bezier.spline.sample.knots

import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.xujiaao.android.bezier.spline.BezierSpline
import com.xujiaao.android.bezier.spline.sample.R
import kotlinx.android.synthetic.main.knots_act.*
import java.util.*

class KnotsActivity : AppCompatActivity() {

    private var count = 4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.knots_act)
        initComponents()
    }

    private fun initComponents() {
        val inflater = LayoutInflater.from(this)
        val random = Random()
        val curveDrawable = CurveDrawable()

        knots.adapter = object : KnotsView.Adapter() {
            override val count: Int get() = this@KnotsActivity.count
            override fun getView(position: Int, view: View?, parent: ViewGroup): View =
                view ?: inflater.inflate(R.layout.knot, parent, false).apply {
                    val layoutParams = layoutParams as KnotsView.LayoutParams
                    layoutParams.centerX = random.nextFloat()
                    layoutParams.centerY = random.nextFloat()
                }
        }

        ViewCompat.setBackground(knots, curveDrawable)
        knots.onChildPositionChangeListener = {
            curveDrawable.invalidateSelf()
        }

        more.setOnClickListener {
            count = Math.min(count + 1, 10)
            knots.adapter?.notifyDataSetChanged()
        }

        less.setOnClickListener {
            count = Math.max(count - 1, 2)
            knots.adapter?.notifyDataSetChanged()
        }
    }

    private inner class CurveDrawable : Drawable() {

        private var bezierSpline: BezierSpline? = null
        private val path = Path()
        private val paint = Paint().apply {
            isAntiAlias = true
            color = ContextCompat.getColor(this@KnotsActivity,
                R.color.colorAccent
            )
            style = Paint.Style.STROKE
            strokeWidth = resources.displayMetrics.density * 2
        }

        override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
        override fun setAlpha(alpha: Int) = Unit
        override fun setColorFilter(colorFilter: ColorFilter?) = Unit
        override fun draw(canvas: Canvas) {
            val count = knots.childCount
            var spline = bezierSpline
            if (spline == null || spline.knots() != count) {
                spline = BezierSpline(count)
                bezierSpline = spline
            }

            for (index in 0 until count) {
                val child = knots.getChildAt(index)
                spline.set(index, child.left + child.width / 2F, child.top + child.height / 2F)
            }

            spline.applyToPath(path)
            canvas.drawPath(path, paint)
        }
    }
}