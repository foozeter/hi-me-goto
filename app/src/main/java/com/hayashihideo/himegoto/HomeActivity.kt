package com.hayashihideo.himegoto

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_home.*
import java.util.*

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setSupportActionBar(appbar)

        list.layoutManager = LinearLayoutManager(this)
        list.adapter = Ad()
//
//        val specs = mutableListOf<CompactChipGroup.ChipSpec>()
//        for (i in 0 until data.size) {
//            specs.add(DefaultChipSpec(data[i]))
//        }
//        chip_group.setChipSpecs(specs)
//        chip_group.maxLines = 4
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.home_menu, menu)
        return true
    }
}

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

private class Ad: RecyclerView.Adapter<Vh>() {

    val specs = mutableListOf<List<CompactChipGroup.ChipSpec>>()

    init {
        for (i in 1..50) {
            val range = (0..10).random()
            val start = (0..(data.size-range)).random()
            val list = mutableListOf<CompactChipGroup.ChipSpec>()
            specs.add(list)
            for (j in start until (start+range)) {
                list.add(DefaultChipSpec(data[j]))
            }
        }
    }

    fun IntRange.random() = Random().nextInt((endInclusive+1)-start)+start

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): Vh {
        return Vh(LayoutInflater.from(p0.context)
                .inflate(R.layout.chunk_list_item, p0, false))
    }

    override fun getItemCount(): Int {
        return specs.size
    }

    override fun onBindViewHolder(p0: Vh, p1: Int) {
        p0.chipGroup.setChipSpecs(specs[p1])
    }
}

private class Vh(view: View): RecyclerView.ViewHolder(view) {
    val chipGroup: CompactChipGroup = view.findViewById(R.id.chip_group)
    init {
        chipGroup.maxLines = 2
    }
}
