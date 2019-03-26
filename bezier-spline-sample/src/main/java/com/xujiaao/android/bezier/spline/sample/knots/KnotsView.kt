package com.xujiaao.android.bezier.spline.sample.knots

import android.annotation.SuppressLint
import android.content.Context
import android.database.DataSetObservable
import android.database.DataSetObserver
import android.support.v4.widget.ViewDragHelper
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT

class KnotsView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {

    private val dragHelper = ViewDragHelper.create(this, Callback())
    private val dataSetObserver = object : DataSetObserver() {
        override fun onChanged() = onDataSetChanged()
        override fun onInvalidated() = onDataSetChanged()
    }

    var onChildPositionChangeListener: (KnotsView.() -> Unit)? = null
    var adapter: Adapter? = null
        set(adapter) {
            field?.unregisterDataSetObserver(dataSetObserver)
            field = adapter

            adapter?.registerDataSetObserver(dataSetObserver)
            onDataSetChanged()
        }

    private fun onDataSetChanged() {
        adapter?.let { adapter ->
            for (position in 0 until adapter.count) {
                val scrap = getChildAt(position)
                val child = adapter.getView(position, scrap, this)
                if (child != scrap) {
                    addViewInLayout(
                        child, position,
                        child.layoutParams ?: generateDefaultLayoutParams()
                    )
                }
            }
        }

        val toBeRemoved = adapter?.count ?: 0
        if (childCount > toBeRemoved) {
            removeViewsInLayout(toBeRemoved, childCount - toBeRemoved)
        }

        requestLayout()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean =
        dragHelper.processTouchEvent(event).run { true }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean =
        dragHelper.shouldInterceptTouchEvent(event)

    override fun checkLayoutParams(layoutParams: ViewGroup.LayoutParams?): Boolean =
        layoutParams is LayoutParams

    override fun generateDefaultLayoutParams(): LayoutParams =
        LayoutParams(
            WRAP_CONTENT,
            WRAP_CONTENT
        )

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams =
        LayoutParams(context, attrs)

    override fun generateLayoutParams(layoutParams: ViewGroup.LayoutParams): LayoutParams =
        LayoutParams(layoutParams)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        for (index in 0 until childCount) {
            val child = getChildAt(index)
            child.measure(
                getChildMeasureSpec(MeasureSpec.UNSPECIFIED, 0, child.layoutParams.width),
                getChildMeasureSpec(MeasureSpec.UNSPECIFIED, 0, child.layoutParams.height)
            )
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val pw = width - paddingLeft - paddingRight
        val ph = height - paddingTop - paddingBottom
        for (index in 0 until childCount) {
            val child = getChildAt(index)
            val childLayoutParams = child.layoutParams as LayoutParams
            val cw = child.measuredWidth
            val ch = child.measuredHeight
            val cl = paddingLeft + ((pw - cw) * childLayoutParams.centerX).toInt()
            val ct = paddingTop + ((ph - ch) * childLayoutParams.centerY).toInt()
            child.layout(cl, ct, cl + cw, ct + ch)
        }

        onChildPositionChangeListener?.invoke(this)
    }

    private inner class Callback : ViewDragHelper.Callback() {

        override fun tryCaptureView(child: View, pointerId: Int): Boolean = true

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            val parent = this@KnotsView
            return Math.min(
                Math.max(left, parent.paddingLeft),
                parent.width - parent.paddingRight - child.width
            )
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            val parent = this@KnotsView
            return Math.min(
                Math.max(top, parent.paddingTop),
                parent.height - parent.paddingBottom - child.height
            )
        }

        override fun onViewPositionChanged(child: View, cl: Int, ct: Int, dx: Int, dy: Int) {
            val parent = this@KnotsView
            val pw = parent.width - parent.paddingLeft - parent.paddingRight - child.width
            val ph = parent.height - parent.paddingTop - parent.paddingBottom - child.height
            with(child.layoutParams as LayoutParams) {
                centerX = if (pw > 0) (cl - parent.paddingLeft).toFloat() / pw else .5F
                centerY = if (ph > 0) (ct - parent.paddingTop).toFloat() / ph else .5F
            }

            onChildPositionChangeListener?.invoke(this@KnotsView)
        }
    }

    class LayoutParams : ViewGroup.LayoutParams {

        var centerX = .5F
        var centerY = .5F

        constructor(width: Int, height: Int) : super(width, height)
        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
        constructor(source: ViewGroup.LayoutParams) : super(source)
    }

    abstract class Adapter {

        private val dataSetObservable = DataSetObservable()

        abstract val count: Int
        abstract fun getView(position: Int, view: View?, parent: ViewGroup): View

        fun registerDataSetObserver(observer: DataSetObserver) =
            dataSetObservable.registerObserver(observer)

        fun unregisterDataSetObserver(observer: DataSetObserver) =
            dataSetObservable.unregisterObserver(observer)

        fun notifyDataSetChanged() =
            dataSetObservable.notifyChanged()
    }
}