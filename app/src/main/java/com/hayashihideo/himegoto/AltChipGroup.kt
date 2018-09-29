package com.hayashihideo.himegoto

import android.content.Context
import android.graphics.Canvas
import android.support.annotation.AttrRes
import android.support.annotation.Px
import android.support.annotation.StyleRes
import android.support.design.chip.Chip
import android.util.AttributeSet
import android.util.Log
import android.view.ViewGroup

class AltChipGroup(
        context: Context,
        attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int,
        @StyleRes defStyleRes: Int)
    : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    var maxLines = 1
    @Px var chipsHorizontalSpace = 0
    @Px var chipsVerticalSpace = 0

    // use these attributes as a substitute for the padding
    // because it will cut off a chip's shadow.
    @Px var chipsMarginStart = 0
    @Px var chipsMarginEnd = 0
    @Px var chipsMarginTop = 0
    @Px var chipsMarginBottom = 0

    private val chips = mutableListOf<Chip>()
    private val restCountChip = Chip(context)
    private val roughLayout = RoughLayout()

    constructor(context: Context,
                attrs: AttributeSet?,
                @AttrRes defStyleAttr: Int): this(context, attrs, defStyleAttr, 0)

    constructor(context: Context,
                attrs: AttributeSet?): this(context, attrs, 0, 0)

    constructor(context: Context): this(context, null, 0, 0)

    init {
        // todo delete this line
        setWillNotDraw(false)
        loadAttrs(context, attrs, defStyleAttr, defStyleRes)
        addView(restCountChip)
    }

    fun setLabels(labels: List<String>) {
        // remove old chips preventing layout requests
        removeAllViewsInLayout()
        addViewInLayout(restCountChip, -1, createWrapWrapLayoutParam(), true)
        labels.forEach {
            val chip = Chip(context)
            val param = LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT)
            chip.text = it
            // add a new chip preventing a layout request
            addViewInLayout(chip, -1, param, true)
            chips.add(chip)
        }
        requestLayout()
    }

    private fun loadAttrs(context: Context,
                          attrs: AttributeSet?,
                          @AttrRes defStyleAttr: Int,
                          @StyleRes defStyleRes: Int) {}

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        Log.d("mylog", "############### let's layout! changed=$changed #############")
        val layoutLeft = paddingStart + chipsMarginStart
        val layoutTop = paddingTop + chipsMarginTop
        roughLayout.iterateInLayout { line, index, integratedWidth, integratedHeight, chip ->
            Log.d("mylog", "layout(${chip.text}, line=$line, index=$index, integH=$integratedHeight, integW=$integratedWidth")
            val l = integratedWidth + index * chipsHorizontalSpace + layoutLeft
            val t = integratedHeight + line * chipsVerticalSpace + layoutTop
            chip.layout(l, t, l + chip.measuredWidth, t + chip.measuredHeight)
        }
        roughLayout.clear()
        // remove unused chips from this view
//        chips.forEach { removeViewInLayout(it) }
    }

    // todo; unnecessary method, delete
    override fun onDraw(canvas: Canvas) {
//        canvas.drawColor(Color.BLACK)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        Log.d("mylog", "######### onMeasure #########")
        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY ||
                MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.AT_MOST) {
            throw IllegalArgumentException()
        }

        measureChildren()

        val width = MeasureSpec.getSize(widthMeasureSpec)
        val availableWidth = width - paddingStart - paddingEnd - chipsMarginStart - chipsMarginEnd
        val restChips = chips.toMutableList()
        var usedChips = listOf<Chip>()
        roughLayout.clear()
        while (restChips.isNotEmpty() && roughLayout.lineCount() < maxLines) {
            usedChips = roughLayout.addLine(availableWidth, chipsHorizontalSpace, restChips)
            restChips.removeAll(usedChips)
        }
        if (restChips.isNotEmpty()) {
            roughLayout.deleteLastLine()
            restChips.addAll(0, usedChips)
            usedChips = roughLayout.addLine(availableWidth - restCountChip.measuredWidth, chipsHorizontalSpace, restChips)
            restChips.removeAll(usedChips)
            restCountChip.text = formatCountsOfRest(restChips.size)
            restCountChip.measure(
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
            roughLayout.addChipToLastLine(restCountChip)
        }

        val lines = roughLayout.lineCount()
        val chipHeight = restCountChip.measuredHeight
        val height = paddingTop + paddingBottom + chipsMarginTop + chipsMarginBottom +
                lines*chipHeight + (lines-1)*chipsVerticalSpace

        Log.d("mylog", "width=$width, height=$height")
        setMeasuredDimension(width, height)
    }

    private fun measureChildren() = forChildren {
        it.measure(
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
    }

    private fun forChildren(doSomething: (chip: Chip) -> Unit) {
        for (i in 0 until  childCount) {
            doSomething(getChildAt(i) as Chip)
        }
    }

    private fun createWrapWrapLayoutParam() = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

    private fun formatCountsOfRest(count: Int) = "+$count"

    private class RoughLayout {

        private val lines = mutableListOf<MutableList<Chip>>()
        private var currentLine = -1

        // try to fill a line with some of the passed chips and no space left
        fun addLine(width: Int, space: Int, chips: List<Chip>): List<Chip> {
            newLine()
            val usedChips = mutableListOf<Chip>()
            var restWidth = width + space
            var i = 0
            while (i < chips.size && 0 < restWidth) {
                val chip = chips[i]
                if (space + chip.measuredWidth < restWidth) {
                    restWidth -= space + chip.measuredWidth
                    lines[currentLine].add(chip)
                    usedChips.add(chip)
                }
                ++i
            }

            if (usedChips.isEmpty()) deleteLastLine()
            return usedChips
        }

        fun addChipToLastLine(chip: Chip) {
            lines[currentLine].add(chip)
        }

        private fun newLine() {
            ++currentLine
            lines.add(mutableListOf())
        }

        fun deleteLastLine() {
            if (lines.isNotEmpty()) {
                lines.removeAt(lines.size - 1)
                --currentLine
            }
        }

        fun clear() {
            lines.clear()
            currentLine = -1
        }

        fun iterateInLayout(process: (line: Int, index: Int, integratedWidth: Int,
                                      integratedHeight: Int, chip: Chip) -> Unit) {
            var integratedHeight = 0
            lines.forEachIndexed { line, chips ->
                var integratedWidth = 0
                chips.forEachIndexed { index, chip ->
                    process(line, index, integratedWidth, integratedHeight, chip)
                    integratedWidth += chip.measuredWidth
                }
                if (chips.isNotEmpty()) integratedHeight += chips[0].measuredHeight
            }
        }

        fun lineCount() = lines.size
    }
}
