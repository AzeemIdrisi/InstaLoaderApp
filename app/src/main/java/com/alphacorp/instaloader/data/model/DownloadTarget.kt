package com.alphacorp.instaloader.data.model

sealed class DownloadTarget {
    abstract val displayName: String

    data class Profile(val username: String) : DownloadTarget() {
        override val displayName: String = username
    }

    data class Post(val shortcode: String) : DownloadTarget() {
        override val displayName: String = shortcode
    }

    data class Hashtag(val tag: String) : DownloadTarget() {
        override val displayName: String = tag
    }
}

enum class DownloadTargetType(val pythonValue: String) {
    PROFILE("profile"),
    POST("post"),
    REEL("reel"),
    HASHTAG("hashtag"),
}
