package com.example.homestyler

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.homestyler.model.DateItem

class HistoryAdapter(private val dates: MutableList<DateItem>,
                     private val onAction: (DateItem, Action) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val ITEM_VIEW_TYPE_DATE = 0
        private const val ITEM_VIEW_TYPE_EMPTY_STATE = 1
    }

    inner class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
    }

    inner class EmptyStateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val emptyStateImageView: ImageView = itemView.findViewById(R.id.emptyStateImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_VIEW_TYPE_DATE) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.single_history_item, parent, false)
            DateViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.empty_state_layout, parent, false)
            EmptyStateViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DateViewHolder) {
            val dateItem = dates[position]
            holder.dateTextView.text = dateItem.date
            holder.itemView.setOnClickListener {
                onAction(dateItem, Action.Click)
            }
            holder.itemView.setOnLongClickListener {
                onAction(dateItem, Action.LongPress)
                true // Return true to indicate the long press was handled
            }
        } else if (holder is EmptyStateViewHolder) {
            // Set the empty state text and drawable here
            holder.emptyStateImageView.setImageResource(R.drawable.planet)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (dates.isEmpty()) ITEM_VIEW_TYPE_EMPTY_STATE else ITEM_VIEW_TYPE_DATE
    }

    override fun getItemCount() = if (dates.isEmpty()) 1 else dates.size
}
