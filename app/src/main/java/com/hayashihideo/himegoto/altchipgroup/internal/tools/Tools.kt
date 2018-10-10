package com.hayashihideo.himegoto.altchipgroup.internal.tools

import android.view.View

internal fun loop(count: Int, process: () -> Unit) {
    for (n in 1..count) process()
}

internal fun <T> MutableList<T>.popLastOrNull()
        = if (isEmpty()) null else removeAt(lastIndex)

internal fun <T> MutableList<T>.popLast()
        = removeAt(lastIndex)

internal fun makeExactlyMeasureSpec(exactlySize: Int) =
        View.MeasureSpec.makeMeasureSpec(exactlySize, View.MeasureSpec.EXACTLY)

