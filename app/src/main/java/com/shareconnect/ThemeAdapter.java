package com.shareconnect;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.shareconnect.database.Theme;
import java.util.ArrayList;
import java.util.List;

public class ThemeAdapter extends RecyclerView.Adapter<ThemeAdapter.ThemeViewHolder> {
    private List<Theme> themes;
    private OnThemeSelectListener listener;
    
    public interface OnThemeSelectListener {
        void onThemeSelected(Theme theme);
    }
    
    public ThemeAdapter(OnThemeSelectListener listener) {
        this.themes = new ArrayList<>();
        this.listener = listener;
    }
    
    public void updateThemes(List<Theme> themes) {
        this.themes.clear();
        this.themes.addAll(themes);
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public ThemeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_theme, parent, false);
        return new ThemeViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ThemeViewHolder holder, int position) {
        Theme theme = themes.get(position);
        holder.bind(theme);
    }
    
    @Override
    public int getItemCount() {
        return themes.size();
    }
    
    class ThemeViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewThemeName;
        private TextView textViewThemeVariant;
        private MaterialButton buttonSelectTheme;
        
        public ThemeViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewThemeName = itemView.findViewById(R.id.textViewThemeName);
            textViewThemeVariant = itemView.findViewById(R.id.textViewThemeVariant);
            buttonSelectTheme = itemView.findViewById(R.id.buttonSelectTheme);
        }
        
        public void bind(Theme theme) {
            textViewThemeName.setText(theme.getName());
            textViewThemeVariant.setText(theme.isDarkMode() ? "Dark" : "Light");
            
            buttonSelectTheme.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onThemeSelected(theme);
                    }
                }
            });
        }
    }
}