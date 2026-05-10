// Team Members: Ellie Traan, Kymmani Allen, Jazmyne Graham
package com.example.pinterestfinal

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var dbHandler: PostDBHandler
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PostAdapter
    private lateinit var drawerLayout: DrawerLayout

    // Refresh when returning from any sub-activity
    private val activityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            refreshFeed()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Navigation Drawer
        drawerLayout = findViewById(R.id.drawerLayout)
        val navView: NavigationView = findViewById(R.id.navView)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.nav_open, R.string.nav_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navView.setNavigationItemSelectedListener(this)

        // Modern back press handling: close drawer first if open
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        // Welcome message
        val tvWelcome: TextView = findViewById(R.id.tvWelcome)
        tvWelcome.text = getString(R.string.welcome_message)

        // Database — seed only on first run
        dbHandler = PostDBHandler(this, null, null, 1)
        if (dbHandler.getAllPosts().isEmpty()) {
            seedDatabase()
        }

        // RecyclerView with staggered grid (Pinterest style: 2 columns)
        recyclerView = findViewById(R.id.recyclerViewFeed)
        recyclerView.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        adapter = PostAdapter(
            posts = dbHandler.getAllPosts().toMutableList(),
            onItemClicked = { post ->
                val intent = Intent(this, PostDetailActivity::class.java)
                intent.putExtra(PostDetailActivity.EXTRA_POST_ID, post.id)
                activityLauncher.launch(intent)
            },
            onLikeClicked = { post, position ->
                val newLiked = !post.isLiked
                dbHandler.setLiked(post.id, newLiked)
                adapter.updateLikeAt(position, newLiked)
                val msg = if (newLiked) getString(R.string.post_liked) else getString(R.string.post_unliked)
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
        )
        recyclerView.adapter = adapter

        // FAB: New Post
        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(this, NewPostActivity::class.java)
            activityLauncher.launch(intent)
        }
    }

    // Toolbar menu (extra: could add search or filter later)
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_new_post -> {
                val intent = Intent(this, NewPostActivity::class.java)
                activityLauncher.launch(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Navigation Drawer item selection
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> { /* already here */ }
            R.id.nav_user_page -> {
                val intent = Intent(this, UserPageActivity::class.java)
                activityLauncher.launch(intent)
            }
            R.id.nav_liked_posts -> {
                val intent = Intent(this, LikedPostsActivity::class.java)
                activityLauncher.launch(intent)
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun refreshFeed() {
        adapter.updatePosts(dbHandler.getAllPosts().toMutableList())
    }

    private fun seedDatabase() {
        val presetPosts = listOf(
            Post("Golden Hour", "A beautiful sunset over the mountains.", "post_sunset", "PinBoard"),
            Post("City Lights", "The city skyline at night.", "post_city", "PinBoard"),
            Post("Fresh Blooms", "A colorful bouquet of spring flowers.", "post_flowers", "PinBoard"),
            Post("Mountain Trail", "Hiking through misty peaks.", "post_mountain", "PinBoard"),
            Post("Ocean Waves", "Peaceful waves crashing on the shore.", "post_ocean", "PinBoard"),
            Post("Cozy Corner", "A perfect reading nook with warm lighting.", "post_cozy", "PinBoard"),
            Post("Street Art", "Vibrant murals in the heart of the city.", "post_art", "PinBoard"),
            Post("Forest Walk", "Tall trees and dappled sunlight.", "post_forest", "PinBoard"),
            Post("Desert Dunes", "Endless golden sand dunes.", "post_desert", "PinBoard"),
            Post("Rainy Day", "Raindrops on a window pane.", "post_rain", "PinBoard")
        )
        presetPosts.forEach { dbHandler.addPost(it) }
    }
}
