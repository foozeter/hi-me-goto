package com.hayashihideo.himegoto.compactchipgroup.internal

import android.support.annotation.Px
import android.support.design.chip.Chip
import com.hayashihideo.himegoto.compactchipgroup.ChipHolder

internal interface ChipsLayoutManager {
    var chipMeasure: ChipMeasure
    var chipsPool: ChipsPool
    var maxLines: Int
    var horizontalGap: Int
    var verticalGap: Int
    var layoutWithinBounds: Boolean
    val lineCount: Int
    val laidOutChipCount: Int
    val lastPosition: Int
    val layoutWidth: Int
    val layoutHeight: Int
    fun getChipForPosition(position: Int): Chip
    fun layout(left: Int, top: Int)
    fun layoutRoughly(chips: List<ChipHolder>, maxBadgeBounds: Int,
                      maxWidth: Int, maxHeight: Int, maxWidthSpecified: Boolean, maxHeightSpecified: Boolean)
}
