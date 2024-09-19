package com.ozzysimpson.project2

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DomainHistory : AppCompatActivity() {
    private lateinit var toolbar: MaterialToolbar
    private lateinit var domainList: RecyclerView
    private lateinit var empty: TextView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_domain_history)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get views
        toolbar = findViewById(R.id.toolbar)
        domainList = findViewById(R.id.domains)
        empty = findViewById(R.id.empty)

        // Get firebase auth instance
        firebaseAuth = FirebaseAuth.getInstance()

        // Get firebase database
        firebaseDatabase = FirebaseDatabase.getInstance()

        // Set toolbar back button
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // Get domains
        val domains = mutableListOf<String>()
        val domainRef = firebaseDatabase.getReference("History/${firebaseAuth.currentUser!!.uid}")
        domainRef.addValueEventListener (object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(
                    this@DomainHistory,
                    getString(R.string.home_historyFail),
                    Toast.LENGTH_LONG
                ).show()
            }
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                domains.clear()
                for (domain in dataSnapshot.children) {
                    val id = domain.key.toString()
                    val fullDomain = domain.value.toString()

                    // Filter by expiration
                    domains.add(fullDomain)
                }

                // Sort by expiration
                domains.sortBy { it }

                val adapter = DomainHistoryAdapter(domains) { domain ->
                    searchDomain(domain)
                }
                domainList.adapter = adapter
                domainList.layoutManager = LinearLayoutManager(this@DomainHistory)

                // Show empty message if no domains
                empty.isVisible = domains.isEmpty()
            }
        })

    }

    private fun searchDomain(domain: String) {
        // Go to DomainSearch activity with this domain passed in
        val intentActivity = Intent(this, DomainSearch::class.java)
        intentActivity.putExtra("SEARCH_TERM", domain)
        startActivity(intentActivity)
    }
}