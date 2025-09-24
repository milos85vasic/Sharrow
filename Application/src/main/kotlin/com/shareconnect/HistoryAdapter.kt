package com.shareconnect

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.shareconnect.database.HistoryItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(private val listener: OnHistoryItemClickListener) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {
    private val historyItems: MutableList<HistoryItem> = ArrayList()

    interface OnHistoryItemClickListener {
        fun onResendClick(item: HistoryItem)
        fun onDeleteClick(item: HistoryItem)
    }

    fun updateHistoryItems(items: List<HistoryItem>) {
        historyItems.clear()
        historyItems.addAll(items)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = historyItems[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return historyItems.size
    }

    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewTitle: TextView = itemView.findViewById(R.id.textViewTitle)
        private val textViewDescription: TextView = itemView.findViewById(R.id.textViewDescription)
        private val textViewUrl: TextView = itemView.findViewById(R.id.textViewUrl)
        private val textViewServiceProvider: TextView = itemView.findViewById(R.id.textViewServiceProvider)
        private val textViewType: TextView = itemView.findViewById(R.id.textViewType)
        private val textViewProfile: TextView = itemView.findViewById(R.id.textViewProfile)
        private val textViewTimestamp: TextView = itemView.findViewById(R.id.textViewTimestamp)
        private val textViewStatus: TextView = itemView.findViewById(R.id.textViewStatus)
        private val textViewServiceType: TextView = itemView.findViewById(R.id.textViewServiceType)
        private val buttonResend: MaterialButton = itemView.findViewById(R.id.buttonResend)
        private val buttonDelete: MaterialButton = itemView.findViewById(R.id.buttonDelete)

        fun bind(item: HistoryItem) {
            textViewTitle.text = item.title ?: "Unknown Title"
            textViewDescription.text = item.description ?: ""
            textViewUrl.text = item.url
            textViewServiceProvider.text = item.serviceProvider
            textViewType.text = item.type
            textViewServiceType.text = item.serviceType ?: itemView.context.getString(R.string.metube)

            textViewProfile.text = if (item.profileName != null && item.profileName!!.isNotEmpty()) {
                item.profileName
            } else {
                itemView.context.getString(R.string.not_sent)
            }

            // Format timestamp
            val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            textViewTimestamp.text = sdf.format(Date(item.timestamp))

            // Set status
            if (item.profileId.isNullOrEmpty()) {
                textViewStatus.setText(R.string.not_sent)
                textViewStatus.setBackgroundResource(R.drawable.status_background)
            } else {
                textViewStatus.text = if (item.isSentSuccessfully) "Sent" else "Failed"
                if (item.isSentSuccessfully) {
                    textViewStatus.setBackgroundResource(R.drawable.status_background)
                } else {
                    // Create a red status background for failed items
                    textViewStatus.setBackgroundResource(R.drawable.tag_background)
                }
            }

            // Show/hide description based on whether it's available
            textViewDescription.visibility = if (item.description.isNullOrEmpty()) View.GONE else View.VISIBLE

            // Set button listeners
            buttonResend.setOnClickListener {
                listener.onResendClick(item)
            }

            buttonDelete.setOnClickListener {
                listener.onDeleteClick(item)
            }
        }
    }
}