package com.shareconnect

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.shareconnect.database.Theme

class ThemeAdapter(private val listener: OnThemeSelectListener) :
    RecyclerView.Adapter<ThemeAdapter.ThemeViewHolder>() {
    private val themes: MutableList<Theme> = ArrayList()

    interface OnThemeSelectListener {
        fun onThemeSelected(theme: Theme)
    }

    fun updateThemes(themes: List<Theme>) {
        this.themes.clear()
        this.themes.addAll(themes)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThemeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_theme, parent, false)
        return ThemeViewHolder(view)
    }

    override fun onBindViewHolder(holder: ThemeViewHolder, position: Int) {
        val theme = themes[position]
        holder.bind(theme)
    }

    override fun getItemCount(): Int {
        return themes.size
    }

    inner class ThemeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewThemeName: TextView = itemView.findViewById(R.id.textViewThemeName)
        private val textViewThemeVariant: TextView = itemView.findViewById(R.id.textViewThemeVariant)
        private val buttonSelectTheme: MaterialButton = itemView.findViewById(R.id.buttonSelectTheme)

        fun bind(theme: Theme) {
            textViewThemeName.text = theme.name
            textViewThemeVariant.text = if (theme.isDarkMode) {
                itemView.context.getString(R.string.dark)
            } else {
                itemView.context.getString(R.string.light)
            }

            buttonSelectTheme.setOnClickListener {
                listener.onThemeSelected(theme)
            }
        }
    }
}