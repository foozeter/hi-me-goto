package com.hayashihideo.himegoto.compactchipgroup.internal

internal class LayoutSpec(
        var maxWidth: Int = -1,
        var maxHeight: Int = -1,
        var maxLines: Int = -1,
        var horizontalGap: Int = 0,
        var verticalGap: Int = 0,
        var maxBadgeBounds: Int = 0,
        var layoutWithinBounds: Boolean = false) {

    fun isLinesUnlimited() = maxLines < 0

    fun isWidthUnlimited() = maxWidth < 0

    fun isHeightUnlimited() = maxHeight < 0

    fun set(other: LayoutSpec) {
        maxWidth = other.maxWidth
        maxHeight = other.maxHeight
        maxLines = other.maxLines
        horizontalGap = other.horizontalGap
        verticalGap = other.verticalGap
        maxBadgeBounds = other.maxBadgeBounds
        layoutWithinBounds = other.layoutWithinBounds
    }

    fun setMaxWidthAsUnlimited() {
        maxWidth = -1
    }

    fun setMaxHeightAsUnlimited() {
        maxHeight = -1
    }

    fun setMaxLinesAsUnlimited() {
        maxLines = -1
    }
}
