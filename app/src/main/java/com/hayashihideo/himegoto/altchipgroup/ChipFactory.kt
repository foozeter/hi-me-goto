package com.hayashihideo.himegoto.altchipgroup

import android.content.Context
import android.support.design.chip.Chip
import android.view.LayoutInflater
import android.view.ViewGroup
import com.hayashihideo.himegoto.R

open class ChipFactory {

    internal fun create(context: Context): Chip {
        val chip = onCreateChip(LayoutInflater.from(context))
        if (chip.parent != null) RuntimeException()
        chip.layoutParams = emptyLayoutParams()
        return chip
    }

    protected fun onCreateChip(inflater: LayoutInflater): Chip =
            inflater.inflate(R.layout.alt_chip_group_default_chip, null, false) as Chip

    private fun emptyLayoutParams() = ViewGroup.LayoutParams(0, 0)
}
