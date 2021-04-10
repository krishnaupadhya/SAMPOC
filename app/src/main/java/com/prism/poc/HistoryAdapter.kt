package com.prism.poc

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.prism.poc.db.HistoryLocation
import com.prism.poc.locationhistory.LocationHistory

internal class HistoryAdapter(private var historyList: List<HistoryLocation>) :
    RecyclerView.Adapter<HistoryAdapter.MyViewHolder>() {


    internal inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var title: TextView = view.findViewById(R.id.title)
    }

    @NonNull
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_history_item, parent, false)
        return MyViewHolder(itemView)
    }
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val location = historyList[position]
        holder.title.text = location.address
    }
    override fun getItemCount(): Int {
        return historyList.size
    }
}