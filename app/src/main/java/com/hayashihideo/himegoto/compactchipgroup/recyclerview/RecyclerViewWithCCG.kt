package com.hayashihideo.himegoto.compactchipgroup.recyclerview

import android.content.Context
import android.support.annotation.AttrRes
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.util.Log
import com.hayashihideo.himegoto.compactchipgroup.internal.ChipFactory
import com.hayashihideo.himegoto.compactchipgroup.internal.ChipMeasure
import com.hayashihideo.himegoto.compactchipgroup.internal.ChipsPool

class RecyclerViewWithCCG(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int)
    : RecyclerView(context, attrs, defStyleAttr) {

    private val chipsPool = ChipsPool(context, ChipFactory())
    private val chipMeasure = ChipMeasure(context, ChipFactory())

    constructor(context: Context,
                attrs: AttributeSet?): this(context, attrs, 0)

    constructor(context: Context): this(context, null, 0)

    internal fun onCCGHolderCreated(holder: CCGHolder) {
        holder.getCcg().setShared(chipsPool, chipMeasure)
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        super.setAdapter(adapter)
        if (adapter != null && adapter is AdapterWithCCG<*>) {
            onAdapterWithCCGSet(adapter as AdapterWithCCG)
        }
    }

    private fun onAdapterWithCCGSet(adapter: AdapterWithCCG<*>) {
        val factory = (adapter as AdapterWithCCG).getChipFactory()
        if (factory != null) {
            chipMeasure.initWithFactory(factory)
            chipsPool.factory = factory
        }
    }
}
