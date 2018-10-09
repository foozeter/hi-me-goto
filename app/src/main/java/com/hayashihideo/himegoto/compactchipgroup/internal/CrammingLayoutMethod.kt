package com.hayashihideo.himegoto.compactchipgroup.internal

import android.graphics.Point
import android.support.v4.math.MathUtils
import android.util.Log
import com.hayashihideo.himegoto.compactchipgroup.ChipHolder

internal class CrammingLayoutMethod: LayoutMethod {

    private val restChips = mutableListOf<ChipHolder>()
    private val usedChips = mutableListOf<ChipHolder>()
    private var spec = LayoutSpec()
    private var layout: RoughLayout? = null
    private var measure: ChipMeasure? = null
    private var outBounds: Point? = null

    override fun invoke(chips: List<ChipHolder>, layout: RoughLayout, spec: LayoutSpec, measure: ChipMeasure, outBounds: Point) {
        this.layout = layout
        this.measure = measure
        this.outBounds = outBounds
        this.spec.set(spec)
        outBounds.set(0, 0)
        restChips.addAll(chips)
        if (spec.layoutWithinBounds) invalidateMaxLines()
        layout()
        restChips.clear()
        this.layout = null
        this.measure = null
    }

    private fun layout() {
        return if (spec.isWidthUnlimited()) layoutWithUnlimitedWidth()
        else layoutWithLimitedWidth()
    }

    private fun layoutWithLimitedWidth() {
        val linesUnlimited = spec.isLinesUnlimited()
        while (restChips.isNotEmpty()) {
            var length = 0
            if (linesUnlimited || layout!!.lineCount + 1 < spec.maxLines) {
                length = cramChipsIntoNewLineAsManyAsPossible(spec.maxWidth)
            } else if (layout!!.lineCount + 1 == spec.maxLines) {
                length = cramChipsIntoNewLineAsManyAsPossible(spec.maxWidth - spec.maxBadgeBounds)
                if ((layout!!.lineCount == 0 && 0 < length) || (1 < layout!!.lineCount)) length += spec.maxBadgeBounds
            }
            if (length == 0) break
            onNewLineAdded(length)
        }
    }

    private fun layoutWithUnlimitedWidth() {
        if (restChips.isEmpty() || spec.maxLines == 0) return
        layout!!.newLine()
        var length = -spec.horizontalGap
        // fill a line with all chips in order
        restChips.forEach {
            length += spec.horizontalGap + measure!!.widthOf(it.label)
            layout!!.addChipToCurrentLine(it)
        }
        restChips.clear()
        onNewLineAdded(length)
    }
    
    // try to fill a line with some of the chips and no space left
    private fun cramChipsIntoNewLineAsManyAsPossible(maxWidth: Int): Int {
        layout!!.newLine()
        val gap = spec.horizontalGap
        var restWidth = maxWidth + gap
        var i = 0
        while (i < restChips.size && 0 < restWidth) {
            val chip = restChips[i]
            val width = measure!!.widthOf(chip.label)
            if (0 <= restWidth - gap - width) {
                restWidth -= gap + width
                layout!!.addChipToCurrentLine(chip)
                usedChips.add(chip)
            }
            ++i
        }

        if (usedChips.isEmpty()) {
            layout!!.deleteLastLine()
            return 0
        } else {
            restChips.removeAll(usedChips)
            usedChips.clear()
            return maxWidth + gap - restWidth
        }
    }

    private fun invalidateMaxLines() {
        if (restChips.isNotEmpty() && !spec.isHeightUnlimited()) {
            val availableMaxLines = (spec.maxHeight + spec.horizontalGap) / (spec.horizontalGap + measure!!.height())
            spec.maxLines = if (spec.isLinesUnlimited()) availableMaxLines else MathUtils.clamp(availableMaxLines, 0, spec.maxLines)
        }
    }

    /**
     * @param length of a new line, which includes the width of the rest count badge.
     */
    private fun onNewLineAdded(length: Int) {
        val out = outBounds!!
        out.x = Math.max(out.x, length)
        if (0 < out.y) {
            out.y += spec.verticalGap + measure!!.height()
        } else {
            out.y = measure!!.height()
        }
    }
}
