package com.hayashihideo.himegoto.compactchipgroup.internal

import android.support.annotation.Px
import android.support.design.chip.Chip
import android.util.Log
import android.view.View
import com.hayashihideo.himegoto.compactchipgroup.ChipHolder
import com.hayashihideo.himegoto.compactchipgroup.CompactChipGroup

internal class ChipsManager(
        private val owner: CompactChipGroup, pool: ChipsPool)
    : LocalChipsPool, ChipsLayoutManager {

    @Px var chipsVerticalGap = 0
        set(value) { field = Math.max(0, value) }

    @Px var chipsHorizontalGap = 0
        set(value) { field = Math.max(0, value) }

    override var chipsPool = pool.apply { register(this@ChipsManager) }
        set(newPool) {
            field.unregister(this)
            newPool.register(this)
            field = newPool
        }

    private val dirtyChips = mutableListOf<MutablePair>()
    private val scrappedChips = mutableListOf<MutablePair>()
    private var cacheSizeChangeListener: LocalChipsPool.LocalCacheSizeChangeListener? = null

    val lastPosition: Int
        get() = if (dirtyChips.isEmpty()) Constant.INVALID_POSITION else dirtyChips.lastIndex

    override fun layout(roughLayout: RoughLayout, left: Int, top: Int) {
        var chipLeft = 0
        var chipTop = 0
        prepareForLayout(roughLayout)
        roughLayout.forEachPosition { line, position, lineChanged ->
            val chip = dirtyChips[position].chip
            if (lineChanged) {
                chipLeft = left
                chipTop = top + line * (chip.measuredHeight + chipsVerticalGap)
            }
            chip.layout(chipLeft, chipTop, chipLeft + chip.measuredWidth, chipTop + chip.measuredHeight)
            chipLeft += chip.width + chipsHorizontalGap
        }
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

    fun getChipForPosition(pos: Int) = dirtyChips[pos].chip

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
