package com.hayashihideo.himegoto.altchipgroup.internal

import android.support.design.chip.Chip
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.hayashihideo.himegoto.R
import com.hayashihideo.himegoto.altchipgroup.AltChipGroup
import com.hayashihideo.himegoto.altchipgroup.ChipHolder
import com.hayashihideo.himegoto.altchipgroup.internal.tools.*

internal class ChipStore(private val owner: AltChipGroup,
                         private var shared: SharedChipPool): LocalChipPool {

    private class MutablePair(var holder: ChipHolder, var chip: Chip)

    private val dirtyChips = mutableListOf<MutablePair>()
    private val scrappedChips = mutableListOf<MutablePair>()
    private var cacheSizeChangeListener: LocalChipPool.LocalCacheSizeChangeListener? = null

    override fun obtainCleanChip(): Chip? {
        val pair = scrappedChips.popLastOrNull()
        pair ?: return null
        return cleanChip(pair.chip)
    }

    override fun cacheSize(): Int = scrappedChips.size

    override fun setLocalCacheSizeChangeListener(listener: LocalChipPool.LocalCacheSizeChangeListener) {
        cacheSizeChangeListener = listener
    }

    override fun removeLocalCacheSizeChangeListener() {
        cacheSizeChangeListener = null
    }

    override fun refresh() {
        clearCache()
        owner.ignoreLayoutRequestDuring {
            dirtyChips.forEach { owner.removeView(it.chip) }
        }
        dirtyChips.clear()
        owner.requestLayout()
    }

    override fun clearCache() {
        owner.ignoreLayoutRequestDuring {
            scrappedChips.forEach { owner.removeView(it.chip) }
        }
        scrappedChips.clear()
        cacheSizeChangeListener?.onLocalCacheSizeChanged()
    }

    fun getChipForPosition(position: Int): Chip = dirtyChips[position].chip

    fun getHolderForPosition(position: Int): ChipHolder = dirtyChips[position].holder

    fun setShared(shared: SharedChipPool) {
        this.shared.unregister(this)
        shared.register(this)
        this.shared = shared
    }

    fun prepareChipsForLayout(roughLayout: RoughLayout) {
        val prevCacheSize = scrappedChips.size
        if (dirtyChips.size < roughLayout.chipCount) {
            dirtyChips.forEachIndexed { pos, pair -> pair.holder = roughLayout.getChipHolderForPosition(pos) }
            for (pos in dirtyChips.size until roughLayout.chipCount) {
                val holder = roughLayout.getChipHolderForPosition(pos)
                var pair = scrappedChips.popLastOrNull()
                if (pair == null) {
                    val chip = shared.obtainCleanChip()
                    owner.addViewInLayoutInternal(chip, -1, chip.layoutParams, preventRequestLayout = true)
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

        if (prevCacheSize != scrappedChips.size) {
            cacheSizeChangeListener?.onLocalCacheSizeChanged()
        }
    }

    private fun cleanChip(chip: Chip): Chip {
        chip.parent ?: return chip
        owner.removeViewInLayout(chip)
        return chip
    }

    fun finalize() {
        owner.ignoreLayoutRequestDuring { owner.removeAllViews() }
        dirtyChips.forEach { shared.recycle(it.chip) }
        scrappedChips.forEach { shared.recycle(it.chip) }
        dirtyChips.clear()
        scrappedChips.clear()
        shared.unregister(this)
    }
}
