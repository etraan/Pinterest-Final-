// Team Members: Ellie Traan, Kymmani Allen, Jazmyne Graham
package com.example.pinterestfinal

class Post {

    var id: Int = 0
    var title: String? = null
    var description: String? = null
    var imageResName: String? = null
    var author: String? = null
    var isLiked: Boolean = false

    // Constructor with id (from database)
    constructor(id: Int, title: String, description: String, imageResName: String, author: String, isLiked: Boolean) {
        this.id = id
        this.title = title
        this.description = description
        this.imageResName = imageResName
        this.author = author
        this.isLiked = isLiked
    }

    // Constructor without id (for new posts)
    constructor(title: String, description: String, imageResName: String, author: String) {
        this.title = title
        this.description = description
        this.imageResName = imageResName
        this.author = author
        this.isLiked = false
    }
}
