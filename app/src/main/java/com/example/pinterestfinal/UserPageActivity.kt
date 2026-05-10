// Team Members: Ellie Traan, Kymmani Allen, Jazmyne Graham
package com.example.pinterestfinal

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class UserPageActivity : AppCompatActivity() {

    private lateinit var dbHandler: PostDBHandler
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: UserPostAdapter
    private lateinit var tvNoPost: TextView

    private val editPostLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            refreshUserPosts()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.title_user_page)

        dbHandler  = PostDBHandler(this, null, null, 1)
        tvNoPost   = findViewById(R.id.tvNoUserPosts)
        recyclerView = findViewById(R.id.recyclerViewUserPosts)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = UserPostAdapter(
            posts = dbHandler.getPostsByAuthor(getString(R.string.current_user)).toMutableList(),
            onEditClicked = { post ->
                val intent = Intent(this, EditPostActivity::class.java)
                intent.putExtra(EditPostActivity.EXTRA_POST_ID, post.id)
                editPostLauncher.launch(intent)
            },
            onDeleteClicked = { post -> confirmDelete(post) }
        )
        recyclerView.adapter = adapter

        refreshUserPosts()

        // FAB: add a new post from user page too
        val fab: FloatingActionButton = findViewById(R.id.fabUserPage)
        fab.setOnClickListener {
            val intent = Intent(this, NewPostActivity::class.java)
            editPostLauncher.launch(intent)
        }
    }

    private fun confirmDelete(post: Post) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.confirm_delete_title))
            .setMessage(getString(R.string.confirm_delete_message, post.title))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                val result = dbHandler.deletePost(post)
                if (result) {
                    Toast.makeText(this, getString(R.string.post_deleted), Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    refreshUserPosts()
                } else {
                    Toast.makeText(this, getString(R.string.no_match_found), Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun refreshUserPosts() {
        val userPosts = dbHandler.getPostsByAuthor(getString(R.string.current_user)).toMutableList()
        adapter.updatePosts(userPosts)
        tvNoPost.visibility = if (userPosts.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
