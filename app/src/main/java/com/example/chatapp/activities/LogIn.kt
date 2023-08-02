package com.example.chatapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.chatapp.R
import com.google.firebase.auth.FirebaseAuth

class LogIn : AppCompatActivity() {
    private lateinit var email : EditText
    private lateinit var password : EditText
    private lateinit var logIn : Button
    private lateinit var signUp : Button
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        fieldInitializer()

        signUp.setOnClickListener {
            sendUserToSignUpActivity()
        }

        logIn.setOnClickListener {
            allowUserToLogin()
        }

    }

    private fun fieldInitializer() {
        email = findViewById(R.id.Email)
        password = findViewById(R.id.Password)
        logIn = findViewById(R.id.LogIn)
        signUp = findViewById(R.id.SignUp)
        firebaseAuth = FirebaseAuth.getInstance()
    }

    private fun sendUserToSignUpActivity() {
        startActivity(Intent(this, SignUp::class.java))
    }

    private fun allowUserToLogin() {
        val getEmail: String = email.text.toString()
        val getPassword: String = password.text.toString()

        firebaseAuth.signInWithEmailAndPassword(getEmail, getPassword).addOnCompleteListener(this){task->
            if(task.isSuccessful){
                sendUserToMainActivity()
                finish()
            }else{
                val exception = task.exception
                val errorMessage = exception?.message ?: "Unknown error occurred."
                Toast.makeText(this, "User Doesn't Exist: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        }

    }


    private fun sendUserToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
    }



}