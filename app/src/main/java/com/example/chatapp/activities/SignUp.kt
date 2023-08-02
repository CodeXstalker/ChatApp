package com.example.chatapp.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.chatapp.R
import com.example.chatapp.modalClasses.UserModal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUp : AppCompatActivity() {
    private lateinit var email: EditText
    private lateinit var name: EditText
    private lateinit var password: EditText
    private lateinit var signUp: Button
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var firebaseReference: DatabaseReference
    private lateinit var firebaseDatabase: FirebaseDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        fieldInitializer()


        signUp.setOnClickListener {
            allowUserToSignUp()
        }
    }

    private fun fieldInitializer() {
        email = findViewById(R.id.Email)
        password = findViewById(R.id.Password)
        signUp = findViewById(R.id.SignUp)
        name = findViewById(R.id.Name)
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        firebaseReference = firebaseDatabase.reference

    }


    private fun allowUserToSignUp() {
        val getEmail: String = email.text.toString()
        val getName: String = name.text.toString()
        val getPassword: String = password.text.toString()

        firebaseAuth.createUserWithEmailAndPassword(getEmail, getPassword).addOnCompleteListener(this){task->
            if(task.isSuccessful){
                addUserToDatabase()
                sendUserToMainActivity()
            }else{
                    Toast.makeText(this,"Something went wrong . . .", Toast.LENGTH_SHORT).show()
            }
        }


    }

    private fun addUserToDatabase() {
        val getName: String = name.text.toString()
        val getEmail : String = email.text.toString()
        val uid = firebaseAuth.currentUser?.uid!!.toString()
        firebaseReference.child("users").child(uid).setValue(UserModal(getName,getEmail,uid))

    }

    private fun sendUserToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }


}