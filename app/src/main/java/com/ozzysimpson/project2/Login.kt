package com.ozzysimpson.project2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {
    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var loginButton: MaterialButton
    private lateinit var signUpButton: MaterialButton
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var remember: SwitchMaterial

    private var usernameSet: Boolean = false
    private var passwordSet: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // Get shared preferences
        val preferences = getSharedPreferences("Oznorts", Context.MODE_PRIVATE)

        // Get views
        username = findViewById(R.id.email)
        password = findViewById(R.id.password)
        loginButton = findViewById(R.id.loginButton)
        signUpButton = findViewById(R.id.signup)
        remember = findViewById(R.id.remember)

        // Get firebase auth instance
        firebaseAuth = FirebaseAuth.getInstance()

        // Disable login button
        loginButton.isEnabled = false

        // Text watchers
        username.addTextChangedListener(usernameWatcher)
        password.addTextChangedListener(passwordWatcher)

        // Get saved login info
        val rememberMe = preferences.getBoolean("REMEMBER_ME", false)
        val savedUsername = preferences.getString("SAVED_USERNAME", "")
        val savedPassword = preferences.getString("SAVED_PASSWORD", "")

        // Set saved info
        if (rememberMe) {
            username.setText(savedUsername)
            password.setText(savedPassword)
            remember.isChecked = true

            // Check if logged in with Firebase already, if so, go to dashboard
            if (firebaseAuth.currentUser != null) {
                val intentActivity = Intent(this, MainActivity::class.java)
                startActivity(intentActivity)
            }
        }
        else {
            remember.isChecked = false
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Handle login
        loginButton.setOnClickListener {

            val inputtedUsername: String = username.text.toString().trim()
            val inputtedPassword: String = password.text.toString().trim()

            firebaseAuth.signInWithEmailAndPassword(inputtedUsername, inputtedPassword)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this, getString(R.string.login_success), Toast.LENGTH_LONG).show()

                        // Save login info
                        if (remember.isChecked) {
                            preferences.edit().putBoolean("REMEMBER_ME", true).apply()
                            preferences.edit().putString("SAVED_USERNAME", username.text.toString()).apply()
                            preferences.edit().putString("SAVED_PASSWORD", password.text.toString()).apply()
                        }
                        else {
                            preferences.edit().putBoolean("REMEMBER_ME", false).apply()
                            preferences.edit().putString("SAVED_USERNAME", "").apply()
                            preferences.edit().putString("SAVED_PASSWORD", "").apply()
                        }

                        // Go to dashboard
                        val intentActivity = Intent(this, MainActivity::class.java)
                        startActivity(intentActivity)
                    }
                    else {
                        // Login failed, show dialog
                        AlertDialog.Builder(this)
                            .setTitle(getString(R.string.login_fail_title))
                            .setMessage(getString(R.string.login_fail_desc))
                            .setPositiveButton(getString(R.string.ok), null)
                            .show()
                    }
                }

        }

        // Handle sign up
        signUpButton.setOnClickListener {
            // Go to signup
            val intentActivity = Intent(this, SignUp::class.java)
            startActivity(intentActivity)
        }
    }

    private val usernameWatcher: TextWatcher = object: TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable?) {
            usernameSet = !s.isNullOrBlank()
            loginButton.isEnabled = usernameSet && passwordSet
        }
    }
    private val passwordWatcher: TextWatcher = object: TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable?) {
            passwordSet = !s.isNullOrBlank()
            loginButton.isEnabled = usernameSet && passwordSet
        }
    }
}