// Team Members: Ellie Traan, Kymmani Allen, Jazmyne Graham
package com.example.pinterestfinal

import android.content.Intent
import android.net.Uri
import android.os.Bundle
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

class EditPostActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_POST_ID = "extra_post_id"
    }

    private lateinit var dbHandler: PostDBHandler
    private lateinit var currentPost: Post

    private lateinit var ivEditPreview: ImageView
    private lateinit var etEditTitle: EditText
    private lateinit var etEditDescription: EditText
    private lateinit var btnPickImage: Button
    private lateinit var btnUpdatePost: Button
    private lateinit var tvEditStatus: TextView

    // Tracks if user picked a new image; empty means keep existing
    private var selectedImageUri: String = ""

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            if (uri != null) {
                contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                selectedImageUri = uri.toString()
                ivEditPreview.setImageURI(uri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_post)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val toolbar: Toolbar = findViewById(R.id.toolbarEditPost)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.title_edit_post)

        dbHandler = PostDBHandler(this, null, null, 1)

        val postId = intent.getIntExtra(EXTRA_POST_ID, -1)
        if (postId == -1) { finish(); return }

        val found = dbHandler.findPost(postId)
        if (found == null) { finish(); return }
        currentPost = found

        ivEditPreview     = findViewById(R.id.ivEditPreview)
        etEditTitle       = findViewById(R.id.etEditTitle)
        etEditDescription = findViewById(R.id.etEditDescription)
        btnPickImage      = findViewById(R.id.btnPickImageEdit)
        btnUpdatePost     = findViewById(R.id.btnUpdatePost)
        tvEditStatus      = findViewById(R.id.tvEditStatus)

        populateFields()

        btnPickImage.setOnClickListener {
            pickImageLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        btnUpdatePost.setOnClickListener { updatePost() }
    }

    private fun populateFields() {
        etEditTitle.setText(currentPost.title)
        etEditDescription.setText(currentPost.description)
        // Show existing image
        ImageUtils.loadImage(ivEditPreview, currentPost.imageResName)
    }

    private fun updatePost() {
        val newTitle = etEditTitle.text.toString().trim()
        val newDesc  = etEditDescription.text.toString().trim()

        if (newTitle.isEmpty() || newDesc.isEmpty()) {
            tvEditStatus.text = getString(R.string.error_empty_fields)
            return
        }

        currentPost.title       = newTitle
        currentPost.description = newDesc
        // Only update image if user picked a new one
        if (selectedImageUri.isNotEmpty()) {
            currentPost.imageResName = selectedImageUri
        }

        val result = dbHandler.updatePost(currentPost)
        if (result) {
            tvEditStatus.text = getString(R.string.post_updated)
            setResult(RESULT_OK)
            finish()
        } else {
            tvEditStatus.text = getString(R.string.no_match_found)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
