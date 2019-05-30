package com.tykesoft.trackcontainers.activity

import android.content.Intent
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

        mAuth = FirebaseAuth.getInstance()

        btLogin.setOnClickListener {
            mAuth.signInWithEmailAndPassword(etEmail.text.toString(), etPassword.text.toString())
                    .addOnCompleteListener(this) {
                        if(it.isSuccessful) {
                            val intent = Intent(this, OperationActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this, "Authentication failed", Toast.LENGTH_LONG).show()
                        }
                    }
        }
    }
}
