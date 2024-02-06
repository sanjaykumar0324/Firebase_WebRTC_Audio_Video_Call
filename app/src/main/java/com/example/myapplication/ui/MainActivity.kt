
package com.example.myapplication.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.adapters.MainRecyclerViewAdapter
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.service.MainRepository

import com.example.myapplication.service.MainService
import com.example.myapplication.service.MainServiceRepository
import com.example.myapplication.utils.DataModel
import com.example.myapplication.utils.DataModelType
import com.example.myapplication.utils.getCameraAndMicPermission
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), MainRecyclerViewAdapter.Listener, MainService.Listener {
    private val TAG = "MainActivity"
    private lateinit var tvUserDetails: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var views: ActivityMainBinding
    private var username: String? = null
    private lateinit var callingButton: Button

    @Inject
    lateinit var mainRepository: MainRepository
    @Inject
    lateinit var mainServiceRepository: MainServiceRepository
    private var mainAdapter: MainRecyclerViewAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        views = ActivityMainBinding.inflate(layoutInflater)
        setContentView(views.root)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        tvUserDetails = findViewById(R.id.tvUserDetails)

        // Fetch and display user details from Firebase
        fetchAndDisplayUserDetails()

        init()

    }

    private fun init() {
        username = intent.getStringExtra("username")
        if (username == null) finish()
        //1. observe other users status
        subscribeObservers()
        //2. start foreground service to listen negotiations and calls.
        startMyService()
    }

    private fun subscribeObservers() {
        setupRecyclerView()
        MainService.listener = this
        mainRepository.observeUsersStatus {
            Log.d(TAG, "subscribeObservers: $it")
            mainAdapter?.updateList(it)
        }
    }

    private fun setupRecyclerView() {
        mainAdapter = MainRecyclerViewAdapter(this)
        val layoutManager = LinearLayoutManager(this)
        views.mainRecyclerView.apply {
            setLayoutManager(layoutManager)
            adapter = mainAdapter
        }
    }

    private fun startMyService() {
        mainServiceRepository.startService(username!!)
    }

    override fun onVideoCallClicked(username: String) {
        //check if permission of mic and camera is taken
        getCameraAndMicPermission {
            mainRepository.sendConnectionRequest(username, true) {
                if (it){
                    //we have to start video call
                    //we wanna create an intent to move to call activity
                    startActivity(Intent(this,CallActivity::class.java).apply {
                        putExtra("target",username)
                        putExtra("isVideoCall",true)
                        putExtra("isCaller",true)
                    })

                }
            }

        }
    }

    override fun onAudioCallClicked(username: String) {
        getCameraAndMicPermission {
            mainRepository.sendConnectionRequest(username, false) {
                if (it){
                    //we have to start audio call
                    //we wanna create an intent to move to call activity
                    startActivity(Intent(this,CallActivity::class.java).apply {
                        putExtra("target",username)
                        putExtra("isVideoCall",false)
                        putExtra("isCaller",true)
                    })
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        mainServiceRepository.stopService()
    }

    override fun onCallReceived(model: DataModel) {
        runOnUiThread {
            views.apply {
                val isVideoCall = model.type == DataModelType.StartVideoCall
                val isVideoCallText = if (isVideoCall) "Video" else "Audio"
                incomingCallTitleTv.text = "${model.sender} is $isVideoCallText Calling you"
                incomingCallLayout.isVisible = true
                acceptButton.setOnClickListener {
                    getCameraAndMicPermission {
                        incomingCallLayout.isVisible = false
                        //create an intent to go to video call activity
                        startActivity(Intent(this@MainActivity,CallActivity::class.java).apply {
                            putExtra("target",model.sender)
                            putExtra("isVideoCall",isVideoCall)
                            putExtra("isCaller",false)
                        })
                    }
                }
                declineButton.setOnClickListener {
                    incomingCallLayout.isVisible = false
                }

            }
        }
    }
    private fun fetchAndDisplayUserDetails() {
        val currentUser: FirebaseUser? = auth.currentUser

        currentUser?.let { user ->
            val userId: String = user.uid
            val usersRef: DatabaseReference = database.getReference("users").child(userId)

            usersRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val userDetails = dataSnapshot.getValue(UserDetails::class.java)

                        userDetails?.let {
                            val userDetailsText = "Name: ${it.name}\nProfession: ${it.profession}\n" +
                                    "Mobile: ${it.mobile}\nEmail: ${it.email}\nUser ID: ${it.userId}"
                            tvUserDetails.text = userDetailsText
                        }
                    } else {
                        // Log a message when data does not exist
                        Log.d("FirebaseData", "No data exists for the user.")
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Log the error message
                    Log.e("FirebaseData", "Error fetching data: ${databaseError.message}")
                }
            })
        }
    }


}