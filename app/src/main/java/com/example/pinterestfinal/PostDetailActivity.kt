// Team Members: Ellie Traan, Kymmani Allen, Jazmyne Graham
package com.example.pinterestfinal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PostDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_POST_ID = "extra_post_id"
    }

    private lateinit var dbHandler: PostDBHandler
    private lateinit var currentPost: Post

    private lateinit var ivDetailImage: ImageView
    private lateinit var tvDetailTitle: TextView
    private lateinit var tvDetailDescription: TextView
    private lateinit var tvDetailAuthor: TextView
    private lateinit var ibDetailLike: ImageButton
    private lateinit var btnShare: Button
    private lateinit var tvLikeStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_post_detail)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Back button support
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.title_post_detail)

        dbHandler = PostDBHandler(this, null, null, 1)

        val postId = intent.getIntExtra(EXTRA_POST_ID, -1)
        if (postId == -1) { finish(); return }

        val found = dbHandler.findPost(postId)
        if (found == null) { finish(); return }
        currentPost = found

        // Wire views
        ivDetailImage      = findViewById(R.id.ivDetailImage)
        tvDetailTitle      = findViewById(R.id.tvDetailTitle)
        tvDetailDescription = findViewById(R.id.tvDetailDescription)
        tvDetailAuthor     = findViewById(R.id.tvDetailAuthor)
        ibDetailLike       = findViewById(R.id.ibDetailLike)
        btnShare           = findViewById(R.id.btnShare)
        tvLikeStatus       = findViewById(R.id.tvLikeStatus)

        populateViews()

        ibDetailLike.setOnClickListener { toggleLike() }
        btnShare.setOnClickListener { sharePost() }
    }

    private fun populateViews() {
        tvDetailTitle.text       = currentPost.title
        tvDetailDescription.text = currentPost.description
        tvDetailAuthor.text      = getString(R.string.label_author_prefix, currentPost.author)

        val resId = resources.getIdentifier(currentPost.imageResName, "drawable", packageName)
        ivDetailImage.setImageResource(if (resId != 0) resId else R.drawable.post_placeholder)

        updateLikeUI()
    }

    private fun updateLikeUI() {
        if (currentPost.isLiked) {
            ibDetailLike.setImageResource(R.drawable.ic_heart_filled)
            tvLikeStatus.text = getString(R.string.liked)
        } else {
            ibDetailLike.setImageResource(R.drawable.ic_heart_outline)
            tvLikeStatus.text = getString(R.string.not_liked)
        }
    }

    private fun toggleLike() {
        val newLiked = !currentPost.isLiked
        dbHandler.setLiked(currentPost.id, newLiked)
        currentPost.isLiked = newLiked
        updateLikeUI()

        val msg = if (newLiked) getString(R.string.post_liked) else getString(R.string.post_unliked)
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        setResult(RESULT_OK)
    }

    // Implicit intent: share post title + description to another app (Messages, etc.)
    private fun sharePost() {
        val shareText = getString(
            R.string.share_text_format,
            currentPost.title,
            currentPost.description
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, currentPost.title)
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)))
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
