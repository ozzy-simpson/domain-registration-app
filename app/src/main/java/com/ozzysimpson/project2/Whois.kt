package com.ozzysimpson.project2

import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Whois : AppCompatActivity() {
    private lateinit var toolbar: MaterialToolbar
    private lateinit var loading: ProgressBar
    private lateinit var emptyView: TextView
    private lateinit var results: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_whois)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Get views
        toolbar = findViewById(R.id.toolbar)
        loading = findViewById(R.id.loading)
        emptyView = findViewById(R.id.noResults)
        results = findViewById(R.id.results)

        // Set toolbar back button
        toolbar.setNavigationOnClickListener {
            finish()
        }

        // Get intent extras
        val domain = intent.getStringExtra("DOMAIN").toString()

        // Set toolbar title
        toolbar.title = getString(R.string.domain_whois_title, domain)

        // Get whois information
        val oznortsManager = OznortsManager()
        CoroutineScope(Dispatchers.IO).launch {
            val whoisResponse = oznortsManager.getWhois(getString(R.string.oznorts_id), getString(R.string.oznorts_secret), getString(R.string.oznorts_accesskey), domain)
            withContext(Dispatchers.Main){
                // check for errors
                if (whoisResponse.result != "success") {
                    // Show error dialog. On close, finish activity
                    AlertDialog.Builder(this@Whois)
                        .setTitle(getString(R.string.domain_whois_error_title))
                        .setMessage(getString(R.string.domain_whois_error_desc, domain))
                        .setPositiveButton(getString(R.string.ok)) { _, _ -> finish() }
                        .setCancelable(false)
                        .show()
                }
                if (whoisResponse.whois?.isEmpty() == true) {
                    emptyView.isVisible = true
                    emptyView.text = getString(R.string.domain_whois_noresults, domain)
                    loading.isVisible = false
                    results.isVisible = false
                    return@withContext
                }

                // display whois information
                results.text = whoisResponse.whois

                // hide loading bar, show results
                loading.isVisible = false
                emptyView.isVisible = false
                results.isVisible = true
            }
        }
    }
}