package com.hayashihideo.himegoto.compactchipgroup.internal

import android.graphics.Point
import com.hayashihideo.himegoto.compactchipgroup.ChipHolder

internal interface LayoutMethod {

    /**
     * @return Length of the longest line in layout. (include the count badge length)
     */
    fun invoke(chips: List<ChipHolder>, layout: RoughLayout, spec: LayoutSpec, measure: ChipMeasure, outBounds: Point)
}
