package com.hayashihideo.himegoto.altchipgroup

import android.content.Context
import android.graphics.Point
import android.support.annotation.AttrRes
import android.support.annotation.StyleRes
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.hayashihideo.himegoto.altchipgroup.internal.ChipStore
import com.hayashihideo.himegoto.altchipgroup.internal.ChipMeasure
import com.hayashihideo.himegoto.altchipgroup.internal.LayoutManager
import com.hayashihideo.himegoto.altchipgroup.internal.SharedChipPool

class AltChipGroup(context: Context,
       attrs: AttributeSet?,
       @AttrRes defStyleAttr: Int,
       @StyleRes defStyleRes: Int): ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    var layoutWithinBounds: Boolean
        get() = layoutManager.layoutWithinBounds
        set(value) { layoutManager.layoutWithinBounds = value }

    var maxLines: Int
        get() = layoutManager.maxLines
        set(value) { layoutManager.maxLines = value }

    var horizontalGap: Int
        get() = layoutManager.horizontalGap
        set(value) { layoutManager.horizontalGap = value }

    var verticalGap: Int
        get() = layoutManager.verticalGap
        set(value) { layoutManager.verticalGap = value }

    var restCountBadgeMarginStart: Int
        get() = layoutManager.restCountBadgeMarginStart
        set(value) { layoutManager.restCountBadgeMarginStart = value }

    var chipsMarginStart: Int
        get() = layoutManager.chipsMarginStart
        set(value) { layoutManager.chipsMarginStart = value }

    var chipsMarginEnd: Int
        get() = layoutManager.chipsMarginEnd
        set(value) { layoutManager.chipsMarginEnd = value }

    var chipsMarginTop: Int
        get() = layoutManager.chipsMarginTop
        set(value) { layoutManager.chipsMarginTop = value }

    var chipsMarginBottom: Int
        get() = layoutManager.chipsMarginBottom
        set(value) { layoutManager.chipsMarginBottom = value }

    private val cachedMeasureSpecs = Point(-1, -1)
    private val cachedMeasuredSize = Point()
    private var layoutIsFrozen = false
    private var layoutRequested = false
    private var measureCached = false

    private val chipHolders = mutableListOf<ChipHolder>()
    private val layoutManager = LayoutManager(this)

    internal var chipMeasure = ChipMeasure(context, ChipFactory())
    internal val viewStore = ChipStore(this, SharedChipPool(context, ChipFactory()))

    constructor(context: Context,
                attrs: AttributeSet?,
                @AttrRes defStyleAttr: Int): this(context, attrs, defStyleAttr, 0)

    constructor(context: Context,
                attrs: AttributeSet?): this(context, attrs, 0, 0)

    constructor(context: Context): this(context, null, 0, 0)

    init {
        loadAttributes(context, attrs, defStyleAttr, defStyleRes)
    }

    private fun loadAttributes(
            context: Context,
            attrs: AttributeSet?,
            @AttrRes defStyleAttr: Int,
            @StyleRes defStyleRes: Int) {}

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (changed || layoutRequested) {
            layoutRequested = false
            layoutManager.onLayout(left, top, right, bottom)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if ((layoutRequested && !measureCached) ||
                cachedMeasureSpecs.x != widthMeasureSpec ||
                cachedMeasureSpecs.y != heightMeasureSpec) {
            val width = MeasureSpec.getSize(widthMeasureSpec)
            val height = MeasureSpec.getSize(heightMeasureSpec)
            val widthMode = MeasureSpec.getMode(widthMeasureSpec)
            val heightMode = MeasureSpec.getMode(heightMeasureSpec)
            layoutManager.onMeasure(width, height, widthMode, heightMode)
            cachedMeasuredSize.set(measuredWidth, measuredHeight)
            cachedMeasureSpecs.set(widthMeasureSpec, heightMeasureSpec)
            measureCached = true
        } else {
            setMeasuredDimension(cachedMeasuredSize.x, cachedMeasuredSize.y)
        }
    }

    internal fun addViewInLayoutInternal(view: View, index: Int, params: LayoutParams, preventRequestLayout: Boolean) =
            addViewInLayout(view, index, params, preventRequestLayout)

    internal fun setMeasuredDimensionInternal(width: Int, height: Int) = setMeasuredDimension(width, height)

    internal fun ignoreLayoutRequestDuring(process: () -> Unit) {
        layoutIsFrozen = true
        process()
        layoutIsFrozen = false
    }

    override fun requestLayout() {
        if (!layoutIsFrozen) {
            measureCached = false
            layoutRequested = true
            super.requestLayout()
        }
    }

    fun chipCount() = chipHolders.size

    fun holders(): List<ChipHolder> = chipHolders

    fun setChipHolders(holders: List<ChipHolder>) {
        chipHolders.clear()
        chipHolders.addAll(holders)
        requestLayout()
    }

    internal fun setShared(pool: SharedChipPool, measure: ChipMeasure) {
        chipMeasure = measure
        viewStore.setShared(pool)
    }
}
