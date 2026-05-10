// Team Members: Ellie Traan, Kymmani Allen, Jazmyne Graham
package com.example.pinterestfinal

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class PostDBHandler(context: Context, name: String?,
                    factory: SQLiteDatabase.CursorFactory?, version: Int) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val CREATE_POSTS_TABLE = ("CREATE TABLE " +
                TABLE_POSTS + "(" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_TITLE + " TEXT," +
                COLUMN_DESCRIPTION + " TEXT," +
                COLUMN_IMAGE_RES + " TEXT," +
                COLUMN_AUTHOR + " TEXT," +
                COLUMN_IS_LIKED + " INTEGER" + ")")
        db.execSQL(CREATE_POSTS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_POSTS")
        onCreate(db)
    }

    // Insert a new post
    fun addPost(post: Post) {
        val values = ContentValues()
        values.put(COLUMN_TITLE, post.title)
        values.put(COLUMN_DESCRIPTION, post.description)
        values.put(COLUMN_IMAGE_RES, post.imageResName)
        values.put(COLUMN_AUTHOR, post.author)
        values.put(COLUMN_IS_LIKED, if (post.isLiked) 1 else 0)

        val db = this.writableDatabase
        db.insert(TABLE_POSTS, null, values)
        db.close()
    }

    // Find a single post by id
    fun findPost(id: Int): Post? {
        val query = "SELECT * FROM $TABLE_POSTS WHERE $COLUMN_ID = $id"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)
        var post: Post? = null

        if (cursor.moveToFirst()) {
            cursor.moveToFirst()
            val postId      = Integer.parseInt(cursor.getString(0))
            val title       = cursor.getString(1)
            val description = cursor.getString(2)
            val imageRes    = cursor.getString(3)
            val author      = cursor.getString(4)
            val isLiked     = cursor.getInt(5) == 1
            post = Post(postId, title, description, imageRes, author, isLiked)
            cursor.close()
        }
        db.close()
        return post
    }

    // Get all posts
    fun getAllPosts(): List<Post> {
        val posts = mutableListOf<Post>()
        val query = "SELECT * FROM $TABLE_POSTS"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val id          = Integer.parseInt(cursor.getString(0))
                val title       = cursor.getString(1)
                val description = cursor.getString(2)
                val imageRes    = cursor.getString(3)
                val author      = cursor.getString(4)
                val isLiked     = cursor.getInt(5) == 1
                posts.add(Post(id, title, description, imageRes, author, isLiked))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return posts
    }

    // Get only liked posts
    fun getLikedPosts(): List<Post> {
        val posts = mutableListOf<Post>()
        val query = "SELECT * FROM $TABLE_POSTS WHERE $COLUMN_IS_LIKED = 1"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val id          = Integer.parseInt(cursor.getString(0))
                val title       = cursor.getString(1)
                val description = cursor.getString(2)
                val imageRes    = cursor.getString(3)
                val author      = cursor.getString(4)
                posts.add(Post(id, title, description, imageRes, author, true))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return posts
    }

    // Get only posts by a specific author (for User Page)
    fun getPostsByAuthor(author: String): List<Post> {
        val posts = mutableListOf<Post>()
        val query = "SELECT * FROM $TABLE_POSTS WHERE $COLUMN_AUTHOR = \"$author\""
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val id          = Integer.parseInt(cursor.getString(0))
                val title       = cursor.getString(1)
                val description = cursor.getString(2)
                val imageRes    = cursor.getString(3)
                val isLiked     = cursor.getInt(5) == 1
                posts.add(Post(id, title, description, imageRes, author, isLiked))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return posts
    }

    // Update an existing post
    fun updatePost(post: Post): Boolean {
        var result = false
        val query = "SELECT * FROM $TABLE_POSTS WHERE $COLUMN_ID = ${post.id}"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            val values = ContentValues()
            values.put(COLUMN_TITLE, post.title)
            values.put(COLUMN_DESCRIPTION, post.description)
            values.put(COLUMN_IMAGE_RES, post.imageResName)
            values.put(COLUMN_AUTHOR, post.author)
            values.put(COLUMN_IS_LIKED, if (post.isLiked) 1 else 0)
            db.update(TABLE_POSTS, values, "$COLUMN_ID = ?", arrayOf(post.id.toString()))
            cursor.close()
            result = true
        }
        db.close()
        return result
    }

    // Toggle like status for a post
    fun setLiked(postId: Int, liked: Boolean): Boolean {
        var result = false
        val query = "SELECT * FROM $TABLE_POSTS WHERE $COLUMN_ID = $postId"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            val values = ContentValues()
            values.put(COLUMN_IS_LIKED, if (liked) 1 else 0)
            db.update(TABLE_POSTS, values, "$COLUMN_ID = ?", arrayOf(postId.toString()))
            cursor.close()
            result = true
        }
        db.close()
        return result
    }

    // Delete a post by id
    fun deletePost(post: Post): Boolean {
        var result = false
        val query = "SELECT * FROM $TABLE_POSTS WHERE $COLUMN_ID = ${post.id}"
        val db = this.writableDatabase
        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            val id = Integer.parseInt(cursor.getString(0))
            db.delete(TABLE_POSTS, "$COLUMN_ID = ?", arrayOf(id.toString()))
            cursor.close()
            result = true
        }
        db.close()
        return result
    }

    companion object {
        private val DATABASE_VERSION = 1
        private val DATABASE_NAME = "pinterestDB.db"
        val TABLE_POSTS = "posts"
        val COLUMN_ID = "_id"
        val COLUMN_TITLE = "title"
        val COLUMN_DESCRIPTION = "description"
        val COLUMN_IMAGE_RES = "image_res"
        val COLUMN_AUTHOR = "author"
        val COLUMN_IS_LIKED = "is_liked"
    }
}
