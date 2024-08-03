package com.theayushyadav11.loginandsignup

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.theayushyadav11.loginandsignup.databinding.EditDialogBinding
import com.theayushyadav11.loginandsignup.databinding.UserDetailsBinding

class UserDetails : AppCompatActivity() {
    lateinit var binding: UserDetailsBinding
    private lateinit var auth: FirebaseAuth
    lateinit var storageReference: StorageReference
    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null
    lateinit var progressDialog: ProgressDialog
    private lateinit var databaseReference: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=UserDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        listeners()
        fetchDetails()
        retrieveImageUrlsFromDatabase()


    }

    private fun fetchDetails() {
        databaseReference.child("name").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.value!=null) {
                    binding.textView.text = snapshot.value.toString()
                    val sharedPreferences = getSharedPreferences("tt", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("name", snapshot.value.toString())
                    editor.apply()
                }
            }
            override fun onCancelled(error: DatabaseError) {

            }

        })

        databaseReference.child("address").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                if(snapshot.value!=null)
                binding.tvAddress.text = snapshot.value.toString()
            }
            override fun onCancelled(error: DatabaseError) {

            }

        })

        databaseReference.child("phone").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.value!=null)
                binding.tvphone.text = snapshot.value.toString()
            }
            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

    fun init()
    {
        auth=FirebaseAuth.getInstance()
        storageReference=FirebaseStorage.getInstance().getReference("images/").child(auth.currentUser?.uid.toString())
        databaseReference = FirebaseDatabase.getInstance().reference.child("images").child(auth.currentUser?.uid.toString())
         progressDialog = ProgressDialog(this).apply {
            setMessage("Loading....")
            setCancelable(false)
        }

    }
    fun listeners()
    {
        binding.ivback.setOnClickListener{
            onBackPressed()
        }
        binding.addImage.setOnClickListener {
            openFileChooser()
        }
        binding.btnSignOut.setOnClickListener {
            signOut()

        }
        binding.ivPersonEdit.setOnClickListener{
            openDialog(1)
        }
        binding.ivAddressEdit.setOnClickListener{
            openDialog(2)
        }
        binding.ivphoneEdit.setOnClickListener{
            openDialog(3)
        }
        binding.ivAddress.setOnClickListener{
            startActivity(Intent(this@UserDetails,menu::class.java))
        }

    }
    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            imageUri = data.data

            uploadImageToFirebaseStorage(data.data!!)
            loadImage(imageUri)

        }
    }
    private fun uploadImageToFirebaseStorage(imageUri: Uri) {
       // progressDialog.setMessage("Uploading image...")

        val uploadTask = storageReference.putFile(imageUri)

        uploadTask.addOnSuccessListener {
            storageReference.downloadUrl.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()
                saveImageUrlToDatabase(imageUrl)
            }
        }.addOnFailureListener { exception ->
            // Handle unsuccessful uploads
            Log.e("FirebaseStorage", "Failed to upload image: ${exception.message}")
        }
    }
    private fun saveImageUrlToDatabase(imageUrl: String) {


        if (true) {
            databaseReference.child("profile").setValue(imageUrl)
                .addOnSuccessListener {

                    Log.d("FirebaseDatabase", "Image URL saved successfully")
                    toast("Profile picture updated successfully")
                }
                .addOnFailureListener { exception ->
                    Log.e("FirebaseDatabase", "Failed to save image URL: ${exception.message}")
                }
        }
    }
    private fun retrieveImageUrlsFromDatabase() {
        log("retriveing")
       progressDialog.setMessage("Loading...")

  //progressDialog.show()
        databaseReference.child("profile").addValueEventListener(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                log(dataSnapshot.toString())
                log("retriveing inside")

                    log("looping")
                    val imageUrl = dataSnapshot.value.toString()
                    if (imageUrl != null) {
                        log("got url")
                        loadImageIntoImageView(imageUrl)
                    }
                    else
                    {
                        log("nhi mila url")
                        binding.ivUser.setImageResource(R.drawable.person)
                        progressDialog.dismiss()
                    }




            }

            override fun onCancelled(databaseError: DatabaseError) {
                progressDialog.dismiss()
                // Handle possible errors
                Log.e("Ayush", "Failed to retrieve image URLs: ${databaseError.message}")

            }
        })
    }
        private fun loadImageIntoImageView(imageUrl: String) {
            if (isDestroyed || isFinishing) {
                progressDialog.dismiss()
                return
            }
           // progressDialog.show()
            val imageView: ImageView = findViewById(R.id.ivUser)

                Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.person) // Optional placeholder
                    .error(R.drawable.person)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: com.bumptech.glide.request.target.Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            progressDialog.dismiss()
                            return false // Important to return false so the error placeholder can be placed
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: com.bumptech.glide.request.target.Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            progressDialog.dismiss()
                            return false // Important to return false so the resource can be placed in the ImageView
                        }
                    })
                    .into(imageView)






        }

    private fun signOut() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

       val googleSignInClient = GoogleSignIn.getClient(this, gso)
        FirebaseAuth.getInstance().signOut()
        googleSignInClient.signOut().addOnCompleteListener { task ->
            // Google sign out completed
            toast("Signed out Successfully")
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    fun toast(s:String)
    {
        Toast.makeText(this, "$s", Toast.LENGTH_SHORT).show()
    }
    fun log(s:String)
    {
        Log.d("Ayush",s)
    }
    private fun loadImage(imageUri: Uri?) {
        imageUri?.let {
            Glide.with(this)
                .load(it)
                .placeholder(R.drawable.person) // Optional placeholder
                .error(R.drawable.person) // Optional error drawable
                .into(binding.ivUser)
        }
    }

    fun openDialog(i:Int)
    {
        val dialog = Dialog(this@UserDetails)
        val bind = EditDialogBinding.inflate(layoutInflater)
        dialog.setContentView(bind.root)
        dialog.setCancelable(false)
        var type=""
        when(i){
            1-> {
                type="name"
                bind.textInputLayout3.hint="Update name"
            }
            2->{
                type="address"
                bind.textInputLayout3.hint="Update address"
            }
            3->{
                type="phone"
                bind.textInputLayout3.hint="Update phone"
                bind.etUpdate.inputType = InputType.TYPE_CLASS_PHONE
            }
        }
 dialog.show()
        bind.cancel.setOnClickListener{
            dialog.dismiss()
        }
        bind.done.setOnClickListener{
            val value=bind.etUpdate.text.toString().trim()
            databaseReference.child(type).setValue(value).addOnCompleteListener {
                if(it.isSuccessful)
                {
                    toast(type.uppercase()+" Updated successfully")

                    when(i){
                        1-> {
                            binding.textView.text = value
                        }
                        2->{
                            binding.tvAddress.text = value
                        }
                        3->{
                            binding.tvphone.text = value
                        }
                    }
  dialog.dismiss()
                }
                else
                {
                    toast(it.exception?.message.toString())
                    dialog.dismiss()
                }
            }

        }


    }

}