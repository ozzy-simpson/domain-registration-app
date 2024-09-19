package com.ozzysimpson.project2

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    private lateinit var welcome: TextView
    private lateinit var domainCount: TextView
    private lateinit var expDomainCount: TextView
    private lateinit var expdDomainCount: TextView
    private lateinit var regDomains: TextView
    private lateinit var expDomains: TextView
    private lateinit var expdDomains: TextView
    private lateinit var searchDomains: ConstraintLayout
    private lateinit var historyDomains: ConstraintLayout
    private lateinit var activeDomains: ConstraintLayout
    private lateinit var expiringDomains: ConstraintLayout
    private lateinit var expiredDomains: ConstraintLayout
    private lateinit var logout: MaterialButton
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get views
        welcome = findViewById(R.id.welcome)
        domainCount = findViewById(R.id.numDomains)
        expDomainCount = findViewById(R.id.numExpiringDomains)
        expdDomainCount = findViewById(R.id.numExpDomains)
        regDomains = findViewById(R.id.regDomains)
        expDomains = findViewById(R.id.expDomains)
        expdDomains = findViewById(R.id.expdDomains)
        searchDomains = findViewById(R.id.searchDomains)
        historyDomains = findViewById(R.id.historyDomains)
        activeDomains = findViewById(R.id.registeredDomains)
        expiringDomains = findViewById(R.id.expiringDomains)
        expiredDomains = findViewById(R.id.expiredDomains)
        logout = findViewById(R.id.logout)

        // Get firebase auth instance
        firebaseAuth = FirebaseAuth.getInstance()

        // Get firebase database
        firebaseDatabase = FirebaseDatabase.getInstance()

        // Update user's name in welcome text
        val user = firebaseAuth.currentUser
        user?.let {
            welcome.text = getString(R.string.welcome, it.displayName)
        }

        // Logout
        logout.setOnClickListener {
            // Logout of firebase
            firebaseAuth.signOut()

            // Go to login activity
            val intentActivity = Intent(this, Login::class.java)
            startActivity(intentActivity)
        }

        // Get count of active domains for user
        val domainRef = firebaseDatabase.getReference("Domains/${firebaseAuth.currentUser!!.uid}")
        domainRef.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.home_domainFail),
                    Toast.LENGTH_LONG
                ).show()

                this@MainActivity.domainCount.text = "N/A"
                this@MainActivity.expDomainCount.text = "N/A"
                this@MainActivity.expdDomainCount.text = "N/A"
                regDomains.text = getString(R.string.home_registeredDomains)
                expDomains.text = getString(R.string.home_expiringDomains)
                expdDomains.text = getString(R.string.home_expiredDomains)
            }
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Only count non-expired domains for active
                val domainCount: Int = dataSnapshot.children.count { domain ->
                    domain.child("expiration").value.toString()
                        .toLong() > System.currentTimeMillis()
                }

                this@MainActivity.domainCount.text = domainCount.toString()
                if (domainCount == 1) {
                    regDomains.text = getString(R.string.home_registeredDomain)
                } else {
                    regDomains.text = getString(R.string.home_registeredDomains)
                }

                // Only count expiring in <30 day domains for expiring
                val expDomainCount: Int = dataSnapshot.children.count { domain ->
                    domain.child("expiration").value.toString()
                        .toLong() > System.currentTimeMillis() && domain.child("expiration").value.toString()
                        .toLong() < System.currentTimeMillis() + 2592000000
                }

                this@MainActivity.expDomainCount.text = expDomainCount.toString()
                if (expDomainCount == 1) {
                    expDomains.text = getString(R.string.home_expiringDomain)
                } else {
                    expDomains.text = getString(R.string.home_expiringDomains)
                }

                // Only count expired domains for expired
                val expdDomainCount: Int = dataSnapshot.children.count { domain ->
                    domain.child("expiration").value.toString()
                        .toLong() < System.currentTimeMillis()
                }

                this@MainActivity.expdDomainCount.text = expdDomainCount.toString()
                if (expdDomainCount == 1) {
                    expdDomains.text = getString(R.string.home_expiredDomain)
                } else {
                    expdDomains.text = getString(R.string.home_expiredDomains)
                }
            }
        })

        // Set click listeners
        searchDomains.setOnClickListener {
            // Go to search activity
            val intentActivity = Intent(this, DomainSearch::class.java)
            startActivity(intentActivity)
        }

        historyDomains.setOnClickListener {
            // Go to search activity
            val intentActivity = Intent(this, DomainHistory::class.java)
            startActivity(intentActivity)
        }

        activeDomains.setOnClickListener {
            // Go to active domains activity
            val intentActivity = Intent(this, DomainList::class.java)
            intentActivity.putExtra("DOMAINS", "active")
            startActivity(intentActivity)
        }

        expiringDomains.setOnClickListener {
            // Go to expiring domains activity
            val intentActivity = Intent(this, DomainList::class.java)
            intentActivity.putExtra("DOMAINS", "expiring")
            startActivity(intentActivity)
        }

        expiredDomains.setOnClickListener {
            // Go to expired domains activity
            val intentActivity = Intent(this, DomainList::class.java)
            intentActivity.putExtra("DOMAINS", "expired")
            startActivity(intentActivity)
        }

    }
}