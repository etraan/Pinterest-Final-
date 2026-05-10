// Team Members: Ellie Traan, Kymmani Allen, Jazmyne Graham
package com.example.pinterestfinal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserPostAdapter(
    private var posts: List<Post>,
    private val onEditClicked: (Post) -> Unit,
    private val onDeleteClicked: (Post) -> Unit
) : RecyclerView.Adapter<UserPostAdapter.UserPostViewHolder>() {

    inner class UserPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivPostImage: ImageView = itemView.findViewById(R.id.ivUserPostImage)
        val tvPostTitle: TextView  = itemView.findViewById(R.id.tvUserPostTitle)
        val tvPostDesc: TextView   = itemView.findViewById(R.id.tvUserPostDesc)
        val btnEdit: Button        = itemView.findViewById(R.id.btnEditPost)
        val btnDelete: Button      = itemView.findViewById(R.id.btnDeletePost)

        fun bind(post: Post) {
            tvPostTitle.text = post.title
            tvPostDesc.text  = post.description

            val resId = itemView.context.resources.getIdentifier(
                post.imageResName, "drawable", itemView.context.packageName
            )
            ivPostImage.setImageResource(if (resId != 0) resId else R.drawable.post_placeholder)

            btnEdit.setOnClickListener   { onEditClicked(post) }
            btnDelete.setOnClickListener { onDeleteClicked(post) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserPostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_post, parent, false)
        return UserPostViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserPostViewHolder, position: Int) {
        holder.bind(posts[position])
    }

    override fun getItemCount() = posts.size

    fun updatePosts(newPosts: List<Post>) {
        posts = newPosts
        notifyDataSetChanged()
    }
}
