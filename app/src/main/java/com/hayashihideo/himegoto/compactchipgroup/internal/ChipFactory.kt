package com.hayashihideo.himegoto.compactchipgroup.internal

import android.content.Context
import android.support.design.chip.Chip
import android.view.LayoutInflater
import android.view.ViewGroup
import com.hayashihideo.himegoto.R
import com.hayashihideo.himegoto.compactchipgroup.CompactChipGroup

internal class ChipFactory {
    companion object {
        fun create(context: Context): Chip {
            val chip = LayoutInflater.from(context)
                    .inflate(R.layout.compact_chip_group_default_chip, null, false) as Chip
            chip.layoutParams = ViewGroup.LayoutParams(0, 0)
            return chip
        }
    }
}
