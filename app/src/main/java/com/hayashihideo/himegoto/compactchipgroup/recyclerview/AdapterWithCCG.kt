package com.hayashihideo.himegoto.compactchipgroup.recyclerview

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.ViewGroup

abstract class AdapterWithCCG<VH>: RecyclerView.Adapter<VH>()
        where VH: RecyclerView.ViewHolder, VH: CCGHolder {

    private var owner: RecyclerView? = null

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        owner = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        owner = null
    }

    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val holder = onMakeViewHolder(parent, viewType)
        if (owner != null && owner is RecyclerViewWithCCG) {
            (owner as RecyclerViewWithCCG).onCCGHolderCreated(holder)
        }
        return holder
    }

    /**
     * An alternative method of RecyclerView.Adapter#onCreateViewHolder(ViewGroup, Int).
     */
    abstract fun onMakeViewHolder(parent: ViewGroup, viewType: Int): VH
}
