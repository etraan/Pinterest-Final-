// Team Members: Ellie Traan, Kymmani Allen, Jazmyne Graham
package com.example.pinterestfinal

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.core.content.ContextCompat
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

    /**
     * BroadcastReceiver that listens for the ACTION_POSTS_FETCHED broadcast
     * sent by PostFetchService when it finishes downloading posts from the API.
     * On receipt, refreshes the RecyclerView feed with the new data.
     */
    private val postsFetchedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == PostFetchService.ACTION_POSTS_FETCHED) {
                // New posts arrived from the service — refresh the feed
                refreshFeed()
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.feed_refreshed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // ActivityResultLauncher — refreshes feed when any sub-activity returns
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

        // Set up toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Set up Navigation Drawer
        drawerLayout = findViewById(R.id.drawerLayout)
        val navView: NavigationView = findViewById(R.id.navView)
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.nav_open, R.string.nav_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navView.setNavigationItemSelectedListener(this)

        // Modern back press: close drawer first if open, then exit normally
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

        // Display welcome message from strings.xml
        val tvWelcome: TextView = findViewById(R.id.tvWelcome)
        tvWelcome.text = getString(R.string.welcome_message)

        // Initialize DB handler — seed preset posts only if DB is empty
        dbHandler = PostDBHandler(this, null, null, 1)
        if (dbHandler.getAllPosts().isEmpty()) {
            seedDatabase()
        }

        // Set up RecyclerView with 2-column staggered grid (Pinterest style)
        recyclerView = findViewById(R.id.recyclerViewFeed)
        recyclerView.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        adapter = PostAdapter(
            posts = dbHandler.getAllPosts().toMutableList(),
            onItemClicked = { post ->
                // Pass post id to PostDetailActivity via explicit intent
                val intent = Intent(this, PostDetailActivity::class.java)
                intent.putExtra(PostDetailActivity.EXTRA_POST_ID, post.id)
                activityLauncher.launch(intent)
            },
            onLikeClicked = { post, position ->
                // Toggle like in DB and update the card's heart icon immediately
                val newLiked = !post.isLiked
                dbHandler.setLiked(post.id, newLiked)
                adapter.updateLikeAt(position, newLiked)
                val msg = if (newLiked) getString(R.string.post_liked)
                else getString(R.string.post_unliked)
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
        )
        recyclerView.adapter = adapter

        // FAB: opens NewPostActivity to create a new post
        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
            val intent = Intent(this, NewPostActivity::class.java)
            activityLauncher.launch(intent)
        }

        // Start the background Service to fetch new posts from Unsplash API.
        // The service runs on a background thread and broadcasts back when done.
        startFetchService()
    }

    /**
     * Register the BroadcastReceiver when the activity becomes visible.
     * This ensures we only receive broadcasts while the app is in the foreground.
     */
    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(PostFetchService.ACTION_POSTS_FETCHED)
        ContextCompat.registerReceiver(
            this,
            postsFetchedReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    /**
     * Unregister the BroadcastReceiver when the activity goes to the background
     * to avoid memory leaks.
     */
    override fun onPause() {
        super.onPause()
        unregisterReceiver(postsFetchedReceiver)
    }

    // Inflate the toolbar menu
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

    // Handle navigation drawer item clicks
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home      -> { /* already on home */ }
            R.id.nav_user_page -> {
                activityLauncher.launch(Intent(this, UserPageActivity::class.java))
            }
            R.id.nav_liked_posts -> {
                activityLauncher.launch(Intent(this, LikedPostsActivity::class.java))
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    /**
     * Starts PostFetchService as a background service.
     * The service fetches posts from the Unsplash API, saves them to SQLite,
     * and broadcasts ACTION_POSTS_FETCHED when done so we can refresh the feed.
     */
    private fun startFetchService() {
        val serviceIntent = Intent(this, PostFetchService::class.java)
        startService(serviceIntent)
    }

    /** Reload the post list from the database and update the RecyclerView. */
    private fun refreshFeed() {
        adapter.updatePosts(dbHandler.getAllPosts().toMutableList())
    }

    /**
     * Seeds the database with 10 preset local posts on first launch.
     * These use drawable resource names stored in res/drawable/.
     */
    private fun seedDatabase() {
        val presetPosts = listOf(
            Post("Golden Hour", "A beautiful sunset!", "post_sunset", "PinBoard"),
            Post("Chicago", "A view of the Chicago river.", "post_city", "PinBoard"),
            Post("Professor Tarimo", "A CS professor at Connecticut College!", "post_tarimo", "PinBoard"),
            Post("Mountains", "Some cool looking mountains.", "post_mountain", "PinBoard"),
            Post("Ocean", "A picture of the Connecticut shore.", "post_ocean", "PinBoard"),
            Post("Cozy Corner", "A cool stock image of someone being cozy.", "post_cozy", "PinBoard"),
            Post("Starry Night", "One of Picasso's most famous paintings!", "post_art", "PinBoard"),
            Post("Conn Coll Arbo", "Picture of the arboretum in the fall.", "post_forest", "PinBoard"),
            Post("Desert Dunes", "A camel in a desert. #rollhumps", "post_desert", "PinBoard"),
            Post("Rainy Day", "Another cool stock image of some rain on an umbrella.", "post_rain", "PinBoard"),
            Post("Fresh Blooms", "Some pretty spring flowers.", "post_flowers", "PinBoard"),
        )
        presetPosts.forEach { dbHandler.addPost(it) }
    }
}
