package com.shareconnect;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {
    private List<ServerProfile> profiles;
    private OnProfileClickListener listener;
    private ProfileManager profileManager;

    public interface OnProfileClickListener {
        void onProfileClick(ServerProfile profile);
        void onSetDefaultClick(ServerProfile profile);
        void onDeleteClick(ServerProfile profile);
    }

    public ProfileAdapter(List<ServerProfile> profiles, OnProfileClickListener listener, ProfileManager profileManager) {
        this.profiles = profiles;
        this.listener = listener;
        this.profileManager = profileManager;
    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_profile, parent, false);
        return new ProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        ServerProfile profile = profiles.get(position);
        holder.bind(profile);
    }

    @Override
    public int getItemCount() {
        return profiles.size();
    }

    class ProfileViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewProfileName;
        private TextView textViewProfileUrl;
        private TextView textViewDefault;
        private ImageView imageViewServiceType;
        private TextView textViewServiceType;
        private MaterialButton buttonSetDefault;
        private MaterialButton buttonDelete;

        public ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewProfileName = itemView.findViewById(R.id.textViewProfileName);
            textViewProfileUrl = itemView.findViewById(R.id.textViewProfileUrl);
            textViewDefault = itemView.findViewById(R.id.textViewDefault);
            imageViewServiceType = itemView.findViewById(R.id.imageViewServiceType);
            textViewServiceType = itemView.findViewById(R.id.textViewServiceType);
            buttonSetDefault = itemView.findViewById(R.id.buttonSetDefault);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }

        public void bind(ServerProfile profile) {
            textViewProfileName.setText(profile.getName());
            textViewProfileUrl.setText(profile.getUrl() + ":" + profile.getPort());
            
            // Set service type icon and text
            setServiceTypeIcon(profile);
            textViewServiceType.setText(profile.getServiceTypeName(itemView.getContext()));
            
            // Check if this is the default profile
            ServerProfile defaultProfile = profileManager.getDefaultProfile();
            boolean isDefault = defaultProfile != null && defaultProfile.getId().equals(profile.getId());
            textViewDefault.setVisibility(isDefault ? View.VISIBLE : View.GONE);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onProfileClick(profile);
                    }
                }
            });

            buttonSetDefault.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onSetDefaultClick(profile);
                    }
                }
            });

            buttonDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onDeleteClick(profile);
                    }
                }
            });
        }
        
        private void setServiceTypeIcon(ServerProfile profile) {
            switch (profile.getServiceType()) {
                case ServerProfile.TYPE_METUBE:
                    imageViewServiceType.setImageResource(R.drawable.ic_service_metube);
                    break;
                case ServerProfile.TYPE_TORRENT:
                    imageViewServiceType.setImageResource(R.drawable.ic_service_torrent);
                    break;
                case ServerProfile.TYPE_JDOWNLOADER:
                    imageViewServiceType.setImageResource(R.drawable.ic_service_jdownloader);
                    break;
                default:
                    imageViewServiceType.setImageResource(R.drawable.ic_service_metube);
                    break;
            }
        }
    }
}