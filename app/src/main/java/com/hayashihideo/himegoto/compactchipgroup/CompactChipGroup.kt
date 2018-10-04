package com.hayashihideo.himegoto.compactchipgroup

import android.content.Context
import android.graphics.Point
import android.support.annotation.AttrRes
import android.support.annotation.StyleRes
import android.support.design.chip.Chip
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.hayashihideo.himegoto.R
import com.hayashihideo.himegoto.compactchipgroup.internal.ChipsLayoutManager
import com.hayashihideo.himegoto.compactchipgroup.internal.ChipsManager
import com.hayashihideo.himegoto.compactchipgroup.internal.ChipsPool

class CompactChipGroup(context: Context,
                       attrs: AttributeSet?,
                       @AttrRes defStyleAttr: Int,
                       @StyleRes defStyleRes: Int)
    : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    var maxLines
        set(value) { layoutManager.maxLines = value }
        get() = layoutManager.maxLines

    var horizontalGap
        get() = layoutManager.horizontalGap
        set(value) { layoutManager.horizontalGap = value }

    var verticalGap
        get() = layoutManager.verticalGap
        set(value) { layoutManager.verticalGap = value }

    var restCountBadgeMarginStart = 0

    // use these attributes as a substitute for the padding
    // because it will cut off a chip's shadow.
    var chipsMarginStart = 0
    var chipsMarginEnd = 0
    var chipsMarginTop = 0
    var chipsMarginBottom = 0

    private val chipMeasure = ChipMeasure(owner = this)
    private val chipHolders = mutableListOf<ChipHolder>()
    private val layoutManager: ChipsLayoutManager = ChipsManager(owner = this, pool = ChipsPool(context))
    private val restCountBadge = inflateRestCountBadge()
    private var layoutRequested = false

    // Use in onMeasure()
    private var measureCached = false
    private val cachedMeasureSpecs = Point(-1, -1)
    private val cachedMeasuredSize = Point()

    constructor(context: Context,
                attrs: AttributeSet?,
                @AttrRes defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context,
                attrs: AttributeSet?) : this(context, attrs, 0, 0)

    constructor(context: Context) : this(context, null, 0, 0)

    init {
        addView(restCountBadge)
    }

    fun setChipHolders(holders: List<ChipHolder>) {
        chipHolders.clear()
        chipHolders.addAll(holders)
        onDataSetChanged()
    }

    private fun inflateRestCountBadge(): TextView = LayoutInflater.from(context)
                    .inflate(R.layout.alt_chip_group_rest_count_badge, this, false) as TextView

    private fun onDataSetChanged() {
        requestLayout()
    }

    internal fun addChipInLayout(chip: Chip, params: ViewGroup.LayoutParams, preventRequestLayout: Boolean)
            = addViewInLayout(chip, -1, params, preventRequestLayout)

    internal fun setChipsPool(pool: ChipsPool) {
        layoutManager.chipsPool = pool
    }

    private class ChipMeasure(owner: CompactChipGroup) {

        private var isDirty = true

        private val chip = LayoutInflater.from(owner.context)
                .inflate(R.layout.compact_chip_group_default_chip, owner, false) as Chip

        fun measureHeight(): Int {
            measure()
            return chip.measuredHeight
        }

        fun measureWidth(): Int {
            measure()
            return chip.measuredWidth
        }

        fun measureWidth(text: String): Int {
            setText(text)
            return measureWidth()
        }

        fun setText(text: String) {
            chip.text = text
            isDirty = true
        }

        private fun measure() {
            if (isDirty) {
                isDirty = false
                chip.measure(
                        MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                        MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
            }
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (changed || layoutRequested) {
            layoutRequested = false
            val layoutLeft = paddingStart + chipsMarginStart
            val layoutTop = paddingTop + chipsMarginTop
            layoutManager.layout(layoutLeft, layoutTop)

            val laidOutChipCount = layoutManager.laidOutChipCount
            if (0 < laidOutChipCount && laidOutChipCount < chipHolders.size) {
                restCountBadge.visibility = View.VISIBLE
                val lastChip = layoutManager.getChipForPosition(layoutManager.lastPosition)
                restCountBadge.layout(lastChip.right + restCountBadgeMarginStart, lastChip.top,
                        lastChip.right + restCountBadgeMarginStart + restCountBadge.measuredWidth,
                        lastChip.top + restCountBadge.measuredHeight)
            } else {
                restCountBadge.visibility = View.GONE
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY ||
//                MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.AT_MOST) {
//            throw IllegalArgumentException()
//        }

        if ((layoutRequested && !measureCached) ||
                cachedMeasureSpecs.x != widthMeasureSpec ||
                cachedMeasureSpecs.y != heightMeasureSpec) {

            val chipHeight = chipMeasure.measureHeight()
            chipHolders.forEach {
                it.layoutParams.width = chipMeasure.measureWidth(it.label)
                it.layoutParams.height = chipHeight
            }

            val width = MeasureSpec.getSize(widthMeasureSpec)
            val availableWidth = width - paddingStart - paddingEnd - chipsMarginStart - chipsMarginEnd

            // measure the max width of restCountBadge
            setRestCount(chipHolders.size)
            restCountBadge.measure(
                    MeasureSpec.makeMeasureSpec(availableWidth, MeasureSpec.AT_MOST),
                    MeasureSpec.makeMeasureSpec(chipHeight, MeasureSpec.EXACTLY))

            val maxBadgeBounds = restCountBadgeMarginStart + restCountBadge.measuredWidth
            layoutManager.layoutRoughly(availableWidth, maxBadgeBounds, chipHolders)

            val laidOutChipCount = layoutManager.laidOutChipCount
            if (0 < laidOutChipCount && laidOutChipCount < chipHolders.size) {
                // measure actual width of restCountBadge
                setRestCount(chipHolders.size - laidOutChipCount)
                restCountBadge.measure(
                        MeasureSpec.makeMeasureSpec(availableWidth, MeasureSpec.AT_MOST),
                        MeasureSpec.makeMeasureSpec(chipHeight, MeasureSpec.EXACTLY))
            }

            val lines = layoutManager.lineCount
            val height = paddingTop + paddingBottom + chipsMarginTop + chipsMarginBottom +
                    lines * chipHeight + (lines - 1) * verticalGap

            cachedMeasuredSize.set(width, height)
            cachedMeasureSpecs.set(widthMeasureSpec, heightMeasureSpec)
            measureCached = true
            setMeasuredDimension(width, height)
        } else {
            setMeasuredDimension(cachedMeasuredSize.x, cachedMeasuredSize.y)
        }
    }

    private fun setRestCount(count: Int) {
        val text = "+$count"
        restCountBadge.text = text
    }

    override fun requestLayout() {
        measureCached = false
        layoutRequested = true
        super.requestLayout()
    }
}
