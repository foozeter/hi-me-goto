package com.hayashihideo.himegoto.compactchipgroup.internal

import com.hayashihideo.himegoto.compactchipgroup.ChipHolder
import com.hayashihideo.himegoto.compactchipgroup.internal.Constant.Companion.UNSPECIFIED

internal class RoughLayout {

    var chipCount = 0
        private set

    val lineCount: Int
        get() = layout.size

    private val layout = mutableListOf<MutableList<ChipHolder>>()

    fun newLine() = layout.add(mutableListOf())

    fun deleteLastLine(): List<ChipHolder> {
        if (layout.isEmpty()) return emptyList()
        val lastLine = layout.removeAt(layout.lastIndex)
        chipCount -= lastLine.size
        return lastLine
    }

    fun clear() {
        layout.clear()
        chipCount = 0
    }

    fun addChipToCurrentLine(chip: ChipHolder) {
        lastLine().add(chip)
        ++chipCount
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
}
