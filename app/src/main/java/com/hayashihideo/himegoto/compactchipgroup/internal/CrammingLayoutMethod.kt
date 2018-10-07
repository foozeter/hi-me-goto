package com.hayashihideo.himegoto.compactchipgroup.internal

import android.support.v4.math.MathUtils
import android.util.Log
import com.hayashihideo.himegoto.compactchipgroup.ChipHolder

internal class CrammingLayoutMethod: LayoutMethod {

    private val restChips = mutableListOf<ChipHolder>()
    private val usedChips = mutableListOf<ChipHolder>()
    private var spec = LayoutSpec()

    override fun invoke(chips: List<ChipHolder>, layout: RoughLayout, spec: LayoutSpec): Int {
        restChips.addAll(chips)
        this.spec.set(spec)
        if (spec.layoutWithinBounds) invalidateMaxLines()
        val ret = layout(layout)
        restChips.clear()
        return ret
    }

    private fun layout(layout: RoughLayout): Int {
        return if (spec.isWidthUnlimited()) layoutWithUnlimitedWidth(layout)
        else layoutWithLimitedWidth(layout)
    }

    private fun layoutWithLimitedWidth(layout: RoughLayout): Int {
        val linesUnlimited = spec.isLinesUnlimited()
        var maxLength = 0
        while (restChips.isNotEmpty()) {
            var length = 0
            if (linesUnlimited || layout.lineCount + 1 < spec.maxLines) {
                length = cramChipsIntoNewLineAsManyAsPossible(layout, spec.maxWidth)
            } else if (layout.lineCount + 1 == spec.maxLines) {
                length = cramChipsIntoNewLineAsManyAsPossible(layout, spec.maxWidth - spec.maxBadgeBounds)
                if ((layout.lineCount == 0 && 0 < length) || (1 < layout.lineCount)) length += spec.maxBadgeBounds
            }
            maxLength = Math.max(maxLength, length)
            if (length == 0) break
        }
        return maxLength
    }

    private fun layoutWithUnlimitedWidth(layout: RoughLayout): Int {
        if (restChips.isEmpty() || spec.maxLines == 0) return 0
        layout.newLine()
        var length = -spec.horizontalGap
        // fill a line with all chips in order
        restChips.forEach {
            length += spec.horizontalGap + it.layoutParams.width
            layout.addChipToCurrentLine(it)
        }
        restChips.clear()
        return length
    }
    
    // try to fill a line with some of the chips and no space left
    private fun cramChipsIntoNewLineAsManyAsPossible(layout: RoughLayout, maxWidth: Int): Int {
        layout.newLine()
        val gap = spec.horizontalGap
        var restWidth = maxWidth + gap
        var i = 0
        while (i < restChips.size && 0 < restWidth) {
            val chip = restChips[i]
            if (0 <= restWidth - gap - chip.layoutParams.width) {
                restWidth -= gap + chip.layoutParams.width
                layout.addChipToCurrentLine(chip)
                usedChips.add(chip)
            }
            ++i
        }

        if (usedChips.isEmpty()) {
            layout.deleteLastLine()
            return 0
        } else {
            restChips.removeAll(usedChips)
            usedChips.clear()
            return maxWidth + gap - restWidth
        }
    }

    private fun invalidateMaxLines() {
        if (restChips.isNotEmpty() && !spec.isHeightUnlimited()) {
            val chipHeight = restChips.first().layoutParams.height
            val availableMaxLines = (spec.maxHeight + spec.horizontalGap) / (spec.horizontalGap + chipHeight)
            spec.maxLines = if (spec.isLinesUnlimited()) availableMaxLines else MathUtils.clamp(availableMaxLines, 0, spec.maxLines)
        }
    }
}
