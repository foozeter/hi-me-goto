package com.hayashihideo.himegoto.compactchipgroup

import android.support.design.chip.Chip
import android.view.ViewGroup

abstract class ChipHolder {

    internal val layoutParams = ViewGroup.LayoutParams(0, 0)

    abstract fun getLabel(): String

    internal fun bind(chip: Chip) {
        chip.text = getLabel()
    }
}
