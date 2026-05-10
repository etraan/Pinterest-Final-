// Team Members: Ellie Traan, Kymmani Allen, Jazmyne Graham
package com.example.pinterestfinal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/**
 * PostDetailActivity — displays the full details of a selected post including
 * the image, title, author, and description. Allows the user to:
 *  - Like / unlike the post (stored in SQLite)
 *  - Share the post to another app via an implicit ACTION_SEND intent
 *  - View related content in a WebView via an explicit intent to WebViewActivity
 */
class PostDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_POST_ID = "extra_post_id"
    }

    private lateinit var dbHandler: PostDBHandler
    private lateinit var currentPost: Post

    // View references
    private lateinit var ivDetailImage: ImageView
    private lateinit var tvDetailTitle: TextView
    private lateinit var tvDetailDescription: TextView
    private lateinit var tvDetailAuthor: TextView
    private lateinit var ibDetailLike: ImageButton
    private lateinit var btnShare: Button
    private lateinit var btnViewOnWeb: Button
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

        // Set up toolbar with back arrow
        val toolbar: Toolbar = findViewById(R.id.toolbarPostDetail)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.title_post_detail)

        // Initialize DB handler using the same constructor style as the class demo
        dbHandler = PostDBHandler(this, null, null, 1)

        // Retrieve the post id passed via intent from MainActivity or LikedPostsActivity
        val postId = intent.getIntExtra(EXTRA_POST_ID, -1)
        if (postId == -1) { finish(); return }

        // Look up the full post object from the database using its id
        val found = dbHandler.findPost(postId)
        if (found == null) { finish(); return }
        currentPost = found

        // Wire up all views by id
        ivDetailImage       = findViewById(R.id.ivDetailImage)
        tvDetailTitle       = findViewById(R.id.tvDetailTitle)
        tvDetailDescription = findViewById(R.id.tvDetailDescription)
        tvDetailAuthor      = findViewById(R.id.tvDetailAuthor)
        ibDetailLike        = findViewById(R.id.ibDetailLike)
        btnShare            = findViewById(R.id.btnShare)
        btnViewOnWeb        = findViewById(R.id.btnViewOnWeb)
        tvLikeStatus        = findViewById(R.id.tvLikeStatus)

        // Populate all views with the post data
        populateViews()

        // Set up button click listeners
        ibDetailLike.setOnClickListener { toggleLike() }
        btnShare.setOnClickListener     { sharePost() }
        btnViewOnWeb.setOnClickListener { openWebView() }
    }

    /** Fills all views with data from the current post object. */
    private fun populateViews() {
        tvDetailTitle.text       = currentPost.title
        tvDetailDescription.text = currentPost.description
        tvDetailAuthor.text      = getString(R.string.label_author_prefix, currentPost.author)

        // ImageUtils handles drawable names, content:// URIs, and https:// URLs
        ImageUtils.loadImage(ivDetailImage, currentPost.imageResName)

        // Update heart icon and status text to match the current liked state
        updateLikeUI()
    }

    /** Updates the heart icon and status label to reflect the post's liked state. */
    private fun updateLikeUI() {
        if (currentPost.isLiked) {
            ibDetailLike.setImageResource(R.drawable.ic_heart_filled)
            tvLikeStatus.text = getString(R.string.liked)
        } else {
            ibDetailLike.setImageResource(R.drawable.ic_heart_outline)
            tvLikeStatus.text = getString(R.string.not_liked)
        }
    }

    /**
     * Toggles the like state of the current post in the SQLite database
     * and updates the UI immediately to reflect the change.
     */
    private fun toggleLike() {
        val newLiked = !currentPost.isLiked
        dbHandler.setLiked(currentPost.id, newLiked)
        currentPost.isLiked = newLiked
        updateLikeUI()
        // Signal MainActivity to refresh the feed when we return
        setResult(RESULT_OK)
    }

    /**
     * Fires an implicit ACTION_SEND intent to share the post title and
     * description with any app that supports sharing text (Messages, Gmail, etc.).
     * This is the implicit intent requirement from the project proposal.
     */
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
        // createChooser shows the app picker dialog (Messages, Gmail, etc.)
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_via)))
    }

    /**
     * Launches WebViewActivity with the post title as a search query.
     * Uses an explicit intent (same pattern as class slides) and passes
     * data via putExtra — WebViewActivity then builds a Pinterest search URL.
     */
    private fun openWebView() {
        val intent = Intent(this, WebViewActivity::class.java)
        intent.putExtra(WebViewActivity.EXTRA_POST_TITLE, currentPost.title)
        startActivity(intent)
    }

    // Handle toolbar back arrow — returns to the calling activity
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
