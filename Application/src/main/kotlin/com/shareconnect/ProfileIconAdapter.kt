package com.shareconnect

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProfileIconAdapter(
    private val context: Context,
    private val profiles: List<ServerProfile>,
    private val onProfileClick: (ServerProfile) -> Unit,
    private val onProfileLongClick: (ServerProfile) -> Boolean
) : RecyclerView.Adapter<ProfileIconAdapter.ProfileViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_profile_icon, parent, false)
        return ProfileViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        val profile = profiles[position]
        holder.bind(profile)
    }

    override fun getItemCount(): Int = profiles.size

    inner class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileIcon: ImageView = itemView.findViewById(R.id.profileIcon)
        private val profileName: TextView = itemView.findViewById(R.id.profileName)
        private val profileType: TextView = itemView.findViewById(R.id.profileType)
        private val defaultIndicator: ImageView = itemView.findViewById(R.id.defaultIndicator)
        private val lockIndicator: ImageView = itemView.findViewById(R.id.lockIndicator)

        fun bind(profile: ServerProfile) {
            profileName.text = profile.name ?: "Unnamed"
            profileType.text = profile.getServiceTypeName(context)

            // Set appropriate icon based on service type
            val iconRes = when (profile.serviceType) {
                ServerProfile.TYPE_METUBE -> R.drawable.ic_foreground
                ServerProfile.TYPE_YTDL -> android.R.drawable.ic_media_play
                ServerProfile.TYPE_TORRENT -> android.R.drawable.ic_menu_upload
                ServerProfile.TYPE_JDOWNLOADER -> android.R.drawable.ic_menu_save
                else -> android.R.drawable.ic_menu_share
            }
            profileIcon.setImageResource(iconRes)

            // Show default indicator if this is the default profile
            defaultIndicator.visibility = if (profile.isDefault) View.VISIBLE else View.GONE

            // Show lock indicator if profile has credentials
            lockIndicator.visibility = if (!profile.username.isNullOrEmpty() && !profile.password.isNullOrEmpty()) {
                View.VISIBLE
            } else {
                View.GONE
            }

            // Handle clicks
            itemView.setOnClickListener {
                onProfileClick(profile)
            }

            itemView.setOnLongClickListener {
                onProfileLongClick(profile)
            }
        }
    }
}