package com.hayashihideo.himegoto.compactchipgroup.recyclerview

import android.content.Context
import android.support.annotation.AttrRes
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.util.Log
import com.hayashihideo.himegoto.compactchipgroup.internal.ChipsPool

class RecyclerViewWithCCG(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int)
    : RecyclerView(context, attrs, defStyleAttr) {

    private val chipsPool = ChipsPool(context)

    constructor(context: Context,
                attrs: AttributeSet?): this(context, attrs, 0)

    constructor(context: Context): this(context, null, 0)

    internal fun onCCGHolderCreated(holder: CCGHolder) {
        holder.getCcg().setChipsPool(chipsPool)
    }
}
