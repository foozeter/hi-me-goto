package com.hayashihideo.himegoto.altchipgroup.internal

import android.support.design.chip.Chip

internal interface LocalChipPool {

    interface LocalCacheSizeChangeListener {
        fun onLocalCacheSizeChanged()
    }

    fun obtainCleanChip(): Chip?
    fun cacheSize(): Int
    fun setLocalCacheSizeChangeListener(listener: LocalCacheSizeChangeListener)
    fun removeLocalCacheSizeChangeListener()
    fun refresh()
    fun clearCache()
}
