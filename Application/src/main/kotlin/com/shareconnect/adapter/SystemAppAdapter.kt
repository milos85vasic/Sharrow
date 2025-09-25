package com.shareconnect.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shareconnect.R
import com.shareconnect.utils.SystemAppDetector

class SystemAppAdapter(
    private val onAppClick: (SystemAppDetector.AppInfo) -> Unit
) : ListAdapter<SystemAppDetector.AppInfo, SystemAppAdapter.AppViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_system_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind(getItem(position), onAppClick)
    }

    class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val appIcon: ImageView = itemView.findViewById(R.id.appIcon)
        private val appName: TextView = itemView.findViewById(R.id.appName)
        private val appPackage: TextView = itemView.findViewById(R.id.appPackage)

        fun bind(appInfo: SystemAppDetector.AppInfo, onAppClick: (SystemAppDetector.AppInfo) -> Unit) {
            appIcon.setImageDrawable(appInfo.icon)
            appName.text = appInfo.appName
            appPackage.text = appInfo.packageName

            itemView.setOnClickListener {
                onAppClick(appInfo)
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<SystemAppDetector.AppInfo>() {
        override fun areItemsTheSame(
            oldItem: SystemAppDetector.AppInfo,
            newItem: SystemAppDetector.AppInfo
        ): Boolean {
            return oldItem.packageName == newItem.packageName
        }

        override fun areContentsTheSame(
            oldItem: SystemAppDetector.AppInfo,
            newItem: SystemAppDetector.AppInfo
        ): Boolean {
            return oldItem == newItem
        }
    }
}