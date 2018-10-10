package com.hayashihideo.himegoto.altchipgroup.internal

import android.graphics.Point
import com.hayashihideo.himegoto.altchipgroup.ChipHolder

internal interface LayoutMethod {

    fun invoke(chips: List<ChipHolder>, layout: RoughLayout,
               spec: LayoutSpec, measure: ChipMeasure, outBounds: Point)
}
