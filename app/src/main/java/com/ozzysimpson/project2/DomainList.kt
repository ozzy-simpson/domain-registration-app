package com.ozzysimpson.project2

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
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

class DomainList : AppCompatActivity() {
    private lateinit var toolbar: MaterialToolbar
    private lateinit var domainList: RecyclerView
    private lateinit var empty: TextView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_domain_list)
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

        // Get intent extra
        val filter = intent.getStringExtra("DOMAINS")

        // Set toolbar title
        when (filter) {
            "active" -> {
                toolbar.title = getString(R.string.home_registeredDomains)
                empty.text = getString(R.string.domain_active_empty)
            }
            "expiring" -> {
                toolbar.title = getString(R.string.home_expiringDomains)
                empty.text = getString(R.string.domain_expiring_empty)
            }
            "expired" -> {
                toolbar.title = getString(R.string.home_expiredDomains)
                empty.text = getString(R.string.domain_expired_empty)
            }
        }

        // Get domains
        val domains = mutableListOf<Domain>()
        val domainRef = firebaseDatabase.getReference("Domains/${firebaseAuth.currentUser!!.uid}")
        domainRef.addValueEventListener (object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(
                    this@DomainList,
                    getString(R.string.domain_fail),
                    Toast.LENGTH_LONG
                ).show()
            }
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                domains.clear()
                for (domain in dataSnapshot.children) {
                    val id = domain.key.toString()
                    val sld = domain.child("sld").value.toString()
                    val tld = domain.child("tld").value.toString()
                    val registration = domain.child("registration").value.toString().toLong()
                    val expiration = domain.child("expiration").value.toString().toLong()

                    // Filter by expiration
                    if (filter == "expiring" && expiration < System.currentTimeMillis() + 2592000000 && expiration > System.currentTimeMillis()) {
                        domains.add(Domain(sld, tld, registration, expiration, id=id))
                    }
                    else if (filter == "expired" && expiration < System.currentTimeMillis()) {
                        domains.add(Domain(sld, tld, registration, expiration, id=id))
                    }
                    else if (filter == "active" && expiration > System.currentTimeMillis()) {
                        domains.add(Domain(sld, tld, registration, expiration, id=id))
                    }
                }

                // Sort by expiration
                domains.sortBy { it.expiration }

                val adapter = DomainAdapter(domains) { domain ->
                    renewDomain(domain)
                }
                domainList.adapter = adapter
                domainList.layoutManager = LinearLayoutManager(this@DomainList)

                // Show empty message if no domains
                empty.isVisible = domains.isEmpty()
            }
        })

    }

    private fun renewDomain(domain: Domain) {
        // Show AlertDialog with confirmation
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.domain_renew_title))
            .setMessage(getString(R.string.domain_renew_desc, "${domain.sld}.${domain.tld}"))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                // Update expiration date (1 year from previous expiration date)
                val domainRef = firebaseDatabase.getReference("Domains/${firebaseAuth.currentUser!!.uid}/${domain.id}")
                domainRef.child("expiration").setValue(domain.expiration?.plus(31556952000))

                // Show success message
                Toast.makeText(this, getString(R.string.domain_renew_success, "${domain.sld}.${domain.tld}"), Toast.LENGTH_SHORT).show()

                // Refresh list

            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }
}