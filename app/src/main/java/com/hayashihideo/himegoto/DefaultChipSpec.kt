package com.hayashihideo.himegoto

class DefaultChipSpec(private var label: String): CompactChipGroup.ChipSpec {

    override fun getLabel() = label

    fun setLabel(label: String) {
        this.label = label
    }
}
