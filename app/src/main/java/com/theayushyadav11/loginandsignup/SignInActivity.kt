package com.theayushyadav11.loginandsignup

import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.theayushyadav11.loginandsignup.databinding.ActivitySignInBinding

class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignInBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.tvSignUp.setOnClickListener{
            startActivity(Intent(this, SignUpActivity::class.java))
        }
        firebaseAuth= FirebaseAuth.getInstance()

        binding.cvGoogle.setOnClickListener {
            signInWithGoogle()
        }


        binding.btnLogin.setOnClickListener {
            val  progressDialog = ProgressDialog(this).apply {
                setMessage("Signing in")
                setCancelable(false)
            }
            progressDialog.show()

            val email=binding.etEmail.text.toString().trim()
            val password=binding.etPassword.text.toString().trim()
            if(email.isNotEmpty()&&password.isNotEmpty())
            {
                firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener {
                    progressDialog.dismiss()
                    if(it.isSuccessful)
                    {
                        Toast.makeText(this, "Signed in Successful", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                    }
                    else
                    {
                        Toast.makeText(this, it.exception?.message.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else
            {
                Toast.makeText(this, "Invalid Email or password", Toast.LENGTH_SHORT).show()
            }
        }

    }
    fun toast(s:String)
    {
        Toast.makeText(this, "$s", Toast.LENGTH_SHORT).show()
    }

//****************************************************************************************************************************************************************************************************
    //Copy from here
    fun signInWithGoogle()
    {
        val gso= GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient= GoogleSignIn.getClient(this,gso)

        val signInIntent = googleSignInClient.signInIntent
        launcher.launch(signInIntent)

    }
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
        if (result.resultCode == Activity.RESULT_OK){

            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            if (task.isSuccessful){


                val account : GoogleSignInAccount? = task.result
                if (account != null){

                    val credential = GoogleAuthProvider.getCredential(account.idToken , null)
                    firebaseAuth.signInWithCredential(credential).addOnCompleteListener {
                        if (it.isSuccessful){

                            val intent : Intent = Intent(this , MainActivity::class.java)
                            intent.putExtra("email" , account.email)
                            intent.putExtra("name" , account.displayName)
                            startActivity(intent)
                        }else{
                            Toast.makeText(this, it.exception.toString() , Toast.LENGTH_SHORT).show()

                        }
                    }
                }
            }else{
                Toast.makeText(this, task.exception.toString() , Toast.LENGTH_SHORT).show()
            }
        }
    }
    //copy till here
//****************************************************************************************************************************************************************************************************


}


