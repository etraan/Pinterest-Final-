// Team Members: Ellie Traan, Kymmani Allen, Jazmyne Graham
package com.example.pinterestfinal

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class NewPostActivity : AppCompatActivity() {

    private lateinit var dbHandler: PostDBHandler
    private lateinit var etNewTitle: EditText
    private lateinit var etNewDescription: EditText
    private lateinit var ivImagePreview: ImageView
    private lateinit var btnPickImage: Button
    private lateinit var btnSavePost: Button
    private lateinit var tvStatus: TextView

    // Stores the URI of the picked image as a string for the database
    private var selectedImageUri: String = ""

    // Photo picker launcher (modern, no permissions needed)
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            if (uri != null) {
                // Persist read permission across app restarts
                contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                selectedImageUri = uri.toString()
                ivImagePreview.setImageURI(uri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_new_post)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar: Toolbar = findViewById(R.id.toolbarNewPost)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.title_new_post)

        dbHandler        = PostDBHandler(this, null, null, 1)
        etNewTitle       = findViewById(R.id.etNewPostTitle)
        etNewDescription = findViewById(R.id.etNewPostDescription)
        ivImagePreview   = findViewById(R.id.ivNewPostPreview)
        btnPickImage     = findViewById(R.id.btnPickImage)
        btnSavePost      = findViewById(R.id.btnSavePost)
        tvStatus         = findViewById(R.id.tvNewPostStatus)

        btnPickImage.setOnClickListener {
            pickImageLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        btnSavePost.setOnClickListener { savePost() }
    }

    private fun savePost() {
        val title       = etNewTitle.text.toString().trim()
        val description = etNewDescription.text.toString().trim()

        if (title.isEmpty() || description.isEmpty()) {
            tvStatus.text = getString(R.string.error_empty_fields)
            return
        }

        // Use picked URI if available, otherwise fall back to placeholder drawable name
        val imageRef = if (selectedImageUri.isNotEmpty()) selectedImageUri else "post_placeholder"

        val post = Post(title, description, imageRef, getString(R.string.current_user))
        dbHandler.addPost(post)

        tvStatus.text = getString(R.string.post_saved)
        setResult(RESULT_OK)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
