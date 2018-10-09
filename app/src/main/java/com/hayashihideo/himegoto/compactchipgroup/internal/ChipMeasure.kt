package com.hayashihideo.himegoto.compactchipgroup.internal

import android.content.Context
import android.util.Log
import android.view.View

internal class ChipMeasure(private val context: Context, factory: ChipFactory) {

    private val unspecifiedMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    private var model = factory.create(context)
    private val cachedWidths = mutableMapOf<String, Int>()
    private var cachedHeight = 0
    private var isDirty = true

    fun widthOf(label: String): Int {
        invalidate()
        var width = cachedWidths[label] ?: -1
        if (width == -1) {
            measure(label)
            width = cachedWidths[label]!!
        }
        return width
    }

    fun height(): Int {
        invalidate()
        return cachedHeight
    }

    fun clearCache() {
        cachedWidths.clear()
        cachedHeight = 0
        isDirty = true
    }

    fun initWithFactory(factory: ChipFactory) {
        model = factory.create(context)
        clearCache()
    }

    private fun measure(label: String) {
        model.text = label
        measure()
        cachedWidths[label] = model.measuredWidth
    }

    private fun measure() {
        model.measure(unspecifiedMeasureSpec, unspecifiedMeasureSpec)
        cachedHeight = model.measuredHeight
    }

    private fun invalidate() {
        if (!isDirty) return
        clearCache()
        measure()
        isDirty = false
    }
}
