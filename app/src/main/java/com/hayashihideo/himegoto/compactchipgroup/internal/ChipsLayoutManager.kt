package com.hayashihideo.himegoto.compactchipgroup.internal

internal interface ChipsLayoutManager {
    var chipsPool: ChipsPool
    fun layout(roughLayout: RoughLayout, left: Int, top: Int)
}
