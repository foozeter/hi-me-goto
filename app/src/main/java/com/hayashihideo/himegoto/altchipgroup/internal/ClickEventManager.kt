package com.hayashihideo.himegoto.altchipgroup.internal

import android.support.design.chip.Chip
import android.view.View
import com.hayashihideo.himegoto.altchipgroup.AltChipGroup

internal class ClickEventManager(private val owner: AltChipGroup) {

    var clickListener: AltChipGroup.OnChipClickListener? = null
    var longClickListener: AltChipGroup.OnChipLongClickListener? = null

    private val onViewClick: (view: View) -> Unit = { view: View ->
        if (clickListener != null) {
            view as Chip
            val position = owner.viewStore.getPositionForChip(view)
            val holder = owner.viewStore.getHolderForPosition(position)
            clickListener?.onChipClick(owner, view, holder, position)
        }
    }

    private val onViewLongClick:(view: View) -> Boolean = { view ->
        if (longClickListener != null) {
            view as Chip
            val position = owner.viewStore.getPositionForChip(view)
            val holder = owner.viewStore.getHolderForPosition(position)
            longClickListener!!.onChipLongClick(owner, view, holder, position)
        } else false
    }

    fun setAllListeners(chip: Chip) {
        chip.setOnClickListener(onViewClick)
        chip.setOnLongClickListener(onViewLongClick)
    }

    fun removeAllListeners(chip: Chip) {
        chip.setOnClickListener(null)
        chip.setOnLongClickListener(null)
    }
}
