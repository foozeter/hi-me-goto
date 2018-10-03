package com.hayashihideo.himegoto

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup

class TestRecyclerView(context: Context, attrs: AttributeSet): RecyclerView(context, attrs) {

    class Item(context: Context): View(context) {

        var position = -1

        val paint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL
        }

        override fun onDraw(canvas: Canvas) {
            val size = height * 0.05f
            canvas.drawRect(0f, 0f, width.toFloat(), size, paint)
        }

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            Log.d("mylog", "ATTACHED (pos=$position, hasParent=${parent != null})")
        }

        override fun onDetachedFromWindow() {
            super.onDetachedFromWindow()
            Log.d("mylog", "@@@@@@@@@@@@@@ DETACHED (pos=$position, hasParent=${parent != null})")
        }
    }

    class Holder(view: View): RecyclerView.ViewHolder(view) {
        fun bind(position: Int) {
            val item = itemView as Item
            Log.d("mylog", "######## BIND (oldPos=${item.position}, newPos=${position}")
            item.position = position
        }
    }

    class Adapter: RecyclerView.Adapter<Holder>() {

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): Holder {
            val view = Item(p0.context)
            view.layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 500)
            return Holder(view)
        }

        override fun getItemCount(): Int {
            return 1000
        }

        override fun onBindViewHolder(p0: Holder, p1: Int) {
            p0.bind(p1)
        }
    }

    init {
        layoutManager = LinearLayoutManager(context)
        adapter = Adapter()
    }
}
