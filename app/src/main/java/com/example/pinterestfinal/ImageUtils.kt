// Team Members: Ellie Traan, Kymmani Allen, Jazmyne Graham
package com.example.pinterestfinal

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.ImageView
import androidx.core.net.toUri
import java.net.URL
import java.util.concurrent.Executors

/**
 * ImageUtils — a helper object for loading images into ImageViews.
 * Handles three types of image references:
 *   1. HTTPS URLs (from the Unsplash API) — downloaded on a background thread
 *   2. content:// or file:// URIs (from the device photo picker)
 *   3. Drawable resource names (for preset seed posts like "post_sunset")
 * Falls back to post_placeholder if none of the above resolve.
 */
object ImageUtils {

    private const val TAG = "ImageUtils"

    // Single-thread executor for background image downloads
    private val executor = Executors.newSingleThreadExecutor()

    /**
     * Loads an image into the given ImageView based on the type of [imageRef]:
     * - https:// → download from network on a background thread, post result to UI thread
     * - content:// or file:// → load from device URI
     * - anything else → treat as a drawable resource name
     */
    fun loadImage(imageView: ImageView, imageRef: String?) {
        if (imageRef.isNullOrEmpty()) {
            imageView.setImageResource(R.drawable.post_placeholder)
            return
        }

        when {
            // --- Case 1: Network URL from Unsplash API ---
            imageRef.startsWith("https://") || imageRef.startsWith("http://") -> {
                // Show placeholder while the real image loads
                imageView.setImageResource(R.drawable.post_placeholder)
                loadFromUrl(imageView, imageRef)
            }

            // --- Case 2: Local device URI from photo picker ---
            imageRef.startsWith("content://") || imageRef.startsWith("file://") -> {
                try {
                    imageView.setImageURI(imageRef.toUri())
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to load URI: $imageRef — ${e.message}")
                    imageView.setImageResource(R.drawable.post_placeholder)
                }
            }

            // --- Case 3: Drawable resource name (e.g. "post_sunset") ---
            else -> {
                val resId = imageView.context.resources.getIdentifier(
                    imageRef, "drawable", imageView.context.packageName
                )
                imageView.setImageResource(
                    if (resId != 0) resId else R.drawable.post_placeholder
                )
            }
        }
    }

    /**
     * Downloads an image from [url] on a background thread and
     * updates [imageView] on the main/UI thread when done.
     * Uses a simple executor + Handler pattern (no external libraries needed).
     */
    private fun loadFromUrl(imageView: ImageView, url: String) {
        executor.execute {
            try {
                // Download the bitmap on the background thread
                val bitmap: Bitmap = BitmapFactory.decodeStream(
                    URL(url).openConnection().getInputStream()
                )
                // Switch back to the UI thread to update the ImageView
                imageView.post {
                    imageView.setImageBitmap(bitmap)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to download image from $url: ${e.message}")
                // Fall back to placeholder on the UI thread
                imageView.post {
                    imageView.setImageResource(R.drawable.post_placeholder)
                }
            }
        }
    }
}
