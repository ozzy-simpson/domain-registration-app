package com.ozzysimpson.project2

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat

class DomainSearch : AppCompatActivity() {
    private lateinit var toolbar: MaterialToolbar
    private lateinit var searchBtn: MaterialButton
    private lateinit var searchTerm: EditText
    private lateinit var loading: ProgressBar
    private lateinit var emptyView: TextView
    private lateinit var searchFirst: TextView
    private lateinit var results: ConstraintLayout
    private lateinit var price: TextView
    private lateinit var status: TextView
    private lateinit var domain: TextView
    private lateinit var register: MaterialButton
    private lateinit var whois: MaterialButton
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_domain_search)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get views
        toolbar = findViewById(R.id.toolbar)
        searchBtn = findViewById(R.id.searchBtn)
        searchTerm = findViewById(R.id.searchTerm)
        loading = findViewById(R.id.loading)
        emptyView = findViewById(R.id.noResults)
        searchFirst = findViewById(R.id.searchFirst)
        results = findViewById(R.id.results)
        price = findViewById(R.id.price)
        status = findViewById(R.id.status)
        domain = findViewById(R.id.domain)
        register = findViewById(R.id.register)
        whois = findViewById(R.id.whois)

        // Get firebase auth instance
        firebaseAuth = FirebaseAuth.getInstance()

        // Get firebase database
        firebaseDatabase = FirebaseDatabase.getInstance()

        // Check for intent extras
        if (intent.hasExtra("SEARCH_TERM")) {
            val toSearch = intent.getStringExtra("SEARCH_TERM").toString()
            searchTerm.setText(toSearch)
            searchDomain()
        }

        // Check for saved state
        if (savedInstanceState != null) {
            val toSearch = savedInstanceState.getString("SEARCH_TERM").toString()
            searchTerm.setText(toSearch)
            searchDomain()
        }

        // Set toolbar back button
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // Add text watcher to search term
        searchTerm.addTextChangedListener {
            // Enable search button if search term is not empty and contains at least one '.' character and each part is not empty
            searchBtn.isEnabled = it.toString().isNotEmpty() &&
                    it.toString().contains(".") &&
                    it.toString().split(".").size >= 2 &&
                    it.toString().split(".").all {part -> part.isNotEmpty() }
        }

        // Add click listener to search button
        searchBtn.setOnClickListener {
            searchDomain()
        }

        // Add click listener to whois button
        whois.setOnClickListener {
            val intentActivity = Intent(this, Whois::class.java)
            intentActivity.putExtra("DOMAIN", searchTerm.text.toString().lowercase())
            startActivity(intentActivity)
        }
    }

    private fun registerDomain(sld: String, tld: String, price: Double) {
        val formatPrice: String = NumberFormat.getCurrencyInstance().format(price)
        // Show AlertDialog with confirmation
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.domain_register_title, "${sld}.${tld}"))
            .setMessage(getString(R.string.domain_register_desc, "${sld}.${tld}", formatPrice))
            .setPositiveButton(getString(R.string.domain_register)) { _, _ ->
                // Create domain object
                val expiration = System.currentTimeMillis() + 31536000000 // 1 year from now
                val registration = System.currentTimeMillis()
                val domain = Domain(sld, tld, registration, expiration, price)

                // Add domain to user's domains
                val domainRef = firebaseDatabase.getReference("Domains/${firebaseAuth.currentUser!!.uid}")
                domainRef.push().setValue(domain)

                // Show success message
                Toast.makeText(this, getString(R.string.domain_register_success, "${domain.sld}.${domain.tld}"), Toast.LENGTH_SHORT).show()

                // Finish this activity and go to DomainList
                val intentActivity = Intent(this, DomainList::class.java)
                intentActivity.putExtra("DOMAINS", "active")
                startActivity(intentActivity)
                finish()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun searchDomain() {
        // Hide messages, results, show loader. Disable search button and term
        emptyView.isVisible = false
        searchFirst.isVisible = false
        results.isVisible = false
        loading.isVisible = true
        searchBtn.isEnabled = false
        searchTerm.isEnabled = false

        // Update text
        emptyView.text = getString(R.string.domain_no_results, searchTerm.text.toString())

        // Search for domain
        val domainSearch = searchTerm.text.toString().lowercase()
        val parts = domainSearch.split(".")
        val sld = parts[0]
        val tld = parts.subList(1, parts.size).joinToString(".")

        // Add to history, but only if it's not already in the history
        val historyRef = firebaseDatabase.getReference("History/${firebaseAuth.currentUser!!.uid}")
        historyRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var domainExists = false
                for (domain in dataSnapshot.children) {
                    if (domain.value.toString() == domainSearch) {
                        domainExists = true
                        break
                    }
                }

                if (!domainExists) {
                    historyRef.push().setValue(domainSearch)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // We don't need to do anything here
            }
        })

        // Check locally
        val domainRef = firebaseDatabase.getReference("Domains")
        domainRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var domainExists = false
                for (user in dataSnapshot.children) {
                    for (domain in user.children) {
                        if (domain.child("sld").value.toString() == sld && domain.child("tld").value.toString() == tld) {
                            domainExists = true
                            break
                        }
                    }
                }

                if (domainExists) {
                    // Show domain availability
                    domain.text = domainSearch

                    // Show domain status
                    status.text = getString(R.string.domain_taken)
                    status.setTextColor(getColor(R.color.red))

                    // Hide domain price
                    price.isVisible = false

                    // Hide register button
                    register.isVisible = false

                    // Show whois button, disabled
                    whois.isEnabled = false
                    whois.isVisible = true

                    // Show results
                    results.isVisible = true

                    // Hide loader. Enable search button and term
                    loading.isVisible = false
                    searchBtn.isEnabled = true
                    searchTerm.isEnabled = true
                }
                else {
                    // Get results from eNom API
                    val enomManager = EnomManager()

                    CoroutineScope(Dispatchers.IO).launch {
                        val domainResponse = enomManager.domainAvailability(sld, tld)
                        withContext(Dispatchers.Main){
                            if (domainResponse.status != "success") {
                                // Show error message
                                emptyView.isVisible = true
                                results.isVisible = false
                                loading.isVisible = false
                                searchBtn.isEnabled = true
                                searchTerm.isEnabled = true
                                return@withContext
                            }

                            // Show domain availability
                            domain.text = domainSearch

                            // Show domain status
                            status.text = if (domainResponse.domain.available!!) getString(R.string.domain_available) else getString(R.string.domain_taken)
                            status.setTextColor(getColor(if (domainResponse.domain.available!!) R.color.green else R.color.red))

                            // Show domain price
                            price.text = NumberFormat.getCurrencyInstance().format(domainResponse.domain.regPrice)
                            price.isVisible = domainResponse.domain.available!!

                            // Show register button
                            register.text = getString(R.string.domain_register_title, domainSearch)
                            register.isEnabled = domainResponse.domain.available!!
                            register.isVisible = domainResponse.domain.available!!

                            // Show results
                            results.isVisible = true

                            // Show whois button if domain is unavailable
                            whois.isEnabled = !domainResponse.domain.available!!
                            whois.isVisible = !domainResponse.domain.available!!

                            // Hide loader. Enable search button and term
                            loading.isVisible = false
                            searchBtn.isEnabled = true
                            searchTerm.isEnabled = true

                            // Handle register button
                            register.setOnClickListener {
                                registerDomain(sld, tld, domainResponse.domain.regPrice!!)
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Show error message
                emptyView.isVisible = true
                results.isVisible = false
                loading.isVisible = false
                searchBtn.isEnabled = true
                searchTerm.isEnabled = true
            }
        })
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("SEARCH_TERM", searchTerm.text.toString().lowercase())
    }
}