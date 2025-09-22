package com.metubeshare;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.metubeshare.database.HistoryItem;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    private List<HistoryItem> historyItems;
    private OnHistoryItemClickListener listener;
    
    public interface OnHistoryItemClickListener {
        void onResendClick(HistoryItem item);
        void onDeleteClick(HistoryItem item);
    }
    
    public HistoryAdapter(OnHistoryItemClickListener listener) {
        this.historyItems = new ArrayList<>();
        this.listener = listener;
    }
    
    public void updateHistoryItems(List<HistoryItem> items) {
        this.historyItems.clear();
        this.historyItems.addAll(items);
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryItem item = historyItems.get(position);
        holder.bind(item);
    }
    
    @Override
    public int getItemCount() {
        return historyItems.size();
    }
    
    class HistoryViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewTitle;
        private TextView textViewUrl;
        private TextView textViewServiceProvider;
        private TextView textViewType;
        private TextView textViewProfile;
        private TextView textViewTimestamp;
        private TextView textViewStatus;
        private TextView textViewServiceType;
        private MaterialButton buttonResend;
        private MaterialButton buttonDelete;
        
        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewUrl = itemView.findViewById(R.id.textViewUrl);
            textViewServiceProvider = itemView.findViewById(R.id.textViewServiceProvider);
            textViewType = itemView.findViewById(R.id.textViewType);
            textViewProfile = itemView.findViewById(R.id.textViewProfile);
            textViewTimestamp = itemView.findViewById(R.id.textViewTimestamp);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
            textViewServiceType = itemView.findViewById(R.id.textViewServiceType);
            buttonResend = itemView.findViewById(R.id.buttonResend);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
        
        public void bind(HistoryItem item) {
            textViewTitle.setText(item.getTitle() != null ? item.getTitle() : "Unknown Title");
            textViewUrl.setText(item.getUrl());
            textViewServiceProvider.setText(item.getServiceProvider());
            textViewType.setText(item.getType());
            textViewServiceType.setText(item.getServiceType() != null ? item.getServiceType() : "MeTube");
            
            if (item.getProfileName() != null && !item.getProfileName().isEmpty()) {
                textViewProfile.setText(item.getProfileName());
            } else {
                textViewProfile.setText("Not sent");
            }
            
            // Format timestamp
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            textViewTimestamp.setText(sdf.format(new Date(item.getTimestamp())));
            
            // Set status
            if (item.getProfileId() == null || item.getProfileId().isEmpty()) {
                textViewStatus.setText("Not sent");
                textViewStatus.setBackgroundResource(R.drawable.status_background);
            } else {
                textViewStatus.setText(item.isSentSuccessfully() ? "Sent" : "Failed");
                if (item.isSentSuccessfully()) {
                    textViewStatus.setBackgroundResource(R.drawable.status_background);
                } else {
                    // Create a red status background for failed items
                    textViewStatus.setBackgroundResource(R.drawable.tag_background);
                }
            }
            
            // Set button listeners
            buttonResend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onResendClick(item);
                    }
                }
            });
            
            buttonDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onDeleteClick(item);
                    }
                }
            });
        }
    }
}