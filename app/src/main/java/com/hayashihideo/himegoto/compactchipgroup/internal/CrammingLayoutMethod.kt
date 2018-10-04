package com.hayashihideo.himegoto.compactchipgroup.internal

import com.hayashihideo.himegoto.compactchipgroup.ChipHolder

internal class CrammingLayoutMethod: LayoutMethod {

    private val restChips = mutableListOf<ChipHolder>()
    private val usedChips = mutableListOf<ChipHolder>()

    override fun invoke(chips: List<ChipHolder>, layout: RoughLayout, spec: LayoutSpec) {
        val noLimitLines = spec.maxLines == Constant.UNSPECIFIED
        restChips.addAll(chips)
        while (restChips.isNotEmpty()) {
            if (noLimitLines || layout.lineCount + 1 < spec.maxLines) {
                cramChipsIntoNewLineAsManyAsPossible(layout, spec.width, spec.horizontalGap)
            } else if (layout.lineCount + 1 == spec.maxLines) {
                cramChipsIntoNewLineAsManyAsPossible(
                        layout, spec.width - spec.maxBadgeBounds, spec.horizontalGap)
            } else break
        }
        restChips.clear()
    }
    
    // try to fill a line with some of the passed chips and no space left
    private fun cramChipsIntoNewLineAsManyAsPossible(
            layout: RoughLayout,
            lineWidth: Int,
            gap: Int) {

        layout.newLine()
        var restWidth = lineWidth + gap
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
        } else {
            restChips.removeAll(usedChips)
            usedChips.clear()
        }
    }
}
