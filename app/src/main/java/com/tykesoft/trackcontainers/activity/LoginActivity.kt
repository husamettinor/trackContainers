package com.tykesoft.trackcontainers.activity

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.tykesoft.trackcontainers.R
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if (checkConnection()) {
            mAuth = FirebaseAuth.getInstance()

            btnLogin.setOnClickListener {
                mAuth.signInWithEmailAndPassword(etEmail.text.toString(), etPassword.text.toString())
                        .addOnCompleteListener(this) {
                            if (it.isSuccessful) {
                                val intent = Intent(this, OperationActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(this, "Authentication failed", Toast.LENGTH_LONG).show()
                            }
                        }
            }
        } else {
            btnLogin.isEnabled = false
            Toast.makeText(this, "Check your internet connection", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkConnection() : Boolean {
        val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return (activeNetwork != null) && (activeNetwork.isConnected)
    }
}
