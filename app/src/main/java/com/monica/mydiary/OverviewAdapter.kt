package com.monica.mydiary

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.monica.mydiary.database.Diary

class OverviewAdapter(private val context: Context): Adapter<OverviewAdapter.MyViewHolder>() {

    private var _diaries: List<Diary> = emptyList()

    class MyViewHolder(view: View): ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.item_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item, parent, false)

        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.textView.text = _diaries[position].title
        holder.textView.setOnClickListener {
            val action = OverviewFragmentDirections
                .actionOverviewFragmentToDetailFragment(_diaries[position].id)
            holder.textView.findNavController().navigate(action)
        }
    }

    override fun getItemCount(): Int {
        return _diaries.size
    }

    fun setData(diaries: List<Diary>) {
       _diaries = diaries
        notifyDataSetChanged()
    }
}