package com.ozzysimpson.project2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import java.text.DateFormat.getDateInstance

class DomainAdapter(private val domains: List<Domain>, val onItemClick: (Domain) -> Unit): RecyclerView.Adapter<DomainAdapter.ViewHolder>() {
    class ViewHolder(rootLayout: View): RecyclerView.ViewHolder(rootLayout) {
        val domain: TextView=rootLayout.findViewById(R.id.domain)
        val registered: TextView=rootLayout.findViewById(R.id.registered)
        val expiration: TextView=rootLayout.findViewById(R.id.expiration)
        val renew: MaterialButton=rootLayout.findViewById(R.id.renew)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        val rootLayout: View = layoutInflater.inflate(R.layout.domain_registered, parent, false)
        return ViewHolder(rootLayout)
    }

    override fun getItemCount(): Int {
        return domains.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentDomain = domains[position]
        "${currentDomain.sld}.${currentDomain.tld}".also { holder.domain.text = it }

        // Format registered (from ms) to human readable date
        val formatter = getDateInstance()
        holder.registered.text = holder.itemView.context.getString(R.string.domain_registered, formatter.format(currentDomain.registration))

        // Format expiration (from ms) to human readable date
        holder.expiration.text = holder.itemView.context.getString(R.string.domain_expires, formatter.format(currentDomain.expiration))

        // If expiration is less than 30 days away, change text color
        if (currentDomain.expiration!! - System.currentTimeMillis() < 2592000000) {
            holder.expiration.setTextColor(holder.itemView.context.getColor(R.color.red))
        }

        // If expired, hide renew button, change text
        if (currentDomain.expiration < System.currentTimeMillis()) {
            holder.renew.isVisible = false
            holder.expiration.text = holder.itemView.context.getString(R.string.domain_expired, formatter.format(currentDomain.expiration))
        }

        // Handle renew button
        holder.renew.setOnClickListener {
            onItemClick(currentDomain)
        }
    }
}