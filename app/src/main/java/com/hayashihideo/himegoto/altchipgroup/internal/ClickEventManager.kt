package com.hayashihideo.himegoto.altchipgroup.internal

import android.support.design.chip.Chip
import android.view.View

internal class ClickEventManager(private val store: ChipStore) {

    var clickListener: View.OnClickListener? = null
        set(value) {
            field = value
            store.forAllChips { it.setOnClickListener(value) }
        }

    fun setAllListeners(chip: Chip) {
        chip.setOnClickListener(clickListener)
    }

    fun removeAllListeners(chip: Chip) {
        chip.setOnClickListener(null)
    }
}
