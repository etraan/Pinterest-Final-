// Team Members: Ellie Traan, Kymmani Allen, Jazmyne Graham
package com.example.pinterestfinal

import android.net.Uri
import android.widget.ImageView
import androidx.core.net.toUri

object ImageUtils {

    /**
     * Loads an image into an ImageView.
     * If [imageRef] starts with "content://" or "file://" it is treated as a URI
     * (photo picked from gallery). Otherwise it is treated as a drawable resource name.
     * Falls back to post_placeholder if neither resolves.
     */
    fun loadImage(imageView: ImageView, imageRef: String?) {
        if (imageRef.isNullOrEmpty()) {
            imageView.setImageResource(R.drawable.post_placeholder)
            return
        }

        if (imageRef.startsWith("content://") || imageRef.startsWith("file://")) {
            try {
                imageView.setImageURI(imageRef.toUri())
            } catch (e: Exception) {
                imageView.setImageResource(R.drawable.post_placeholder)
            }
        } else {
            val resId = imageView.context.resources.getIdentifier(
                imageRef, "drawable", imageView.context.packageName
            )
            imageView.setImageResource(if (resId != 0) resId else R.drawable.post_placeholder)
        }
    }
}
