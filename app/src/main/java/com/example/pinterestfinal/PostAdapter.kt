// Team Members: Ellie Traan, Kymmani Allen, Jazmyne Graham
package com.example.pinterestfinal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PostAdapter(
    private var posts: List<Post>,
    private val onItemClicked: (Post) -> Unit,
    private val onLikeClicked: (Post, Int) -> Unit
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivPostImage: ImageView   = itemView.findViewById(R.id.ivPostImage)
        val tvPostTitle: TextView    = itemView.findViewById(R.id.tvPostTitle)
        val tvPostAuthor: TextView   = itemView.findViewById(R.id.tvPostAuthor)
        val ibLike: ImageButton      = itemView.findViewById(R.id.ibLike)

        fun bind(post: Post, position: Int) {
            tvPostTitle.text  = post.title
            tvPostAuthor.text = post.author

            // Resolve drawable by name, fall back to placeholder
            val resId = itemView.context.resources.getIdentifier(
                post.imageResName, "drawable", itemView.context.packageName
            )
            ivPostImage.setImageResource(if (resId != 0) resId else R.drawable.post_placeholder)

            // Heart icon: filled if liked
            ibLike.setImageResource(
                if (post.isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
            )

            itemView.setOnClickListener { onItemClicked(post) }
            ibLike.setOnClickListener { onLikeClicked(post, position) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(posts[position], position)
    }

    override fun getItemCount() = posts.size

    fun updatePosts(newPosts: List<Post>) {
        posts = newPosts
        notifyDataSetChanged()
    }

    fun updateLikeAt(position: Int, liked: Boolean) {
        (posts as? MutableList)?.get(position)?.isLiked = liked
        notifyItemChanged(position)
    }
}
