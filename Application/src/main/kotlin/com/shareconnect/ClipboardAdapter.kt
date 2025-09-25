package com.shareconnect

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shareconnect.utils.ClipboardItem
import java.text.SimpleDateFormat
import java.util.*

class ClipboardAdapter(
    private val clipboardItems: List<ClipboardItem>,
    private val onItemClick: (ClipboardItem) -> Unit
) : RecyclerView.Adapter<ClipboardAdapter.ClipboardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClipboardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_clipboard, parent, false)
        return ClipboardViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClipboardViewHolder, position: Int) {
        val item = clipboardItems[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = clipboardItems.size

    inner class ClipboardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val clipboardItemIcon: ImageView = itemView.findViewById(R.id.clipboardItemIcon)
        private val clipboardItemText: TextView = itemView.findViewById(R.id.clipboardItemText)
        private val clipboardItemTime: TextView = itemView.findViewById(R.id.clipboardItemTime)
        private val clipboardItemTypeIcon: ImageView = itemView.findViewById(R.id.clipboardItemTypeIcon)

        fun bind(item: ClipboardItem) {
            clipboardItemText.text = item.text

            // Format timestamp
            val formatter = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            val timeAgo = getTimeAgo(item.timestamp)
            clipboardItemTime.text = timeAgo

            // Set icon based on content type
            if (item.isUrl) {
                when {
                    item.text.contains("youtube.com") || item.text.contains("youtu.be") -> {
                        clipboardItemIcon.setImageResource(android.R.drawable.ic_media_play)
                        clipboardItemTypeIcon.setImageResource(android.R.drawable.ic_media_play)
                    }
                    item.text.contains("vimeo.com") -> {
                        clipboardItemIcon.setImageResource(android.R.drawable.ic_media_play)
                        clipboardItemTypeIcon.setImageResource(android.R.drawable.ic_media_play)
                    }
                    item.text.contains("twitch.tv") -> {
                        clipboardItemIcon.setImageResource(android.R.drawable.ic_media_play)
                        clipboardItemTypeIcon.setImageResource(android.R.drawable.ic_media_play)
                    }
                    item.text.contains("reddit.com") || item.text.contains("twitter.com") ||
                    item.text.contains("instagram.com") || item.text.contains("facebook.com") -> {
                        clipboardItemIcon.setImageResource(android.R.drawable.ic_menu_share)
                        clipboardItemTypeIcon.setImageResource(android.R.drawable.ic_menu_share)
                    }
                    item.text.startsWith("magnet:") -> {
                        clipboardItemIcon.setImageResource(android.R.drawable.ic_menu_upload)
                        clipboardItemTypeIcon.setImageResource(android.R.drawable.ic_menu_upload)
                    }
                    else -> {
                        clipboardItemIcon.setImageResource(android.R.drawable.ic_menu_view)
                        clipboardItemTypeIcon.setImageResource(android.R.drawable.ic_menu_view)
                    }
                }
            } else {
                clipboardItemIcon.setImageResource(android.R.drawable.ic_menu_edit)
                clipboardItemTypeIcon.setImageResource(android.R.drawable.ic_menu_edit)
            }

            // Handle click
            itemView.setOnClickListener {
                onItemClick(item)
            }
        }

        private fun getTimeAgo(timestamp: Long): String {
            val now = System.currentTimeMillis()
            val diff = now - timestamp

            return when {
                diff < 60 * 1000 -> "Just now"
                diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} minutes ago"
                diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} hours ago"
                diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)} days ago"
                else -> {
                    val formatter = SimpleDateFormat("MMM dd", Locale.getDefault())
                    formatter.format(Date(timestamp))
                }
            }
        }
    }
}