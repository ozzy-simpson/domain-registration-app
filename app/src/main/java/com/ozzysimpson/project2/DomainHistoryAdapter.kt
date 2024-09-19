package com.ozzysimpson.project2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class DomainHistoryAdapter(private val domains: List<String>, val onItemClick: (String) -> Unit): RecyclerView.Adapter<DomainHistoryAdapter.ViewHolder>() {
    class ViewHolder(rootLayout: View): RecyclerView.ViewHolder(rootLayout) {
        val domain: TextView=rootLayout.findViewById(R.id.domain)
        val search: MaterialButton=rootLayout.findViewById(R.id.search)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        val rootLayout: View = layoutInflater.inflate(R.layout.domain_search, parent, false)
        return ViewHolder(rootLayout)
    }

    override fun getItemCount(): Int {
        return domains.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentDomain = domains[position]
        holder.domain.text = currentDomain

        // Handle renew button
        holder.search.setOnClickListener {
            onItemClick(currentDomain)
        }
    }
}