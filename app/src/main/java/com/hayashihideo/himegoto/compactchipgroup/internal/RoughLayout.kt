package com.hayashihideo.himegoto.compactchipgroup.internal

import com.hayashihideo.himegoto.compactchipgroup.ChipHolder
import com.hayashihideo.himegoto.compactchipgroup.internal.Constant.Companion.UNSPECIFIED

internal class RoughLayout {

    var verticalGap = 0
        set(value) { field = Math.max(0, value) }

    var horizontalGap = 0
        set(value) { field = Math.max(0, value) }

    var maxLines = UNSPECIFIED
        set(value) { field = Math.max(UNSPECIFIED, value) }

    var chipCount = 0
        private set

    val lineCount: Int
        get() = layout.size

    private val layout = mutableListOf<MutableList<ChipHolder>>()

    fun layout(width: Int, chips: List<ChipHolder>): List<ChipHolder> {
        clear()
        val laidOutChips = if (width == UNSPECIFIED) {
            putAllChipsIntoNewLineInOrder(chips)
            chips
        } else {
            val restChips = chips.toMutableList()
            val totalUsedChips = mutableListOf<ChipHolder>()
            var usedChips: List<ChipHolder>
            val noLimitLines = maxLines == UNSPECIFIED
            while (restChips.isNotEmpty() && (noLimitLines || lineCount < maxLines)) {
                usedChips = cramChipsIntoNewLineAsManyAsPossible(width, restChips)
                totalUsedChips.addAll(usedChips)
                restChips.removeAll(usedChips)
            }
            totalUsedChips
        }
        chipCount += laidOutChips.size
        return laidOutChips
    }

    // try to fill a line with some of the passed chips and no space left
    private fun cramChipsIntoNewLineAsManyAsPossible(
            width: Int, chips: List<ChipHolder>): MutableList<ChipHolder> {

        newLine()
        val usedChips = mutableListOf<ChipHolder>()
        var restWidth = width + horizontalGap
        var i = 0
        while (i < chips.size && 0 < restWidth) {
            val chip = chips[i]
            if (horizontalGap + chip.layoutParams.width < restWidth) {
                restWidth -= horizontalGap + chip.layoutParams.width
                lastLine().add(chip)
                usedChips.add(chip)
            }
            ++i
        }

        if (usedChips.isEmpty()) deleteLastLine()
        return usedChips
    }

    private fun putAllChipsIntoNewLineInOrder(chips: List<ChipHolder>) {
        if (chips.isEmpty()) return
        newLine()
        lastLine().addAll(chips)
    }

    private fun newLine() {
        layout.add(mutableListOf())
    }

    fun deleteLastLine(): List<ChipHolder> {
        if (layout.isEmpty()) return emptyList()
        val lastLine = layout.removeAt(layout.lastIndex)
        chipCount -= lastLine.size
        return lastLine
    }

    fun forEachPosition(action: (line: Int, position: Int, lineChanged: Boolean) -> Unit) {
        var pos = 0
        layout.forEachIndexed { line, holders ->
            holders.forEachIndexed { index, _ ->
                action(line, pos, index == 0)
                ++pos
            }
        }
    }

    private fun lastLine() = layout[layout.lastIndex]

    fun clear() {
        layout.clear()
        chipCount = 0
    }

    fun getChipHolderForPosition(position: Int): ChipHolder {
        var pos = position
        var i = 0
        while (i < layout.size && layout[i].size <= pos) {
            pos -= layout[i].size
            ++i
        }
        return if (pos < layout[i].size) layout[i][pos]
        else throw NoSuchElementException()
    }
}
