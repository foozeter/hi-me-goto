package com.hayashihideo.himegoto.altchipgroup

import android.content.Context
import android.graphics.Point
import android.support.annotation.AttrRes
import android.support.annotation.StyleRes
import android.support.design.chip.Chip
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.hayashihideo.himegoto.altchipgroup.internal.*

class AltChipGroup(context: Context,
       attrs: AttributeSet?,
       @AttrRes defStyleAttr: Int,
       @StyleRes defStyleRes: Int): ViewGroup(context, attrs, defStyleAttr, defStyleRes) {

    var layoutWithinBounds: Boolean
        get() = layoutManager.layoutWithinBounds
        set(value) { layoutManager.layoutWithinBounds = value }

    var maxLines: Int
        get() = layoutManager.maxLines
        set(value) { layoutManager.maxLines = value }

    var horizontalGap: Int
        get() = layoutManager.horizontalGap
        set(value) { layoutManager.horizontalGap = value }

    var verticalGap: Int
        get() = layoutManager.verticalGap
        set(value) { layoutManager.verticalGap = value }

    var restCountBadgeMarginStart: Int
        get() = layoutManager.restCountBadgeMarginStart
        set(value) { layoutManager.restCountBadgeMarginStart = value }

    var chipsMarginStart: Int
        get() = layoutManager.chipsMarginStart
        set(value) { layoutManager.chipsMarginStart = value }

    var chipsMarginEnd: Int
        get() = layoutManager.chipsMarginEnd
        set(value) { layoutManager.chipsMarginEnd = value }

    var chipsMarginTop: Int
        get() = layoutManager.chipsMarginTop
        set(value) { layoutManager.chipsMarginTop = value }

    var chipsMarginBottom: Int
        get() = layoutManager.chipsMarginBottom
        set(value) { layoutManager.chipsMarginBottom = value }

    private val cachedMeasureSpecs = Point(-1, -1)
    private val cachedMeasuredSize = Point()
    private var layoutIsFrozen = false
    private var layoutRequested = false
    private var measureCached = false

    private val chipHolders = mutableListOf<ChipHolder>()
    private val layoutManager = LayoutManager(this)

    internal var chipMeasure = ChipMeasure(context, ChipFactory())
    internal val viewStore = ChipStore(this, SharedChipPool(context, ChipFactory()))
    internal val clickEventManager = ClickEventManager(this)

    constructor(context: Context,
                attrs: AttributeSet?,
                @AttrRes defStyleAttr: Int): this(context, attrs, defStyleAttr, 0)

    constructor(context: Context,
                attrs: AttributeSet?): this(context, attrs, 0, 0)

    constructor(context: Context): this(context, null, 0, 0)

    init {
        loadAttributes(context, attrs, defStyleAttr, defStyleRes)
    }

    fun holders(): List<ChipHolder> = chipHolders

    fun setChipHolders(holders: List<ChipHolder>) {
        chipHolders.clear()
        chipHolders.addAll(holders)
        requestLayout()
    }

    fun setOnChipClickListener(listener: (group: AltChipGroup, chip: Chip, holder: ChipHolder, position: Int) -> Unit) {
        clickEventManager.clickListener = object: OnChipClickListener {
            override fun onChipClick(group: AltChipGroup, chip: Chip, holder: ChipHolder, position: Int)
                    = listener(group, chip, holder, position)
        }
    }

    fun setOnChipLongClickListener(listener: (group: AltChipGroup, chip: Chip, holder: ChipHolder, position: Int) -> Boolean) {
        clickEventManager.longClickListener = object: OnChipLongClickListener {
            override fun onChipLongClick(group: AltChipGroup, chip: Chip, holder: ChipHolder, position: Int): Boolean
                    = listener(group, chip, holder, position)
        }
    }

    fun setOnChipClickListener(listener: OnChipClickListener) {
        clickEventManager.clickListener = listener
    }

    fun setOnChipLongClickListener(listener: OnChipLongClickListener) {
        clickEventManager.longClickListener = listener
    }

    fun removeOnChipClickListener() {
        clickEventManager.clickListener = null
    }

    fun removeOnChipLongClickListener() {
        clickEventManager.longClickListener = null
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (changed || layoutRequested) {
            layoutRequested = false
            layoutManager.onLayout(left, top, right, bottom)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if ((layoutRequested && !measureCached) ||
                cachedMeasureSpecs.x != widthMeasureSpec ||
                cachedMeasureSpecs.y != heightMeasureSpec) {
            val width = MeasureSpec.getSize(widthMeasureSpec)
            val height = MeasureSpec.getSize(heightMeasureSpec)
            val widthMode = MeasureSpec.getMode(widthMeasureSpec)
            val heightMode = MeasureSpec.getMode(heightMeasureSpec)
            layoutManager.onMeasure(width, height, widthMode, heightMode)
            cachedMeasuredSize.set(measuredWidth, measuredHeight)
            cachedMeasureSpecs.set(widthMeasureSpec, heightMeasureSpec)
            measureCached = true
        } else {
            setMeasuredDimension(cachedMeasuredSize.x, cachedMeasuredSize.y)
        }
    }

    override fun requestLayout() {
        if (!layoutIsFrozen) {
            measureCached = false
            layoutRequested = true
            super.requestLayout()
        }
    }


    internal fun addViewInLayoutInternal(view: View, index: Int, params: LayoutParams, preventRequestLayout: Boolean) =
            addViewInLayout(view, index, params, preventRequestLayout)

    internal fun setMeasuredDimensionInternal(width: Int, height: Int) = setMeasuredDimension(width, height)

    internal fun ignoreLayoutRequestDuring(process: () -> Unit) {
        layoutIsFrozen = true
        process()
        layoutIsFrozen = false
    }

    internal fun setShared(pool: SharedChipPool, measure: ChipMeasure) {
        chipMeasure = measure
        viewStore.setShared(pool)
    }

    private fun loadAttributes(
            context: Context,
            attrs: AttributeSet?,
            @AttrRes defStyleAttr: Int,
            @StyleRes defStyleRes: Int) {

    }

    interface OnChipClickListener {
        fun onChipClick(group: AltChipGroup, chip: Chip, holder: ChipHolder, position: Int)
    }

    interface OnChipLongClickListener {

        /**
         * @return true if the callback consumed the long click, false otherwise.
         */
        fun onChipLongClick(group: AltChipGroup, chip: Chip, holder: ChipHolder, position: Int): Boolean
    }
}
