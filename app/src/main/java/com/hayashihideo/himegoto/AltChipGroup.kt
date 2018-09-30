package com.hayashihideo.himegoto

import android.content.Context
import android.support.annotation.AttrRes
import android.support.annotation.Px
import android.support.annotation.StyleRes
import android.support.design.chip.Chip
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView

class AltChipGroup(context: Context,
                   attrs: AttributeSet?,
                   @AttrRes defStyleAttr: Int,
                   @StyleRes defStyleRes: Int)
    : ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    var maxLines = RoughLayout.UNSPECIFIED
        set(value) {
            field = value
            roughLayout.maxLines = value
        }

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

    private val chipsManager = ChipsManager(owner = this)

    private val restCountBadge = inflateRestCountBadge()

    constructor(context: Context,
                attrs: AttributeSet?,
                @AttrRes defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

    constructor(context: Context,
                attrs: AttributeSet?) : this(context, attrs, 0, 0)

    constructor(context: Context) : this(context, null, 0, 0)

    init {
        addView(restCountBadge)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (changed) {
            val layoutLeft = paddingStart + chipsMarginStart
            val layoutTop = paddingTop + chipsMarginTop
            var chipLeft = 0
            var chipTop = 0
            val chipHeight = chipMeasure.measureHeight()
            var chip: Chip
            roughLayout.iterateInLayout(
                    onNewLineBegin = { line ->
                        chipLeft = layoutLeft
                        chipTop = layoutTop + line * (chipHeight + chipsVerticalGap)
                    },
                    onLayoutChip = { position, holder ->
                        chip = chipsManager.getChipForPositionInLayout(position)
                        bindChipSpec(chip, holder)
                        measureChip(chip, holder)
                        chip.layout(chipLeft, chipTop, chipLeft + holder.layoutParams.width, chipTop + holder.layoutParams.height)
                        chipLeft += holder.layoutParams.width + chipsHorizontalGap
                    })

            if (roughLayout.laidOutChipCount < chipHolders.size) {
                chip = chipsManager.getChipForPositionInLayout(roughLayout.laidOutChipCount - 1)
                restCountBadge.layout(
                        chip.right + restCountBadgeMarginStart,
                        chip.top,
                        chip.right + restCountBadgeMarginStart + restCountBadge.measuredWidth,
                        chip.bottom)
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY ||
                MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.AT_MOST) {
            throw IllegalArgumentException()
        }

        // measure chips
        val chipHeight = chipMeasure.measureHeight()
        chipHolders.forEach {
            it.layoutParams.width = chipMeasure.measureWidth(it.label)
            it.layoutParams.height = chipHeight
        }

        val width = MeasureSpec.getSize(widthMeasureSpec)
        val availableWidth = width - paddingStart - paddingEnd - chipsMarginStart - chipsMarginEnd
        val restChips = chipHolders.toMutableList()
        roughLayout.clear()
        var usedChips = roughLayout.layout(availableWidth, chipsHorizontalGap, chipHolders)
        restChips.removeAll(usedChips)

        if (restChips.isNotEmpty()) {
            usedChips = roughLayout.deleteLastLine() ?: emptyList()
            restChips.addAll(0, usedChips)
            usedChips = roughLayout.layoutInNewLine(
                    availableWidth - restCountBadge.measuredWidth, chipsHorizontalGap, restChips)
            restChips.removeAll(usedChips)
            restCountBadge.setRestCount(restChips.size)
            restCountBadge.measure(
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                    MeasureSpec.makeMeasureSpec(chipHeight, MeasureSpec.EXACTLY))
        }

        val lines = roughLayout.lines()
        val height = paddingTop + paddingBottom + chipsMarginTop + chipsMarginBottom +
                lines * chipHeight + (lines - 1) * chipsVerticalGap

        setMeasuredDimension(width, height)
    }

    fun addLabels(labels: List<String>) {
        labels.forEach {
            chipHolders.add(ChipHolder(it, createEmptyLayoutParams()))
        }
    }

    private fun bindChipSpec(chip: Chip, holder: ChipHolder) {
        chip.text = holder.label
    }

    private fun measureChip(chip: Chip, holder: ChipHolder) {
        chip.measure(MeasureSpec.makeMeasureSpec(holder.layoutParams.width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(holder.layoutParams.height, MeasureSpec.EXACTLY))
    }

    private fun inflateRestCountBadge(): TextView = LayoutInflater.from(context)
                    .inflate(R.layout.alt_chip_group_rest_count_badge, this, false) as TextView
    
    private fun createEmptyLayoutParams() = ViewGroup.LayoutParams(0, 0)

    private data class ChipHolder(var label: String,
                                  var layoutParams: LayoutParams,
                                  var isLaidOut: Boolean = false)

    private class RoughLayout {

        companion object {
            const val UNSPECIFIED = -1
            private const val INVALID_LINE = -1
        }

        var maxLines = UNSPECIFIED

        var laidOutChipCount = 0
            private set

        private val lines = mutableListOf<MutableList<ChipHolder>>()
        private var currentLine = INVALID_LINE

        fun layout(width: Int, gap: Int, chips: List<ChipHolder>): List<ChipHolder> {
            clear()
            val laidOutChips = if (width == UNSPECIFIED) {
                layoutAllChipsInSingleLine(gap, chips)
            } else {
                layoutDefault(width, gap, chips)
            }
            laidOutChipCount += laidOutChips.size
            return laidOutChips
        }

        fun layoutInNewLine(width: Int, gap: Int, chips: List<ChipHolder>): List<ChipHolder> {
            val laidOutChips = if (width == UNSPECIFIED) {
                layoutAllChipsInSingleLine(gap, chips)
            } else {
                fillUpNewLine(width, gap, chips)
            }
            laidOutChipCount += laidOutChips.size
            return laidOutChips
        }

        private fun layoutAllChipsInSingleLine(gap: Int, chips: List<ChipHolder>): List<ChipHolder> {
            TODO("NOT IMPLEMENTED")
        }

        private fun layoutDefault(width: Int, gap: Int, chips: List<ChipHolder>): List<ChipHolder> {
            val restChips = chips.toMutableList()
            val totalUsedChips = mutableListOf<ChipHolder>()
            var usedChips: List<ChipHolder>
            val noLimitLines = maxLines == UNSPECIFIED
            while (restChips.isNotEmpty() && (noLimitLines || lines() < maxLines)) {
                usedChips = fillUpNewLine(width, gap, restChips)
                totalUsedChips.addAll(usedChips)
                restChips.removeAll(usedChips)
            }
            return totalUsedChips
        }

        // try to fill a line with some of the passed chips and no space left
        private fun fillUpNewLine(width: Int, gap: Int, chips: List<ChipHolder>): List<ChipHolder> {
            newLine()
            val usedChips = mutableListOf<ChipHolder>()
            var restWidth = width + gap
            var i = 0
            while (i < chips.size && 0 < restWidth) {
                val chip = chips[i]
                if (gap + chip.layoutParams.width < restWidth) {
                    restWidth -= gap + chip.layoutParams.width
                    lines[currentLine].add(chip)
                    usedChips.add(chip)
                }
                ++i
            }

            if (usedChips.isEmpty()) deleteLastLine()
            return usedChips
        }

        private fun newLine() {
            ++currentLine
            lines.add(mutableListOf())
        }

        fun deleteLastLine(): List<ChipHolder>? {
            if (lines.isNotEmpty()) {
                val ret = lines.removeAt(lines.size - 1)
                laidOutChipCount -= ret.size
                --currentLine
                return ret
            }
            return null
        }

        fun lines() = lines.size

        fun clear() {
            lines.clear()
            laidOutChipCount = 0
            currentLine = INVALID_LINE
        }

        fun iterateInLayout(onNewLineBegin: (line: Int) -> Unit,
                            onLayoutChip: (position: Int, holder: ChipHolder) -> Unit) {

            var position = 0
            lines.forEachIndexed { line, specs ->
                onNewLineBegin(line)
                specs.forEach { spec ->
                    onLayoutChip(position, spec)
                    ++position
                }
            }
        }
    }

    private class ChipMeasure(owner: AltChipGroup) {

        private var isDirty = true

        private val chip = LayoutInflater.from(owner.context)
                .inflate(R.layout.alt_chip_group_default_chip, owner, false) as Chip

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

    private class ChipsManager(val owner: AltChipGroup) {

        private val dirtyChips = mutableMapOf<Int, Chip>()

        fun getChipForPositionInLayout(position: Int): Chip {
            var chip = dirtyChips[position]
            if (chip == null) {
                chip = inflateChip()
                owner.addViewInLayout(chip, -1, createWrapWrapLayoutParams())
                dirtyChips[position] = chip
            }
            return chip
        }

        private fun createWrapWrapLayoutParams() = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        private fun inflateChip(): Chip
                = LayoutInflater.from(owner.context)
                .inflate(R.layout.alt_chip_group_default_chip, owner, false) as Chip
    }

    private fun TextView.setRestCount(count: Int) {
        val text = "+$count"
        setText(text)
    }
}
