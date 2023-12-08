package com.example.books

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        val emailEditText: EditText = findViewById(R.id.registerEmailEditText)
        val passwordEditText: EditText = findViewById(R.id.registerPasswordEditText)
        val confirmPasswordEditText: EditText = findViewById(R.id.confirmPasswordEditText)
        val registerButton: Button = findViewById(R.id.registerButton)
        val loginInsteadButton: Button = findViewById(R.id.loginInsteadButton)
        val progressBar: ProgressBar = findViewById(R.id.registerProgressBar)

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                if (password == confirmPassword) {
                    // Show ProgressBar when registration starts
                    progressBar.visibility = ProgressBar.VISIBLE
                    registerUser(email, password, progressBar)
                } else {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        loginInsteadButton.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun registerUser(email: String, password: String, progressBar: ProgressBar) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                // Hide ProgressBar after registration completes
                progressBar.visibility = ProgressBar.INVISIBLE

                if (task.isSuccessful) {
                    // Obtain the UID of the newly registered user
                    val user = task.result?.user
                    val uid = user?.uid

                    if (uid != null) {
                        // Create a Firestore document for the user with the UID
                        createFirestoreAccount(uid, email)
                    }

                    // Registration successful, navigate to MainActivity
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    // If registration fails, display a message to the user
                    Toast.makeText(this, "Registration Failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun createFirestoreAccount(uid: String, email: String) {
        val db = FirebaseFirestore.getInstance()
        val userMap = hashMapOf(
            "email" to email
        )

        // Create a document in the "users" collection with the UID as the document ID
        db.collection("users").document(uid)
            .set(userMap)
            .addOnSuccessListener {
                // Handle success (optional)
                Log.d(TAG, "Firestore account created successfully for UID: $uid")
            }
            .addOnFailureListener { e ->
                // Handle failure (optional)
                Log.w(TAG, "Error creating Firestore account for UID: $uid", e)
            }
    }

}

