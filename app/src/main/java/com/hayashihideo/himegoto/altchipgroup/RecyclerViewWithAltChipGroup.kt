package com.hayashihideo.himegoto.altchipgroup

import android.content.Context
import android.support.annotation.AttrRes
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.hayashihideo.himegoto.altchipgroup.internal.ChipMeasure
import com.hayashihideo.himegoto.altchipgroup.internal.SharedChipPool

class RecyclerViewWithAltChipGroup(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int)
    : RecyclerView(context, attrs, defStyleAttr) {

    private val chipsPool = SharedChipPool(context, ChipFactory())
    private val chipMeasure = ChipMeasure(context, ChipFactory())

    constructor(context: Context,
                attrs: AttributeSet?): this(context, attrs, 0)

    constructor(context: Context): this(context, null, 0)

    internal fun onViewHolderCreated(holder: ViewHolder) {
        holder.getAltChipGroup().setShared(chipsPool, chipMeasure)
    }

    fun setAdapter(adapter: Adapter<*>?) {
        super.setAdapter(adapter)
        adapter ?: return
        val factory = adapter.getChipFactory()
        if (factory != null) {
            chipMeasure.initWithFactory(factory)
            chipsPool.factory = factory
        }
    }

    abstract class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        abstract fun getAltChipGroup(): AltChipGroup
    }

    abstract class Adapter<VH: ViewHolder>: RecyclerView.Adapter<VH>() {

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
            if (owner != null && owner is RecyclerViewWithAltChipGroup) {
                (owner as RecyclerViewWithAltChipGroup).onViewHolderCreated(holder)
            }
            return holder
        }

        /**
         * An alternative method of RecyclerView.Adapter#onCreateViewHolder(ViewGroup, Int).
         */
        abstract fun onMakeViewHolder(parent: ViewGroup, viewType: Int): VH

        /**
         * Return a custom chip factory, or null.
         */
        open fun getChipFactory(): ChipFactory? = null
    }
}
