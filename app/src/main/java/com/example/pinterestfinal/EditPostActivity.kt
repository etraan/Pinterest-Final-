// Team Members: Ellie Traan, Kymmani Allen, Jazmyne Graham
package com.example.pinterestfinal

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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
    private lateinit var etEditImageRes: EditText
    private lateinit var btnUpdatePost: Button
    private lateinit var tvEditStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_post)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.title_edit_post)

        dbHandler = PostDBHandler(this, null, null, 1)

        val postId = intent.getIntExtra(EXTRA_POST_ID, -1)
        if (postId == -1) { finish(); return }

        val found = dbHandler.findPost(postId)
        if (found == null) { finish(); return }
        currentPost = found

        ivEditPreview    = findViewById(R.id.ivEditPreview)
        etEditTitle      = findViewById(R.id.etEditTitle)
        etEditDescription = findViewById(R.id.etEditDescription)
        etEditImageRes   = findViewById(R.id.etEditImageRes)
        btnUpdatePost    = findViewById(R.id.btnUpdatePost)
        tvEditStatus     = findViewById(R.id.tvEditStatus)

        populateFields()

        btnUpdatePost.setOnClickListener { updatePost() }
    }

    private fun populateFields() {
        etEditTitle.setText(currentPost.title)
        etEditDescription.setText(currentPost.description)
        etEditImageRes.setText(currentPost.imageResName)

        val resId = resources.getIdentifier(currentPost.imageResName, "drawable", packageName)
        ivEditPreview.setImageResource(if (resId != 0) resId else R.drawable.post_placeholder)
    }

    private fun updatePost() {
        val newTitle = etEditTitle.text.toString().trim()
        val newDesc  = etEditDescription.text.toString().trim()
        val newImage = etEditImageRes.text.toString().trim().ifEmpty { "post_placeholder" }

        if (newTitle.isEmpty() || newDesc.isEmpty()) {
            tvEditStatus.text = getString(R.string.error_empty_fields)
            return
        }

        currentPost.title       = newTitle
        currentPost.description = newDesc
        currentPost.imageResName = newImage

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
