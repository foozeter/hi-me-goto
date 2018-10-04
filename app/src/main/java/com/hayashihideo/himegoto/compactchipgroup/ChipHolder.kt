package com.hayashihideo.himegoto.compactchipgroup

import android.support.design.chip.Chip
import android.view.ViewGroup

/**
 * A subclass of ChipHolder must implements equals() and hashCode() methods.
 */
open class ChipHolder(var label: String) {

    internal val layoutParams = ViewGroup.LayoutParams(0, 0)

    open fun bind(chip: Chip) {
        chip.text = label
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChipHolder

        if (label != other.label) return false
        if (layoutParams != other.layoutParams) return false

        return true
    }

    override fun hashCode(): Int {
        var result = label.hashCode()
        result = 31 * result + layoutParams.hashCode()
        return result
    }
}
