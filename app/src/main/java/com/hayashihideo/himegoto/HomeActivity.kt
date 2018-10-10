package com.hayashihideo.himegoto

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import android.widget.TextView
import com.hayashihideo.himegoto.altchipgroup.AltChipGroup
import com.hayashihideo.himegoto.altchipgroup.RecyclerViewWithAltChipGroup
import com.hayashihideo.himegoto.altchipgroup.ChipHolder
import kotlinx.android.synthetic.main.activity_home.*
import java.util.*

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setSupportActionBar(appbar)

        list.layoutManager = LinearLayoutManager(this)
        list.adapter = Ad()
        list.setOnChipClickListener { chip, holder, chipPosition, itemPosition ->
            Log.d("mylog", "chip was clicked! (chipPosition = $chipPosition, itemPosition = $itemPosition, label = ${holder.label})")
        }

        list.setOnChipLongClickListener { chip, holder, chipPosition, itemPosition ->
            Log.d("mylog", "chip was long clicked! (chipPosition = $chipPosition, itemPosition = $itemPosition, label = ${holder.label})")
            true
        }

        val holders = mutableListOf<List<ChipHolder>>()
        for (i in 1..500) {
            val range = (0..10).random()
            val start = (0..(data.size-range)).random()
            val list = mutableListOf<ChipHolder>()
            holders.add(list)
            for (j in start until (start+range)) {
                list.add(ChipHolder(data[j]))
            }
        }

        val chipGroup = findViewById<AltChipGroup>(R.id.chip_group)
        chipGroup.setChipHolders(holders[0])
        chipGroup.maxLines = 2
        chipGroup.layoutWithinBounds = true
        chipGroup.setOnChipClickListener { group, chip, holder, position ->
            Log.d("mylog", "chip was clicked! (position = $position, label = ${holder.label})")
        }

        var index = 1
        button.setOnClickListener {
            Log.d("mylog", "set ${holders[index].size} chips")
            chipGroup.setChipHolders(holders[index])
            ++index
            if (index == holders.size) index = 0
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return true
    }
}


fun IntRange.random() = Random().nextInt((endInclusive+1)-start)+start

private val data = listOf(
        "android studio",
        "super market fantasy",
        "mr.children",
        "the pillows",
        "snoopy",
        "hello kitty",
        "blouse drive monster",
        "center of universe",
        "i",
        "world's end",
        "i know you",
        "kind of love",
        "love",
        "verses",
        "atomic heart",
        "b-side",
        "bolero",
        "discovery",
        "here comes my love",
        "everything",
        "home",
        "himawari",
        "i love you",
        "it's a wonderful world",
        "micro",
        "macro",
        "q",
        "sense")

private class Ad: RecyclerViewWithAltChipGroup.Adapter<Vh>() {

    val holders = mutableListOf<List<ChipHolder>>()

    init {
        for (i in 1..500) {
            val range = (0..10).random()
            val start = (0..(data.size-range)).random()
            val list = mutableListOf<ChipHolder>()
            holders.add(list)
            for (j in start until (start+range)) {
                list.add(ChipHolder(data[j]))
            }
        }
    }

    override fun onCreateViewHolderAlt(parent: ViewGroup, viewType: Int): Vh {
        return Vh(LayoutInflater.from(parent.context)
                .inflate(R.layout.chunk_list_item, parent, false))
    }

    override fun getItemCount(): Int {
        return holders.size
    }

    override fun onBindViewHolderAlt(holder: Vh, position: Int) {
        holder.chipGroup.setChipHolders(holders[position])
        holder.title.text = "${holders[position].size} CHIPS"
    }
}

private class Vh(view: View): RecyclerViewWithAltChipGroup.ViewHolder(view) {
    val chipGroup: AltChipGroup = view.findViewById(R.id.chip_group)
    val title: TextView = view.findViewById(R.id.title)

    override fun getAltChipGroup(): AltChipGroup {
        return chipGroup
    }

    init {
        chipGroup.maxLines = 2
        chipGroup.horizontalGap =  24
        chipGroup.verticalGap = 24
        chipGroup.chipsMarginTop = 24
        chipGroup.chipsMarginBottom = 24
        chipGroup.chipsMarginStart = 24
        chipGroup.restCountBadgeMarginStart = 36
    }
}
