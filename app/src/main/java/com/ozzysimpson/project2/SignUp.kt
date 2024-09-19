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
import com.google.firebase.auth.userProfileChangeRequest

class SignUp : AppCompatActivity() {
    private lateinit var name: EditText
    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var password2: EditText
    private lateinit var loginButton: MaterialButton
    private lateinit var signUpButton: MaterialButton
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var remember: SwitchMaterial

    private var nameSet: Boolean = false
    private var usernameSet: Boolean = false
    private var passwordSet: Boolean = false
    private var password2Set: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        // Get shared preferences
        val preferences = getSharedPreferences("Oznorts", Context.MODE_PRIVATE)

        // Get views
        name = findViewById(R.id.name)
        username = findViewById(R.id.email)
        password = findViewById(R.id.password)
        password2 = findViewById(R.id.password2)
        signUpButton = findViewById(R.id.signupButton)
        loginButton = findViewById(R.id.login)
        remember = findViewById(R.id.remember)

        // Get firebase auth instance
        firebaseAuth = FirebaseAuth.getInstance()

        // Disable signup button
        signUpButton.isEnabled = false

        // Text watchers
        name.addTextChangedListener(nameWatcher)
        username.addTextChangedListener(usernameWatcher)
        password.addTextChangedListener(passwordWatcher)
        password2.addTextChangedListener(password2Watcher)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Handle login
        loginButton.setOnClickListener {
            // Go to login
            val intentActivity = Intent(this, Login::class.java)
            startActivity(intentActivity)
        }

        // Handle signup
        signUpButton.setOnClickListener {
            val inputtedName: String = name.text.toString().trim()
            val inputtedUsername: String = username.text.toString().trim()
            val inputtedPassword: String = password.text.toString().trim()
            val inputtedPassword2: String = password2.text.toString().trim()

            if (inputtedPassword != inputtedPassword2) {
                // Passwords don't match, show alert
                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.signup_password_title))
                    .setMessage(getString(R.string.signup_password_desc))
                    .setPositiveButton(getString(R.string.ok), null)
                    .show()
                return@setOnClickListener
            }

            firebaseAuth.createUserWithEmailAndPassword(inputtedUsername, inputtedPassword)
                .addOnCompleteListener{ signupTask ->
                    if (signupTask.isSuccessful) {
                        val user = firebaseAuth.currentUser
                        val profileUpdates = userProfileChangeRequest {
                            displayName = inputtedName
                        }
                        user!!.updateProfile(profileUpdates)
                            .addOnCompleteListener{ profileTask ->
                                if (profileTask.isSuccessful) {
                                    Toast.makeText(this, getString(R.string.signup_success), Toast.LENGTH_LONG).show()

                                    // Save login info
                                    if (remember.isChecked) {
                                        preferences.edit().putBoolean("REMEMBER_ME", true).apply()
                                        preferences.edit()
                                            .putString("SAVED_USERNAME", username.text.toString())
                                            .apply()
                                        preferences.edit()
                                            .putString("SAVED_PASSWORD", password.text.toString())
                                            .apply()
                                    } else {
                                        preferences.edit().putBoolean("REMEMBER_ME", false).apply()
                                        preferences.edit().putString("SAVED_USERNAME", "").apply()
                                        preferences.edit().putString("SAVED_PASSWORD", "").apply()
                                    }

                                    // Go to dashboard
                                    val intentActivity = Intent(this, MainActivity::class.java)
                                    startActivity(intentActivity)
                                }
                                else {
                                    // Failed to update profile, show alert
                                    AlertDialog.Builder(this)
                                        .setTitle(getString(R.string.signup_fail_title))
                                        .setMessage(getString(R.string.signup_profileFail_desc))
                                        .setPositiveButton(getString(R.string.ok), null)
                                        .show()
                                }
                            }
                    }
                    else {
                        // Failed to signup, show alert
                        AlertDialog.Builder(this)
                            .setTitle(getString(R.string.signup_fail_title))
                            .setMessage(getString(R.string.signup_fail_desc))
                            .setPositiveButton(getString(R.string.ok), null)
                            .show()
                    }
                }
        }
    }

    private val nameWatcher: TextWatcher = object: TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable?) {
            nameSet = !s.isNullOrBlank()
            signUpButton.isEnabled = usernameSet && passwordSet && password2Set && nameSet
        }
    }
    private val usernameWatcher: TextWatcher = object: TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable?) {
            usernameSet = !s.isNullOrBlank()
            signUpButton.isEnabled = usernameSet && passwordSet && password2Set && nameSet
        }
    }
    private val passwordWatcher: TextWatcher = object: TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable?) {
            passwordSet = !s.isNullOrBlank()
            signUpButton.isEnabled = usernameSet && passwordSet && password2Set && nameSet
        }
    }
    private val password2Watcher: TextWatcher = object: TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable?) {
            password2Set = !s.isNullOrBlank()
            signUpButton.isEnabled = usernameSet && passwordSet && password2Set && nameSet
        }
    }
}