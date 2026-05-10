// Team Members: Ellie Traan, Kymmani Allen, Jazmyne Graham
package com.example.pinterestfinal

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class LikedPostsActivity : AppCompatActivity() {

    private lateinit var dbHandler: PostDBHandler
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PostAdapter
    private lateinit var tvNoLiked: TextView

    private val detailLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            refreshLikedPosts()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_liked_posts)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.title_liked_posts)

        dbHandler    = PostDBHandler(this, null, null, 1)
        tvNoLiked    = findViewById(R.id.tvNoLikedPosts)
        recyclerView = findViewById(R.id.recyclerViewLikedPosts)
        recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        adapter = PostAdapter(
            posts = dbHandler.getLikedPosts().toMutableList(),
            onItemClicked = { post ->
                val intent = Intent(this, PostDetailActivity::class.java)
                intent.putExtra(PostDetailActivity.EXTRA_POST_ID, post.id)
                detailLauncher.launch(intent)
            },
            onLikeClicked = { post, position ->
                // Un-liking from this screen removes it from the list
                dbHandler.setLiked(post.id, false)
                Toast.makeText(this, getString(R.string.post_unliked), Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                refreshLikedPosts()
            }
        )
        recyclerView.adapter = adapter

        refreshLikedPosts()
    }

    private fun refreshLikedPosts() {
        val liked = dbHandler.getLikedPosts().toMutableList()
        adapter.updatePosts(liked)
        tvNoLiked.visibility = if (liked.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
