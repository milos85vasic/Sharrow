package com.shareconnect

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class ProfileAdapter(
    private val profiles: List<ServerProfile>,
    private val listener: OnProfileClickListener,
    private val profileManager: ProfileManager
) : RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder>() {

    interface OnProfileClickListener {
        fun onProfileClick(profile: ServerProfile)
        fun onSetDefaultClick(profile: ServerProfile)
        fun onDeleteClick(profile: ServerProfile)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_profile, parent, false)
        return ProfileViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        val profile = profiles[position]
        holder.bind(profile)
    }

    override fun getItemCount(): Int {
        return profiles.size
    }

    inner class ProfileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewProfileName: TextView = itemView.findViewById(R.id.textViewProfileName)
        private val textViewProfileUrl: TextView = itemView.findViewById(R.id.textViewProfileUrl)
        private val imageViewDefault: ImageView = itemView.findViewById(R.id.imageViewDefault)
        private val imageViewAuthenticated: ImageView = itemView.findViewById(R.id.imageViewAuthenticated)
        private val imageViewServiceType: ImageView = itemView.findViewById(R.id.imageViewServiceType)
        private val textViewServiceType: TextView = itemView.findViewById(R.id.textViewServiceType)
        private val buttonSetDefault: MaterialButton = itemView.findViewById(R.id.buttonSetDefault)
        private val buttonDelete: MaterialButton = itemView.findViewById(R.id.buttonDelete)

        fun bind(profile: ServerProfile) {
            textViewProfileName.text = profile.name
            textViewProfileUrl.text = profile.url + ":" + profile.port

            // Set service type icon and text
            setServiceTypeIcon(profile)
            textViewServiceType.text = profile.getServiceTypeName(itemView.context)

            // Check if this is the default profile
            val defaultProfile = profileManager.defaultProfile()
            val isDefault = defaultProfile != null && defaultProfile.id == profile.id
            imageViewDefault.visibility = if (isDefault) View.VISIBLE else View.GONE

            // Check if this profile has authentication
            val hasAuth = !profile.username.isNullOrEmpty() && !profile.password.isNullOrEmpty()
            imageViewAuthenticated.visibility = if (hasAuth) View.VISIBLE else View.GONE

            itemView.setOnClickListener {
                listener.onProfileClick(profile)
            }

            buttonSetDefault.setOnClickListener {
                listener.onSetDefaultClick(profile)
            }

            buttonDelete.setOnClickListener {
                listener.onDeleteClick(profile)
            }
        }

        private fun setServiceTypeIcon(profile: ServerProfile) {
            when (profile.serviceType) {
                ServerProfile.TYPE_METUBE -> imageViewServiceType.setImageResource(R.drawable.ic_service_metube)
                ServerProfile.TYPE_TORRENT -> imageViewServiceType.setImageResource(R.drawable.ic_service_torrent)
                ServerProfile.TYPE_JDOWNLOADER -> imageViewServiceType.setImageResource(R.drawable.ic_service_jdownloader)
                else -> imageViewServiceType.setImageResource(R.drawable.ic_service_metube)
            }
        }
    }
}