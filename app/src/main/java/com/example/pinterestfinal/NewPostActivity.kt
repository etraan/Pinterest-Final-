// Team Members: Ellie Traan, Kymmani Allen, Jazmyne Graham
package com.example.pinterestfinal

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class NewPostActivity : AppCompatActivity() {

    private lateinit var dbHandler: PostDBHandler
    private lateinit var etNewTitle: EditText
    private lateinit var etNewDescription: EditText
    private lateinit var etNewImageRes: EditText
    private lateinit var btnSavePost: Button
    private lateinit var tvStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_new_post)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.title_new_post)

        dbHandler        = PostDBHandler(this, null, null, 1)
        etNewTitle       = findViewById(R.id.etNewPostTitle)
        etNewDescription = findViewById(R.id.etNewPostDescription)
        etNewImageRes    = findViewById(R.id.etNewPostImageRes)
        btnSavePost      = findViewById(R.id.btnSavePost)
        tvStatus         = findViewById(R.id.tvNewPostStatus)

        btnSavePost.setOnClickListener { savePost() }
    }

    private fun savePost() {
        val title       = etNewTitle.text.toString().trim()
        val description = etNewDescription.text.toString().trim()
        val imageRes    = etNewImageRes.text.toString().trim().ifEmpty { "post_placeholder" }

        if (title.isEmpty() || description.isEmpty()) {
            tvStatus.text = getString(R.string.error_empty_fields)
            return
        }

        val post = Post(title, description, imageRes, getString(R.string.current_user))
        dbHandler.addPost(post)

        tvStatus.text = getString(R.string.post_saved)
        etNewTitle.setText("")
        etNewDescription.setText("")
        etNewImageRes.setText("")

        setResult(RESULT_OK)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
