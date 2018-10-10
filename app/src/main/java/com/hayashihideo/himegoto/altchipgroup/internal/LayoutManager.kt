package com.hayashihideo.himegoto.altchipgroup.internal

import android.graphics.Point
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.MeasureSpec
import android.widget.TextView
import com.hayashihideo.himegoto.R
import com.hayashihideo.himegoto.altchipgroup.AltChipGroup

internal class LayoutManager(private val owner: AltChipGroup) {

    var layoutWithinBounds
        set(value) { layoutSpec.layoutWithinBounds = value }
        get() = layoutSpec.layoutWithinBounds

    var maxLines
        set(value) { layoutSpec.maxLines = value }
        get() = layoutSpec.maxLines

    var horizontalGap
        get() = layoutSpec.horizontalGap
        set(value) { layoutSpec.horizontalGap = value }

    var verticalGap
        get() = layoutSpec.verticalGap
        set(value) { layoutSpec.verticalGap = value }

    var restCountBadgeMarginStart = 0
    // use these attributes as a substitute for the padding
    // because it will cut off a chip's shadow.
    var chipsMarginStart = 0
    var chipsMarginEnd = 0
    var chipsMarginTop = 0
    var chipsMarginBottom = 0

    private val roughLayout = RoughLayout()
    private val layoutBounds = Point()
    private val layoutSpec = LayoutSpec()
    private val layoutMethod = CrammingLayoutMethod()
    
    private val badge = inflateRestCountBadge()
    private var showBadge = false

    init {
        owner.addView(badge)
    }

    fun onLayout(left: Int, top: Int, right: Int, bottom: Int) {
        val layoutLeft = owner.paddingStart + chipsMarginStart
        val layoutTop = owner.paddingTop + chipsMarginTop
        var chipLeft = 0
        var chipTop = 0
        roughLayout.forEachPosition { line, position, lineChanged ->
            val chip = owner.viewStore.getChipForPosition(position)
            if (lineChanged) {
                chipLeft = layoutLeft
                chipTop = layoutTop + line * (chip.measuredHeight + verticalGap)
            }
            chip.layout(chipLeft, chipTop,
                    chipLeft + chip.measuredWidth, chipTop + chip.measuredHeight)
            chipLeft += chip.width + horizontalGap
        }

        if (showBadge) {
            val badge = badge
            badge.visibility = View.VISIBLE
            val lastChip = owner.viewStore.getChipForPosition(roughLayout.lastPosition)
            badge.layout(lastChip.right + restCountBadgeMarginStart, lastChip.top,
                    lastChip.right + restCountBadgeMarginStart + badge.measuredWidth,
                    lastChip.top + badge.measuredHeight)
        } else {
            badge.visibility = View.GONE
        }
    }

    fun onMeasure(width: Int, height: Int, widthMode: Int, heightMode: Int) {
        val maxLayoutWidth = if (widthMode == MeasureSpec.UNSPECIFIED) -1 else
            width - owner.paddingStart - owner.paddingEnd - chipsMarginStart - chipsMarginEnd
        val maxLayoutHeight = if (heightMode == MeasureSpec.UNSPECIFIED) -1 else
            height - owner.paddingTop - owner.paddingBottom - chipsMarginTop - chipsMarginBottom

        val maxLayoutWidthSpecified = -1 < maxLayoutWidth
        val maxLayoutHeightSpecified = -1 < maxLayoutHeight
        val chipHeight = owner.chipMeasure.height()

        // chipMeasure the max maxWidth of the badge temporarily
        setRestCount(owner.chipCount())
        badge.measure(
                MeasureSpec.makeMeasureSpec(maxLayoutWidth, MeasureSpec.AT_MOST),
                MeasureSpec.makeMeasureSpec(chipHeight, MeasureSpec.EXACTLY))
        val maxBadgeBounds = restCountBadgeMarginStart + badge.measuredWidth

        // layout roughly
        roughLayout.clear()
        if (maxLayoutWidthSpecified) layoutSpec.maxWidth = maxLayoutWidth else layoutSpec.setMaxWidthAsUnlimited()
        if (maxLayoutHeightSpecified) layoutSpec.maxHeight = maxLayoutHeight else layoutSpec.setMaxHeightAsUnlimited()
        layoutSpec.maxBadgeBounds = maxBadgeBounds
        layoutMethod.invoke(owner.holders(), roughLayout, layoutSpec, owner.chipMeasure, layoutBounds)

        val laidOutChipCount = roughLayout.chipCount
        if (0 < laidOutChipCount && laidOutChipCount < owner.holders().size) {
            showBadge = true
            setRestCount(owner.holders().size - laidOutChipCount)
            // determine actual width of the badge
            badge.measure(
                    MeasureSpec.makeMeasureSpec(maxLayoutWidth, MeasureSpec.AT_MOST),
                    MeasureSpec.makeMeasureSpec(chipHeight, MeasureSpec.EXACTLY))
        } else {
            showBadge = false
        }

        // measure chips
        owner.viewStore.prepareChipsForLayout(roughLayout)
        for (position in 0 until roughLayout.chipCount) {
            val chip = owner.viewStore.getChipForPosition(position)
            val holder = owner.viewStore.getHolderForPosition(position)
            holder.bind(chip)
            owner.chipMeasure.measureChipForHolder(chip, holder)
        }

        var measuredWidth = width
        var measuredHeight = height
        if (widthMode != MeasureSpec.EXACTLY) {
            val needed = layoutBounds.x + owner.paddingStart + owner.paddingEnd + chipsMarginStart + chipsMarginEnd
            measuredWidth = if (widthMode == MeasureSpec.AT_MOST) Math.min(needed, width) else needed
        }

        if (heightMode != MeasureSpec.EXACTLY) {
            val needed = layoutBounds.y + owner.paddingTop + owner.paddingBottom + chipsMarginTop + chipsMarginBottom
            measuredHeight = if (heightMode == MeasureSpec.AT_MOST) Math.min(needed, height) else needed
        }

        if (owner.holders().isNotEmpty() && laidOutChipCount == 0) {
            Log.e(AltChipGroup::class.java.name, "There is not enough space for putting chips.")
        }

        owner.setMeasuredDimensionInternal(measuredWidth, measuredHeight)
    }

    private fun setRestCount(count: Int) {
        val text = "+$count"
        badge.text = text
    }

    private fun inflateRestCountBadge(): TextView = LayoutInflater.from(owner.context)
            .inflate(R.layout.alt_chip_group_rest_count_badge, owner, false) as TextView
}
