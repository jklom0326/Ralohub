package com.ralhub

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {
    lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        auth = FirebaseAuth.getInstance()

        register_btn.setOnClickListener {
            signinEmail()
        }
    }
    fun signinEmail(){
        auth.createUserWithEmailAndPassword(email_register_id.text.toString(),email_register_password.text.toString())
            .addOnCompleteListener {
                    task ->
                if(task.isSuccessful){
                    //login
                    moveMainPage(task.result?.user)
                } else if (task.exception?.message.isNullOrEmpty()) {
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                } else{
                    //show the error message
                    Toast.makeText(this,task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }

    }
    fun moveMainPage(user: FirebaseUser?){
        if (user != null){
            startActivity(Intent(this,MainActivity::class.java))
        }
    }
}