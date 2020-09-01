package com.ralhub

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.CheckedTextView
import android.widget.Toast
import com.facebook.*
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*
import java.util.*


class LoginActivity : AppCompatActivity() {
    lateinit var auth : FirebaseAuth
    lateinit var callbackManger : CallbackManager
    lateinit var checkedTextView : CheckedTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        auth = FirebaseAuth.getInstance()

        callbackManger = CallbackManager.Factory.create()
        checkedTextView = findViewById(R.id.checkedTextView)
        checkedTextView.setOnClickListener {
            facebookLogin()
        }
    }

    fun signinAndSignup(){
        auth.createUserWithEmailAndPassword(email_text.text.toString(),password_edittext.text.toString()).addOnCompleteListener {
            if (it.isSuccessful){
                moveMainPage(it.result?.user)
            }else if(it.exception?.message.isNullOrEmpty()){
                
            }
        }
    }


    fun facebookLogin(){
        LoginManager.getInstance()
            .logInWithReadPermissions(this, Arrays.asList("public_profile","email"))

        LoginManager.getInstance()
            .registerCallback(callbackManger, object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    handleFacebookAccessToken(loginResult.accessToken)
                }

                override fun onCancel() {
                }

                override fun onError(error: FacebookException?) {
                    Log.e("Callback :: ", "onError : " + error?.stackTrace);
                }

            })
    }

    fun handleFacebookAccessToken(token : AccessToken){
        val credential = FacebookAuthProvider.getCredential(token.token!!)
        auth.signInWithCredential(credential)
            .addOnCompleteListener {
                    task ->
                if (task.isSuccessful){
                    moveMainPage(task.result?.user)
                }else{
                    Toast.makeText(this,task.exception?.message, Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManger.onActivityResult(requestCode,resultCode,data)
    }

    fun moveMainPage(user: FirebaseUser?){
        if (user != null){
            startActivity(Intent(this,MainActivity::class.java))
        }
    }
}