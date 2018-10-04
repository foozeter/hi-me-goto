package com.hayashihideo.himegoto.compactchipgroup.internal

import android.support.annotation.Px
import android.support.design.chip.Chip
import com.hayashihideo.himegoto.compactchipgroup.ChipHolder

internal interface ChipsLayoutManager {
    var chipsPool: ChipsPool
    var maxLines: Int
    var horizontalGap: Int
    var verticalGap: Int
    val lineCount: Int
    val laidOutChipCount: Int
    val lastPosition: Int
    fun layout(left: Int, top: Int)
    fun layoutRoughly(width: Int, maxBadgeBounds: Int, chips: List<ChipHolder>)
    fun getChipForPosition(position: Int): Chip
}
