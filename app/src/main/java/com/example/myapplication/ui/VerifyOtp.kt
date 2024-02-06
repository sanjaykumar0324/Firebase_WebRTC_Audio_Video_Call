package com.example.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R

class VerifyOtp : AppCompatActivity() {

    private lateinit var etOtp: EditText
    private lateinit var btnVerifyOtp: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_otp)

        etOtp = findViewById(R.id.etOtp)
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp)

        val userId = intent.getStringExtra("userId") ?: ""

        btnVerifyOtp.setOnClickListener {
            val enteredOtp = etOtp.text.toString()

            // Implement OTP verification logic (you might use a library or Firebase Phone Auth)
            val isOtpVerified = verifyOtp(enteredOtp)

            if (isOtpVerified) {
                // Navigate to the main activity
                val intent = Intent(this, MainActivity::class.java).apply {
                    putExtra("userId", userId)
                }
                startActivity(intent)
                finish()
            } else {
                // Handle OTP verification failure
            }
        }
    }

    private fun verifyOtp(enteredOtp: String): Boolean {
        // Implement OTP verification logic here
        // For simplicity, assume OTP verification is successful
        return true
    }
}
