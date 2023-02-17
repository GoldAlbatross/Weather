package com.example.earthsweather.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.earthsweather.R
import com.example.earthsweather.okhttp.Location

class CitiesAdapter(private val clickListener: LocationClickListener) : RecyclerView.Adapter<CitiesViewHolder>() {
    var locations = ArrayList<Location>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitiesViewHolder {
        return CitiesViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.location_name, parent, false))
    }
    override fun getItemCount(): Int = locations.size

    override fun onBindViewHolder(holder: CitiesViewHolder, position: Int) {
        holder.bind(locations[position])
        holder.itemView.setOnClickListener { clickListener.onLocationClick(locations[position]) }
    }

    fun interface LocationClickListener {
        fun onLocationClick(location: Location)
    }
}


class CitiesViewHolder(v: View) : RecyclerView.ViewHolder(v) {
    var name: TextView = v.findViewById(R.id.city_name)
    fun bind(location: Location) {
        name.setText("${location.name} (${location.country})")
    }
}


