package com.example.pinterestfinal

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * PostFetchService - A background Service that fetches trending photo posts
 * from the Unsplash public API and stores them in the local SQLite database.
 * Mirrors the pattern taught in class: network download runs in a background
 * thread inside a Service so the UI is never blocked.
 */
class PostFetchService : Service() {

    companion object {
        private const val TAG = "PostFetchService"

        // Unsplash public API — no key needed for the demo endpoint
        // Returns a JSON array of photo objects
        private const val API_URL =
            "https://api.unsplash.com/photos?per_page=10&client_id=YOUR_UNSPLASH_ACCESS_KEY"

        // Broadcast action sent back to MainActivity when new posts are ready
        const val ACTION_POSTS_FETCHED = "com.example.pinterestapp.POSTS_FETCHED"
    }

    // onBind is required but not used — this is a started Service, not a bound one
    override fun onBind(intent: Intent?): IBinder? = null

    /**
     * Called when MainActivity starts the service via startService(intent).
     * We kick off a background thread here so network work never runs on the UI thread.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "PostFetchService started — launching fetch thread")

        // Run the network call on a background thread (same pattern as class demos)
        Thread {
            fetchAndStorePosts()
            // Stop the service when the work is done
            stopSelf()
        }.start()

        // START_NOT_STICKY: don't restart the service if the system kills it
        return START_NOT_STICKY
    }

    /**
     * Downloads photo data from the Unsplash API, parses the JSON array,
     * and saves each photo as a Post in the SQLite database.
     * Only saves posts that are not already stored (checks by title).
     */
    private fun fetchAndStorePosts() {
        try {
            // --- Step 1: Open HTTP connection to the API ---
            val url = URL(API_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000   // 10 second timeout
            connection.readTimeout    = 10000
            connection.connect()

            val responseCode = connection.responseCode
            Log.d(TAG, "API response code: $responseCode")

            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "API call failed with response code: $responseCode")
                return
            }

            // --- Step 2: Read the raw JSON response string ---
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val jsonString = reader.readText()
            reader.close()
            connection.disconnect()

            Log.d(TAG, "Raw JSON received: ${jsonString.take(200)}...")

            // --- Step 3: Parse the JSON array ---
            // Unsplash returns a JSONArray of photo objects at the top level
            val jsonArray = JSONArray(jsonString)
            Log.d(TAG, "Parsed ${jsonArray.length()} photo objects from JSON")

            // --- Step 4: Open DB and save each photo as a Post ---
            val dbHandler = PostDBHandler(this, null, null, 1)
            val existingPosts = dbHandler.getAllPosts()

            var addedCount = 0
            for (i in 0 until jsonArray.length()) {
                val photoObj = jsonArray.getJSONObject(i)

                // Extract fields from the JSON object
                // Unsplash photo object structure:
                // { "id": "...", "description": "...", "alt_description": "...",
                //   "urls": { "small": "..." }, "user": { "name": "..." } }
                val photoId      = photoObj.optString("id", "unknown")
                val description  = photoObj.optString("alt_description", "")
                    .ifEmpty { photoObj.optString("description", "A beautiful photo") }
                val imageUrl     = photoObj
                    .optJSONObject("urls")
                    ?.optString("small", "") ?: ""
                val authorName   = photoObj
                    .optJSONObject("user")
                    ?.optString("name", "Unsplash") ?: "Unsplash"

                // Use a cleaned-up version of description as the title
                val title = description
                    .split(" ")
                    .take(5)
                    .joinToString(" ")
                    .replaceFirstChar { it.uppercase() }
                    .ifEmpty { "Photo #${i + 1}" }

                // Only add if we don't already have a post with this title
                val alreadyExists = existingPosts.any { it.title == title }
                if (!alreadyExists && imageUrl.isNotEmpty()) {
                    // Store the full HTTPS image URL as imageResName —
                    // ImageUtils.loadImage() already handles content:// and https:// URIs
                    val post = Post(title, description.ifEmpty { "Photo from Unsplash" },
                        imageUrl, authorName)
                    dbHandler.addPost(post)
                    addedCount++
                    Log.d(TAG, "Saved post: $title by $authorName")
                }
            }

            Log.d(TAG, "Fetch complete. Added $addedCount new posts.")

            // --- Step 5: Broadcast back to MainActivity so it can refresh the feed ---
            val broadcastIntent = Intent(ACTION_POSTS_FETCHED)
            sendBroadcast(broadcastIntent)

        } catch (e: Exception) {
            // Network errors (no connection, timeout, bad JSON) are caught here
            Log.e(TAG, "Error fetching posts: ${e.message}")
        }
    }
}