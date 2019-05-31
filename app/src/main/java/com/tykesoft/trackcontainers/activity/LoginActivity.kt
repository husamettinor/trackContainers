package com.tykesoft.trackcontainers.activity

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
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
                if(etEmail.text.toString() != "" && etPassword.text.toString() != "") {
                    mAuth.signInWithEmailAndPassword(etEmail.text.toString(), etPassword.text.toString())
                            .addOnCompleteListener(this) {
                                if (it.isSuccessful) {
                                    val intent = Intent(this, OperationActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    showError("Authentication failed", true)
                                }
                            }
                } else {
                    showError("E-mail and password are required", true)
                }
            }
        } else {
            showError("Check your internet connection", false)
        }

        etEmail.setOnFocusChangeListener { _, _ ->
            showError("", true)
        }

        etPassword.setOnFocusChangeListener { _, _ ->
            showError("", true)
        }
    }

    private fun checkConnection() : Boolean {
        val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return (activeNetwork != null) && (activeNetwork.isConnected)
    }

    private fun showError(message: String, isLoginEnabled: Boolean) {
        txtError.text = message
        btnLogin.isEnabled = isLoginEnabled
    }
}
