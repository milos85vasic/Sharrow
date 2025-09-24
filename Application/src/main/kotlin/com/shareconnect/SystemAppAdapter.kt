package com.shareconnect

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SystemAppAdapter(
    private val context: Context,
    private val apps: List<ResolveInfo>,
    private val onAppClick: (ResolveInfo) -> Unit
) : RecyclerView.Adapter<SystemAppAdapter.AppViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_system_app, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = apps[position]
        holder.bind(app)
    }

    override fun getItemCount(): Int = apps.size

    inner class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val appIcon: ImageView = itemView.findViewById(R.id.appIcon)
        private val appName: TextView = itemView.findViewById(R.id.appName)

        fun bind(app: ResolveInfo) {
            val pm = context.packageManager

            // Set app icon
            appIcon.setImageDrawable(app.loadIcon(pm))

            // Set app name
            appName.text = app.loadLabel(pm).toString()

            // Handle click
            itemView.setOnClickListener {
                onAppClick(app)
            }
        }
    }
}