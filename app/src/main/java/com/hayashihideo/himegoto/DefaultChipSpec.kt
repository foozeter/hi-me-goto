package com.hayashihideo.himegoto

class DefaultChipSpec(private var label: String): AltChipGroup.ChipSpec {

    override fun getLabel() = label

    fun setLabel(label: String) {
        this.label = label
    }
}
