package com.example.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    private lateinit var etLoginEmail: EditText
    private lateinit var etLoginPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegister: TextView // Added TextView for registration

    private lateinit var auth: FirebaseAuth
    private lateinit var verificationId: String
    private lateinit var phoneNo: String // To store the registered phone number

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        etLoginEmail = findViewById(R.id.etLoginId) // Change to etLoginEmail
        etLoginPassword = findViewById(R.id.etLoginPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegister = findViewById(R.id.tvRegister) // Initialize TextView for registration
        tvRegister.setOnClickListener {
            // Open the RegisterActivity when the text is clicked
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }

        btnLogin.setOnClickListener {
            val email = etLoginEmail.text.toString()
            val password = etLoginPassword.text.toString()

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Authentication success, fetch UID and then fetch mobile number
                        val currentUser: FirebaseUser? = auth.currentUser
                        currentUser?.let { user ->
                            fetchMobileNumber(user)
                        }
                    } else {
                        // If authentication fails, display a message to the user.
                        Toast.makeText(
                            this@LoginActivity,
                            "Authentication failed: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun fetchMobileNumber(user: FirebaseUser) {
        val userId: String = user.uid
        val usersRef: DatabaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId)

        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // User details found, retrieve the mobile number
                    val mobile = dataSnapshot.child("mobile").getValue(String::class.java)
                    Log.d("LoginActivity", "Mobile number from database: $mobile")


                        // Mobile number found, send OTP or do further processing
                        val completeMobile = "+919814344693"
                        sendOtpForVerification(completeMobile)

                } else {
                    val completeMobile = "+919814344693"
                    sendOtpForVerification(completeMobile)
                    Toast.makeText(this@LoginActivity, "User  found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
                Toast.makeText(this@LoginActivity, "Error fetching data: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun sendOtpForVerification(phoneNumber: String) {
        // Use Firebase Authentication Phone Auth provider to send OTP to the registered phone number
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1. Instant verification. In some cases, the phone number can be instantly verified without needing to send or enter an OTP.
            // 2. Auto-retrieval. On some devices, Google Play services can automatically detect the incoming verification SMS and perform verification without user action.

            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked if an invalid request for verification is made, for instance, if the phone number format is not valid.
            Toast.makeText(this@LoginActivity, "Verification failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            // This callback is invoked after the code is sent successfully to the provided phone number.
            // You should save the verification ID and use it to verify the code with the user.
            this@LoginActivity.verificationId = verificationId

            // Navigate to the OTP verification activity
            val intent = Intent(this@LoginActivity, VerifyOtp::class.java).apply {
                putExtra("verificationId", verificationId)
            }
            startActivity(intent)
            finish()
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = task.result?.user
                    // Handle the signed-in user as needed (e.g., navigate to the main activity)
                    handleSignInSuccess(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun handleSignInSuccess(user: FirebaseUser?) {
        // Handle the signed-in user as needed
        // For example, navigate to the main activity
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
