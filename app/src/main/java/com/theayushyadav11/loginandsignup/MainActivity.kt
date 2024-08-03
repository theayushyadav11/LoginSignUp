package com.theayushyadav11.loginandsignup

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.theayushyadav11.loginandsignup.databinding.ActivityMainBinding
import com.theayushyadav11.loginandsignup.databinding.EditDialogBinding
import com.theayushyadav11.loginandsignup.databinding.ListelementBinding

class MainActivity : AppCompatActivity(), TodoAdapter.onItemClickListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var databaseref:DatabaseReference
    private lateinit var database: FirebaseDatabase
    private var list: ArrayList<Todo> = arrayListOf()
    private lateinit var todoRecyclerView: RecyclerView
    private lateinit var todoAdapter: TodoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        fetchTodos()
        listeners()














    }

    private fun init() {
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        this.databaseref = database.reference.child("tasks").child(firebaseAuth.currentUser?.uid.toString())
        todoRecyclerView=binding.rv
        todoRecyclerView.layoutManager=LinearLayoutManager(this)
        todoAdapter= TodoAdapter(list,this)
        todoRecyclerView.adapter=todoAdapter
        val sharedPreferences = getSharedPreferences("tt", Context.MODE_PRIVATE)
        val n= firebaseAuth.currentUser?.displayName
            binding.tvHeading.text ="Hi, "+sharedPreferences.getString("name",n)?.uppercase()

    }
    private fun listeners(){
        binding.btnAdd.setOnClickListener {
            addTodo()
        }

        binding.btnDetails.setOnClickListener {
            startActivity(Intent(this@MainActivity,UserDetails::class.java))
        }
    }
    private fun fetchTodos() {
        databaseref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                list.clear()
                for (data in snapshot.children) {
                    if(data!=null)
                    {
                        val todo=Todo(data.key.toString(), data.value.toString())
                        list.add(todo)
                    }
                }
                list.reverse()
                todoAdapter.notifyDataSetChanged()
                binding.pb.isVisible=false
                binding.rv.isVisible=true

            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MainActivity", "DatabaseError: ${error.message}")
            }
        })
    }

    private fun addTodo()
    {
        val todo = binding.etTodo.text.toString().trim()

        if (todo.isNotEmpty()) {


            if (!list.any { it.value == todo }) {


                databaseref.push().setValue(todo).addOnCompleteListener {
                    if (it.isSuccessful) {
                        toast("Todo added successfully")
                        binding.etTodo.text = null
                    } else {
                        toast(it.exception?.message.toString())
                    }
                }
            } else {
                toast("Todo Already Exists")
            }
        } else {
            toast("todo cannot be empty")
        }
    }
    //*************************************************************************************************************
    //SIGN OUT FROM GOOGLE

    //*************************************************************************************************************
    fun toast(s: String) {
        Toast.makeText(this, "$s", Toast.LENGTH_SHORT).show()
    }



    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()

    }

    override fun deleteTodo(item: Todo) {
        val  dialog= AlertDialog.Builder(this@MainActivity)
            .setTitle("Delete Confirmation")
            .setMessage("Are you sure you want to delete this item?")
            .setPositiveButton("Yes") { dialog, which ->

                item.key?.let {
                    databaseref.child(it).removeValue()
                        .addOnSuccessListener { Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show() }
                        .addOnFailureListener { Log.e("MainActivity", "Failed to delete: ${it.message}") }
                }
            }
            .setNegativeButton("No", null)
            .create()

        dialog.show()


    }

    override fun updateTodo(todo: Todo) {
        val dialog = Dialog(this@MainActivity)
        val bind = EditDialogBinding.inflate(layoutInflater)
        dialog.setContentView(bind.root)
        dialog.setCancelable(false)
        bind.etUpdate.setText(todo.value)

        bind.done.setOnClickListener {
            val updatedValue = bind.etUpdate.text.toString().trim()
            val updates: MutableMap<String, Any> = HashMap()
            updates[todo.key] = bind.etUpdate.text.toString()
            if(updatedValue.isNotEmpty()) {
                databaseref.updateChildren(updates).addOnCompleteListener {
                    if (it.isSuccessful) {
                        toast("Updated Successfully")
                    } else {
                        toast(it.exception?.message.toString())
                    }
                    dialog.dismiss()
                }
            }
            else
            {
                toast("Todo cannot be empty")
            }

        }
        dialog.show()


    }
    }
