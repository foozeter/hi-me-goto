package com.hayashihideo.himegoto

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import android.widget.TextView
import com.hayashihideo.himegoto.compactchipgroup.ChipHolder
import com.hayashihideo.himegoto.compactchipgroup.CompactChipGroup
import com.hayashihideo.himegoto.compactchipgroup.recyclerview.AdapterWithCCG
import com.hayashihideo.himegoto.compactchipgroup.recyclerview.CCGHolder
import kotlinx.android.synthetic.main.activity_home.*
import java.util.*

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setSupportActionBar(appbar)

        list.layoutManager = LinearLayoutManager(this)
        list.adapter = Ad()

//        val holders = mutableListOf<ChipHolder>()
//        for (i in 0 until data.size) {
//            holders.add(Ch(data[i]))
//        }

//        val holders = mutableListOf<ChipHolder>()
//        val labels = listOf("i know you", "kind of love", "love", "verses")
//        labels.forEach { holders.add(Ch(it)) }






//        val holders = mutableListOf<List<ChipHolder>>()
//        for (i in 1..500) {
//            val range = (0..10).random()
//            val start = (0..(data.size-range)).random()
//            val list = mutableListOf<ChipHolder>()
//            holders.add(list)
//            for (j in start until (start+range)) {
//                list.add(Ch(data[j]))
//            }
//        }
//
//        val chipGroup = findViewById<CompactChipGroup>(R.id.chip_group)
//        chipGroup.setChipHolders(holders[0])
//        chipGroup.maxLines = 2
//
//        var index = 1
//        button.setOnClickListener {
//            chipGroup.setChipHolders(holders[index])
//            ++index
//            if (index == holders.size) index = 0
//        }
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

private class Ad: AdapterWithCCG<Vh>() {

    val holders = mutableListOf<List<ChipHolder>>()

    init {
        for (i in 1..500) {
            val range = (0..10).random()
            val start = (0..(data.size-range)).random()
            val list = mutableListOf<ChipHolder>()
            holders.add(list)
            for (j in start until (start+range)) {
                list.add(Ch(data[j]))
            }
        }
    }

    override fun onMakeViewHolder(parent: ViewGroup, viewType: Int): Vh {
        return Vh(LayoutInflater.from(parent.context)
                .inflate(R.layout.chunk_list_item, parent, false))
    }

    override fun getItemCount(): Int {
        return holders.size
    }

    override fun onBindViewHolder(p0: Vh, p1: Int) {
        p0.chipGroup.setChipHolders(holders[p1])
        p0.title.text = "${holders[p1].size} CHIPS"
    }
}

private class Vh(view: View): RecyclerView.ViewHolder(view), CCGHolder {
    val chipGroup: CompactChipGroup = view.findViewById(R.id.chip_group)
    val title: TextView = view.findViewById(R.id.title)

    override fun getCcg(): CompactChipGroup {
        return chipGroup
    }

    init {
        chipGroup.maxLines = 2
    }
}

private class Ch(private val label: String): ChipHolder() {
    override fun getLabel(): String = label
}

//fun test(context: Context) {
//    val pool = ChipsPool(context)
//    val m_a_1 = ChipsManager()
//    val m_a_2 = m_a_1
//    val m_b = ChipsManager()
//    Log.d("mylog", "pool.register(m_a_1) == ${pool.register(m_a_1)} (expected 'true')")
//    Log.d("mylog", "pool.register(m_a_1) == ${pool.register(m_a_1)} (expected 'false')")
//    Log.d("mylog", "pool.register(m_a_2) == ${pool.register(m_a_2)} (expected 'false')")
//    Log.d("mylog", "pool.register(m_b) == ${pool.register(m_b)} (expected 'true')")
//
//    Log.d("mylog", "pool.unregister(m_a_1) == ${pool.unregister(m_a_1)} (expected 'true')")
//    Log.d("mylog", "pool.unregister(m_b) == ${pool.unregister(m_b)} (expected 'true')")
//    Log.d("mylog", "pool.unregister(m_b) == ${pool.unregister(m_b)} (expected 'false')")
//    Log.d("mylog", "pool.unregister(m_a_2) == ${pool.unregister(m_a_2)} (expected 'false')")
//}
