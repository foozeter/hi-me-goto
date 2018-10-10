package com.hayashihideo.himegoto.altchipgroup.internal

import android.content.Context
import android.support.design.chip.Chip
import com.hayashihideo.himegoto.altchipgroup.ChipFactory
import com.hayashihideo.himegoto.altchipgroup.internal.tools.popLast
import java.lang.ref.SoftReference

internal class SharedChipPool(private val context: Context, factory: ChipFactory)
    : LocalChipPool.LocalCacheSizeChangeListener {

    var factory = factory
        set(value) {
            field = value
            refresh()
        }

    private val localPools = mutableListOf<LocalChipPool>()
    private val cleanChipRefs = mutableListOf<SoftReference<Chip>>()
    private var ignoreLocalCacheSizeChanges = false

    fun recycle(chip: Chip) {
        if (chip.parent != null) {
            throw IllegalArgumentException(
                    "This Chip has a parent, " +
                            "remove it from its parent before recycle.")
        }
        cleanChipRefs.add(SoftReference(chip))
    }

    fun obtainCleanChip(): Chip {
        while (cleanChipRefs.isNotEmpty()) {
            val chip = cleanChipRefs.popLast().get()
            if (chip != null) return chip
        }
        for (pool in localPools) {
            val chip = pool.obtainCleanChip()
            if (chip != null) return chip
        }
        return factory.create(context)
    }

    fun register(pool: LocalChipPool): Boolean {
        val added = addLocalPool(pool)
        if (added) {
            pool.setLocalCacheSizeChangeListener(this)
            sortLocalPools()
        }
        return added
    }

    fun unregister(pool: LocalChipPool): Boolean {
        val removed = removeLocalPool(pool)
        if (removed) {
            pool.removeLocalCacheSizeChangeListener()
            sortLocalPools()
        }
        return removed
    }

    override fun onLocalCacheSizeChanged() {
        if (ignoreLocalCacheSizeChanges) return
        sortLocalPools()
    }

    private fun sortLocalPools()
            = localPools.sortBy { it.cacheSize() * -1 }

    private fun addLocalPool(pool: LocalChipPool): Boolean {
        var contains = false
        localPools.forEach {
            contains = contains.or(isSameReference(it, pool))
        }
        if (!contains) {
            localPools.add(pool)
        }
        return !contains
    }

    private fun removeLocalPool(pool: LocalChipPool): Boolean {
        var removed = false
        for (i in 0 until localPools.size) {
            if (isSameReference(localPools[i], pool)) {
                localPools.removeAt(i)
                removed = true
                break
            }
        }
        return removed
    }

    private fun isSameReference(m1: LocalChipPool, m2: LocalChipPool)
            = m1 === m2

    private fun refresh() = ignoreCacheSizeChangesDuring {
        clearCache()
        localPools.forEach { it.refresh() }
    }

    private fun clearCache() {
        cleanChipRefs.clear()
        localPools.forEach { it.clearCache() }
    }

    private fun ignoreCacheSizeChangesDuring(process: () -> Unit) {
        ignoreLocalCacheSizeChanges = true
        process()
        ignoreLocalCacheSizeChanges =false
    }
}
