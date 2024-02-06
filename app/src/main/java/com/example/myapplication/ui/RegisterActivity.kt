package com.example.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var etRegisterName: EditText
    private lateinit var etRegisterProfession: EditText
    private lateinit var etRegisterMobile: EditText
    private lateinit var etRegisterEmail: EditText
    private lateinit var etRegisterPassword: EditText
    private lateinit var btnRegister: Button

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        etRegisterName = findViewById(R.id.etRegisterName)
        etRegisterProfession = findViewById(R.id.etRegisterProfession)
        etRegisterMobile = findViewById(R.id.etRegisterMobile)
        etRegisterEmail = findViewById(R.id.etRegisterEmail)
        etRegisterPassword = findViewById(R.id.etRegisterPassword)
        btnRegister = findViewById(R.id.btnRegister)

        btnRegister.setOnClickListener {
            // Register a new user with email and password
            val email = etRegisterEmail.text.toString()
            val password = etRegisterPassword.text.toString()

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Registration successful, save additional user details and navigate to LoginActivity
                        val userDetails = saveUserDetails()
                        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show()

                        // Navigate to LoginActivity
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        // If registration fails, display a message to the user.
                        Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun saveUserDetails(): UserDetails {
        // For demonstration purposes, you would save user details to Firebase here
        val name = etRegisterName.text.toString()
        val profession = etRegisterProfession.text.toString()
        val mobile = etRegisterMobile.text.toString()
        val email = etRegisterEmail.text.toString()

        // Get a reference to the Firebase database
        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("users")

        // Generate a unique key for the user
        val userId = usersRef.push().key!!

        // Create a UserDetails object with user information
        val userDetails = UserDetails(userId, name, profession, mobile, email)

        // Save user details to the database
        usersRef.child(userId).setValue(userDetails)

        return userDetails
    }
}
