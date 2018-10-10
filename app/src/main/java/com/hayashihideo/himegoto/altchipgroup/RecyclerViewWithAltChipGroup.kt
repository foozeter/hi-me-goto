package com.hayashihideo.himegoto.altchipgroup

import android.content.Context
import android.support.annotation.AttrRes
import android.support.design.chip.Chip
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.hayashihideo.himegoto.R
import com.hayashihideo.himegoto.altchipgroup.internal.ChipMeasure
import com.hayashihideo.himegoto.altchipgroup.internal.SharedChipPool

class RecyclerViewWithAltChipGroup(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int)
    : RecyclerView(context, attrs, defStyleAttr),
        AltChipGroup.OnChipClickListener,
        AltChipGroup.OnChipLongClickListener {

    private var onChipClickListener: OnChipClickListener? = null
    private var onChipLongClickListener: OnChipLongClickListener? = null

    private val chipsPool = SharedChipPool(context, ChipFactory())
    private val chipMeasure = ChipMeasure(context, ChipFactory())

    constructor(context: Context,
                attrs: AttributeSet?): this(context, attrs, 0)

    constructor(context: Context): this(context, null, 0)

    /**
     * an implementation of AltChipGroup.OnChipClickListener.
     */
    override fun onChipClick(group: AltChipGroup, chip: Chip, holder: ChipHolder, position: Int) {
        val adapterPosition = group.getTag(KEY_ADAPTER_POSITION) as Int
        onChipClickListener?.onChipClick(chip, holder, position, adapterPosition)
    }

    /**
     * an implementation of AltChipGroup.OnChipLongClickListener.
     */
    override fun onChipLongClick(group: AltChipGroup, chip: Chip, holder: ChipHolder, position: Int): Boolean {
        val adapterPosition = group.getTag(KEY_ADAPTER_POSITION) as Int
        return onChipLongClickListener?.onChipLongClick(chip, holder, position, adapterPosition) ?: false
    }

    override fun setAdapter(adapter: RecyclerView.Adapter<*>?) {
        super.setAdapter(adapter)
        adapter ?: return
        adapter as Adapter
        val factory = adapter.getChipFactory()
        if (factory != null) {
            chipMeasure.initWithFactory(factory)
            chipsPool.factory = factory
        }

    }

    /**
     * this must be only called in Adapter#onCreateViewHolder(ViewGroup, Int).
     */
    internal fun onViewHolderCreated(holder: ViewHolder) {
        holder.getAltChipGroup().apply {
            setShared(chipsPool, chipMeasure)
            setOnChipClickListener(this@RecyclerViewWithAltChipGroup)
            setOnChipLongClickListener(this@RecyclerViewWithAltChipGroup)
        }
    }

    fun setOnChipClickListener(listener: (chip: Chip, holder: ChipHolder, chipPosition: Int, itemPosition: Int) -> Unit) {
        onChipClickListener = object: OnChipClickListener {
            override fun onChipClick(chip: Chip, holder: ChipHolder, chipPosition: Int, itemPosition: Int)
            = listener(chip, holder, chipPosition, itemPosition)
        }
    }

    fun setOnChipLongClickListener(listener: (chip: Chip, holder: ChipHolder, chipPosition: Int, itemPosition: Int) -> Boolean) {
        onChipLongClickListener = object: OnChipLongClickListener {
            override fun onChipLongClick(chip: Chip, holder: ChipHolder, chipPosition: Int, itemPosition: Int): Boolean
                    = listener(chip, holder, chipPosition, itemPosition)
        }
    }

    fun setOnChipClickListener(listener: OnChipClickListener) {
        onChipClickListener = listener
    }

    fun setOnChipLongClickListener(listener: OnChipLongClickListener) {
        onChipLongClickListener = listener
    }

    companion object {
        // use resource id as a key of View's tag.
        private const val KEY_ADAPTER_POSITION = R.integer.alt_chip_group_tag_key_item_adapter_position
    }

    interface OnChipClickListener {
        fun onChipClick(chip: Chip, holder: ChipHolder, chipPosition: Int, itemPosition: Int)
    }

    interface OnChipLongClickListener {

        /**
         * @return true if the callback consumed the long click, false otherwise.
         */
        fun onChipLongClick(chip: Chip, holder: ChipHolder, chipPosition: Int, itemPosition: Int): Boolean
    }

    abstract class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        abstract fun getAltChipGroup(): AltChipGroup
    }

    abstract class Adapter<VH: ViewHolder>: RecyclerView.Adapter<VH>() {

        private var owner: RecyclerViewWithAltChipGroup? = null

        override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
            super.onAttachedToRecyclerView(recyclerView)
            if (recyclerView is RecyclerViewWithAltChipGroup) {
                owner = recyclerView
            }
        }

        override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
            super.onDetachedFromRecyclerView(recyclerView)
            owner = null
        }

        final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val holder = onCreateViewHolderAlt(parent, viewType)
            owner?.onViewHolderCreated(holder)
            return holder
        }

        final override fun onBindViewHolder(holder: VH, position: Int) {
            holder.getAltChipGroup().setTag(KEY_ADAPTER_POSITION, holder.adapterPosition)
            onBindViewHolderAlt(holder, position)
        }

        /**
         * An alternative method of RecyclerView.Adapter#onCreateViewHolder(ViewGroup, Int).
         */
        abstract fun onCreateViewHolderAlt(parent: ViewGroup, viewType: Int): VH

        /**
         * An alternative method of RecyclerView.Adapter#onBindViewHolder(RecyclerView.ViewHolder, Int).
         */
        abstract fun onBindViewHolderAlt(holder: VH, position: Int)

        /**
         * Return a custom chip factory, or null.
         */
        open fun getChipFactory(): ChipFactory? = null
    }
}
