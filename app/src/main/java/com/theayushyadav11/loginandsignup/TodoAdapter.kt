package com.theayushyadav11.loginandsignup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class TodoAdapter(
    private val todoList: List<Todo>,
    private val itemClickListener: onItemClickListener
): RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {
    interface onItemClickListener{
        fun deleteTodo(item: Todo)
        fun updateTodo(item: Todo)
    }
    inner class TodoViewHolder(itemView:View) : RecyclerView.ViewHolder(itemView)
    {
        val element=itemView.findViewById<TextView>(R.id.element)
        val edit=itemView.findViewById<ImageView>(R.id.delete)
        val main=itemView.findViewById<CardView>(R.id.main)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.listelement, parent, false)
        return TodoViewHolder(view)
    }

    override fun getItemCount()=todoList.size

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val todo=todoList[position]
        holder.element.text=todo.value
        holder.edit.setOnClickListener{itemClickListener.updateTodo(todo)   }
        holder.main.setOnLongClickListener { itemClickListener.deleteTodo(todo)
         true}

    }


}