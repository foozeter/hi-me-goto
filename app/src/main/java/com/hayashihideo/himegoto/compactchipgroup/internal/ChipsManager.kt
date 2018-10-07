package com.hayashihideo.himegoto.compactchipgroup.internal

import android.support.design.chip.Chip
import android.view.View
import com.hayashihideo.himegoto.compactchipgroup.ChipHolder
import com.hayashihideo.himegoto.compactchipgroup.CompactChipGroup

internal class ChipsManager(
        private val owner: CompactChipGroup, pool: ChipsPool)
    : LocalChipsPool, ChipsLayoutManager {

    override var chipsPool = pool.apply { register(this@ChipsManager) }
        set(newPool) {
            field.unregister(this)
            newPool.register(this)
            field = newPool
        }

    override var verticalGap
        get() = layoutSpec.verticalGap
        set(value) { layoutSpec.verticalGap = Math.max(0, value) }

    override var horizontalGap
        get() = layoutSpec.horizontalGap
        set(value) { layoutSpec.horizontalGap = Math.max(0, value) }

    override var maxLines
        get() = layoutSpec.maxLines
        set(value) { layoutSpec.maxLines = value }

    override var layoutWithinBounds: Boolean
        get() = layoutSpec.layoutWithinBounds
        set(value) { layoutSpec.layoutWithinBounds = value }

    override val lineCount: Int
        get() = roughLayout.lineCount

    override val laidOutChipCount: Int
        get() = roughLayout.chipCount

    override val longestLineLength: Int
        get() = longestLineLengthInternal

    private val dirtyChips = mutableListOf<MutablePair>()
    private val scrappedChips = mutableListOf<MutablePair>()
    private val roughLayout = RoughLayout()
    private val layoutSpec = LayoutSpec()
    private var cacheSizeChangeListener: LocalChipsPool.LocalCacheSizeChangeListener? = null
    private var layoutMethod = CrammingLayoutMethod()
    private var longestLineLengthInternal = 0

    override val lastPosition: Int
        get() = if (dirtyChips.isEmpty()) Constant.INVALID_POSITION else dirtyChips.lastIndex

    override fun layout(left: Int, top: Int) {
        var chipLeft = 0
        var chipTop = 0
        prepareForLayout(roughLayout)
        roughLayout.forEachPosition { line, position, lineChanged ->
            val chip = dirtyChips[position].chip
            if (lineChanged) {
                chipLeft = left
                chipTop = top + line * (chip.measuredHeight + verticalGap)
            }
            chip.layout(chipLeft, chipTop, chipLeft + chip.measuredWidth, chipTop + chip.measuredHeight)
            chipLeft += chip.width + horizontalGap
        }
    }

    override fun layoutRoughly(chips: List<ChipHolder>, maxBadgeBounds: Int,
                               maxWidth: Int, maxHeight: Int, maxWidthSpecified: Boolean, maxHeightSpecified: Boolean) {
        layoutSpec.apply {
            if (maxWidthSpecified) this.maxWidth = maxWidth else setMaxWidthAsUnlimited()
            if (maxHeightSpecified) this.maxHeight = maxHeight else setMaxHeightAsUnlimited()
            this.maxBadgeBounds = maxBadgeBounds
        }
        roughLayout.clear()
        longestLineLengthInternal = layoutMethod.invoke(chips, roughLayout, layoutSpec)
    }

    private fun prepareForLayout(roughLayout: RoughLayout) {
        val prevCacheSize = scrappedChips.size
        if (dirtyChips.size < roughLayout.chipCount) {
            dirtyChips.forEachIndexed { pos, pair -> pair.holder = roughLayout.getChipHolderForPosition(pos) }
            for (pos in dirtyChips.size until roughLayout.chipCount) {
                val holder = roughLayout.getChipHolderForPosition(pos)
                var pair = scrappedChips.popLastOrNull()
                if (pair == null) {
                    val chip = chipsPool.obtainCleanChip()
                    owner.addChipInLayout(chip, holder.layoutParams, preventRequestLayout = true)
                    pair = MutablePair(holder, chip)
                } else {
                    pair.holder = holder
                }
                pair.chip.visibility = View.VISIBLE
                dirtyChips.add(pair)
            }

        } else if (roughLayout.chipCount < dirtyChips.size) {
            for (pos in 0 until roughLayout.chipCount) {
                dirtyChips[pos].holder = roughLayout.getChipHolderForPosition(pos)
            }
            loop(dirtyChips.size - roughLayout.chipCount) {
                val pair = dirtyChips.popLast()
                pair.chip.visibility = View.GONE
                scrappedChips.add(pair)
            }

        } else {
            dirtyChips.forEachIndexed { pos, pair -> pair.holder = roughLayout.getChipHolderForPosition(pos) }
        }

        dirtyChips.forEach {
            val chip = it.chip
            val holder = it.holder
            holder.bind(chip)
            chip.measure(View.MeasureSpec.makeMeasureSpec(holder.layoutParams.width, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(holder.layoutParams.height, View.MeasureSpec.EXACTLY))
        }

        if (prevCacheSize != scrappedChips.size) {
            cacheSizeChangeListener?.onLocalCacheSizeChanged()
        }
    }

    override fun cacheSize() = scrappedChips.size

    override fun setLocalCacheSizeChangeListener(
            listener: LocalChipsPool.LocalCacheSizeChangeListener) {
        cacheSizeChangeListener = listener
    }

    override fun removeLocalCacheSizeChangeListener() {
        cacheSizeChangeListener = null
    }

    override fun getChipForPosition(position: Int) = dirtyChips[position].chip

    override fun obtainCleanChip(): Chip? {
        val pair = scrappedChips.popLastOrNull()
        pair ?: return null
        return cleanChip(pair.chip)
    }

    private fun cleanChip(chip: Chip): Chip {
        chip.parent ?: return chip
        owner.removeViewInLayout(chip)
        return chip
    }

    private fun loop(count: Int, process: () -> Unit) {
        for (n in 1..count) process()
    }

    private fun max(a: Int, b: Int, c: Int) = Math.max(Math.max(a, b), c)

    private fun <T> MutableList<T>.popLastOrNull()
            = if (isEmpty()) null else removeAt(lastIndex)

    private fun <T> MutableList<T>.popLast()
            = removeAt(lastIndex)

    fun finalize() {
        dirtyChips.forEach {
            owner.removeViewInLayout(it.chip)
            chipsPool.recycle(it.chip)
        }
        scrappedChips.forEach {
            owner.removeViewInLayout(it.chip)
            chipsPool.recycle(it.chip)
        }
        chipsPool.unregister(this)
    }

    private class MutablePair(var holder: ChipHolder, var chip: Chip)
}
