package com.hayashihideo.himegoto.compactchipgroup

import android.content.Context
import android.graphics.Point
import android.support.annotation.AttrRes
import android.support.annotation.Px
import android.support.annotation.StyleRes
import android.support.design.chip.Chip
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import com.hayashihideo.himegoto.R
import com.hayashihideo.himegoto.compactchipgroup.internal.ChipsLayoutManager
import com.hayashihideo.himegoto.compactchipgroup.internal.ChipsManager
import com.hayashihideo.himegoto.compactchipgroup.internal.ChipsPool
import com.hayashihideo.himegoto.compactchipgroup.internal.RoughLayout

class CompactChipGroup(context: Context,
                       attrs: AttributeSet?,
                       @AttrRes defStyleAttr: Int,
                       @StyleRes defStyleRes: Int)
    : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    var maxLines
        set(value) { roughLayout.maxLines = value }
        get() = roughLayout.maxLines

    @Px var chipsHorizontalGap = 0
    @Px var chipsVerticalGap = 0
    @Px var restCountBadgeMarginStart = 0

    // use these attributes as a substitute for the padding
    // because it will cut off a chip's shadow.
    @Px var chipsMarginStart = 0
    @Px var chipsMarginEnd = 0
    @Px var chipsMarginTop = 0
    @Px var chipsMarginBottom = 0

    private val chipMeasure = ChipMeasure(owner = this)

    private val chipHolders = mutableListOf<ChipHolder>()

    private val roughLayout = RoughLayout()

    private val layoutManager: ChipsLayoutManager =
            ChipsManager(owner = this, pool = ChipsPool(context))

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
            layoutManager.layout(roughLayout, layoutLeft, layoutTop)
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
                it.layoutParams.width = chipMeasure.measureWidth(it.getLabel())
                it.layoutParams.height = chipHeight
            }

            val width = MeasureSpec.getSize(widthMeasureSpec)
            val availableWidth = width - paddingStart - paddingEnd - chipsMarginStart - chipsMarginEnd
            val restChips = chipHolders.toMutableList()
            var usedChips = roughLayout.layout(availableWidth, chipHolders)
            restChips.removeAll(usedChips)

//        if (restChips.isNotEmpty()) {
//            usedChips = roughLayout.deleteLastLine()
//            restChips.addAll(0, usedChips)
//            usedChips = roughLayout.layoutInNewLine(
//                    availableWidth - restCountBadge.measuredWidth, chipsHorizontalGap, restChips)
//            restChips.removeAll(usedChips)
//            restCountBadge.setRestCount(restChips.size)
//            restCountBadge.measure(
//                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
//                    MeasureSpec.makeMeasureSpec(chipHeight, MeasureSpec.EXACTLY))
//        }

            val height = paddingTop + paddingBottom + chipsMarginTop + chipsMarginBottom +
                    roughLayout.lineCount * chipHeight + (roughLayout.lineCount - 1) * chipsVerticalGap

            cachedMeasuredSize.set(width, height)
            cachedMeasureSpecs.set(widthMeasureSpec, heightMeasureSpec)
            measureCached = true
            setMeasuredDimension(width, height)
        } else {
            setMeasuredDimension(cachedMeasuredSize.x, cachedMeasuredSize.y)
        }
    }

    override fun requestLayout() {
        measureCached = false
        layoutRequested = true
        super.requestLayout()
    }
}
