package com.theayushyadav11.loginandsignup

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.theayushyadav11.loginandsignup.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding:ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth=FirebaseAuth.getInstance()
        if(auth.currentUser!=null)
        {
            startActivity(Intent(this, MainActivity::class.java))
        }
        listeners()

    }
    private fun listeners()
    {
        binding.btnLogin.setOnClickListener {
            signUp()
        }
        binding.tvSignIn.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }
        binding.cvGoogle.setOnClickListener{
          signInWithGoogle()
        }
    }
    fun signUp()
    {
        val email=binding.etEmail.text.toString().trim()
        val password=binding.etPassword.text.toString().trim()
        val cnfPassword=binding.etConfirmPassword.text.toString().trim()
        if(email.isNotEmpty()&&password.isNotEmpty()&&cnfPassword.isNotEmpty())
        {
            if(password==cnfPassword)
            {
                auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener{
                    if(it.isSuccessful)
                    {
                        toast("SignUp Successfull")
                        startActivity(Intent(this, MainActivity::class.java))

                    }
                    else
                    {
                        toast(it.exception?.message.toString())
                    }
                }
            }
            else
            {
                toast("Password and confirm password does not match")
            }
        }
        else
        {
            toast("No Parameters should be Empty!")
        }

    }
    fun toast(s:String)
    {
        Toast.makeText(this, "$s", Toast.LENGTH_SHORT).show()
    }
    fun signInWithGoogle()
    {
        val gso= GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient= GoogleSignIn.getClient(this,gso)

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
                    auth.signInWithCredential(credential).addOnCompleteListener {
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
}