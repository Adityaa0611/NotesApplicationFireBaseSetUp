package com.example.note77

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.sign_up_activity)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signup_page)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val userName = findViewById<TextInputEditText>(R.id.user_name)
        val email = findViewById<TextInputEditText>(R.id.email)
        val password = findViewById<TextInputEditText>(R.id.password)
        val signup = findViewById<Button>(R.id.signup)
        val login = findViewById<TextView>(R.id.tvLogin)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        login.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        signup.setOnClickListener {
            val name = userName.text.toString().trim()
            val mailId = email.text.toString().trim()
            val pass = password.text.toString().trim()

            if (name.isEmpty() || mailId.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(mailId).matches()) {
                Toast.makeText(this, "Enter a valid email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isStrongPassword(pass)) {
                Toast.makeText(this, "Password must be at least 8 characters and include a digit, lowercase, uppercase, and special character", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            database = FirebaseDatabase.getInstance().getReference("users")

            database.child(name).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Username already taken", Toast.LENGTH_SHORT).show()

                    // Clear input fields
                    userName.setText("")
                    email.setText("")
                    password.setText("")
                } else {
                    val user = Users(name, mailId, pass)
                    database.child(name).setValue(user)
                        .addOnSuccessListener {
                            progressBar.visibility = View.GONE
                            Toast.makeText(this, "User registered successfully", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, NotesActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener {
                            progressBar.visibility = View.GONE
                            Toast.makeText(this, "Registration failed: ${it.message}", Toast.LENGTH_LONG).show()
                        }
                }
            }.addOnFailureListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isStrongPassword(password: String): Boolean {
        val pattern = Regex("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#\$%^&+=!]).{8,}\$")
        return pattern.matches(password)
    }
}
