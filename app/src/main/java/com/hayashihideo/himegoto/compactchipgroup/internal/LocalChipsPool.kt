package com.hayashihideo.himegoto.compactchipgroup.internal

import android.support.design.chip.Chip

internal interface LocalChipsPool {

    interface LocalCacheSizeChangeListener {
        fun onLocalCacheSizeChanged()
    }

    fun obtainCleanChip(): Chip?
    fun cacheSize(): Int
    fun setLocalCacheSizeChangeListener(listener: LocalCacheSizeChangeListener)
    fun removeLocalCacheSizeChangeListener()
}
