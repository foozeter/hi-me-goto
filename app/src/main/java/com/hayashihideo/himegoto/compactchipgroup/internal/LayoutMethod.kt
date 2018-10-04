package com.hayashihideo.himegoto.compactchipgroup.internal

import com.hayashihideo.himegoto.compactchipgroup.ChipHolder

internal interface LayoutMethod {
    fun invoke(chips: List<ChipHolder>, layout: RoughLayout, spec: LayoutSpec)
}
