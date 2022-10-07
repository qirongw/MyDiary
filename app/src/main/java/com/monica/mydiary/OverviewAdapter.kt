package com.monica.mydiary

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.monica.mydiary.database.Diary
import java.text.SimpleDateFormat

class OverviewAdapter(private val context: Context,
                      private val viewModel: DiariesViewModel,
                    private val lifecycleOwner: LifecycleOwner): Adapter<OverviewAdapter.MyViewHolder>() {

    companion object {
        val dateFormatter = SimpleDateFormat("MMM d yyyy")
    }

    private var _diaries: List<Diary> = emptyList()

    class MyViewHolder(view: View): ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.item_title)
        val date: TextView = view.findViewById(R.id.date)
        val content: TextView = view.findViewById(R.id.content)
        val image: ImageView = view.findViewById(R.id.item_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item, parent, false)

        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.title.text = _diaries[position].title
        holder.content.text = _diaries[position].content
        holder.date.text = dateFormatter.format(_diaries[position].date)

        holder.itemView.setOnClickListener {
            val action = OverviewFragmentDirections
                .actionOverviewFragmentToDetailFragment(_diaries[position].id)
            holder.title.findNavController().navigate(action)
        }

        viewModel.getImageFromDiary(_diaries[position]).observe(lifecycleOwner) {_bitmap ->
            if (_bitmap != null) {
                holder.image.setImageBitmap(_bitmap)
                holder.image.visibility = View.VISIBLE
            } else {
                holder.image.visibility = View.GONE
            }
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