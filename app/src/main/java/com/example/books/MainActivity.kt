package com.example.books

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var titleEditText: EditText
    private lateinit var dateEditText: EditText
    private lateinit var addButton: Button
    private lateinit var searchView: SearchView
    private lateinit var searchResultTextView: TextView
    private lateinit var randomGenreButton: Button
    private lateinit var displayTextView: TextView
    private lateinit var displayBooksButton: Button
    private lateinit var allBooksTextView: TextView

    private val genres = listOf("Fantasy", "Science Fiction", "Mystery", "Thriller", "Romance", "Historical", "Non-Fiction")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        titleEditText = findViewById(R.id.titleEditText)
        dateEditText = findViewById(R.id.dateEditText)
        addButton = findViewById(R.id.addButton)
        searchView = findViewById(R.id.searchView)
        searchResultTextView = findViewById(R.id.searchResultTextView)
        randomGenreButton = findViewById(R.id.randomGenreButton)
        displayTextView = findViewById(R.id.displayTextView)
        displayBooksButton = findViewById(R.id.displayBooksButton)
        allBooksTextView = findViewById(R.id.allBooksTextView)

        addButton.setOnClickListener {
            val title = titleEditText.text.toString().trim()
            val date = dateEditText.text.toString().trim()

            if (title.isNotEmpty() && date.isNotEmpty()) {
                addBookToFirestore(title, date)
            } else {
                Toast.makeText(this, "Title and date cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        randomGenreButton.setOnClickListener {
            displayRandomGenre()
        }

        displayBooksButton.setOnClickListener {
            displayAllBooks()
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    searchBooks(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }

    private fun addBookToFirestore(title: String, date: String) {
        val user = auth.currentUser
        if (user != null) {
            val book = hashMapOf("title" to title, "date" to date)
            firestore.collection("users").document(user.uid).collection("books")
                .add(book)
                .addOnSuccessListener {
                    Toast.makeText(this, "Book added successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to add book", Toast.LENGTH_SHORT).show()
                }
        }
    }



    private fun displayRandomGenre() {
        val randomGenre = genres.random()
        displayTextView.text = "Suggested Genre: $randomGenre"
    }

    private fun displayAllBooks() {
        val user = auth.currentUser
        if (user != null) {
            firestore.collection("users").document(user.uid).collection("books")
                .get()
                .addOnSuccessListener { documents ->
                    val books = StringBuilder()
                    for (document in documents) {
                        val title = document.getString("title") ?: "No Title"
                        val date = document.getString("date") ?: "No Date"
                        books.append("Title: $title, Date: $date\n")
                    }
                    allBooksTextView.text = books.toString()
                }
                .addOnFailureListener {
                    allBooksTextView.text = "Error retrieving books"
                }
        }
    }

    private fun searchBooks(query: String) {
        val user = auth.currentUser
        if (user != null) {
            firestore.collection("users").document(user.uid).collection("books")
                .whereEqualTo("title", query)
                .get()
                .addOnSuccessListener { documents ->
                    val searchResults = StringBuilder()
                    for (document in documents) {
                        val title = document.getString("title") ?: "No Title"
                        val date = document.getString("date") ?: "No Date"
                        searchResults.append("Title: $title, Date: $date\n")
                    }

                    if (searchResults.isNotEmpty()) {
                        searchResultTextView.text = searchResults.toString()
                    } else {
                        searchResultTextView.text = "No books found."
                    }
                }
                .addOnFailureListener {
                    searchResultTextView.text = "Error in searching books"
                }
        }
    }
}
