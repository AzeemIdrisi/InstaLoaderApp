package com.alphacorp.instaloader.data.model

data class DownloadOptions(
    val quiet: Boolean = true,
    val downloadPictures: Boolean = true,
    val downloadVideos: Boolean = true,
    val downloadVideoThumbnails: Boolean = true,
    val downloadGeotags: Boolean = false,
    val downloadComments: Boolean = false,
    val saveMetadata: Boolean = false,
    val compressJson: Boolean = true,
    val saveCaptions: Boolean = false,
    val postMetadataTxtPattern: String = "{caption}",
    val storyitemMetadataTxtPattern: String = "",
    val profilePic: Boolean = true,
    val posts: Boolean = true,
    val tagged: Boolean = false,
    val igtv: Boolean = false,
    val highlights: Boolean = false,
    val stories: Boolean = false,
    val reels: Boolean = false,
    val fastUpdate: Boolean = false,
    val maxCount: Int? = null,
    val postFilter: String = "",
    val storyitemFilter: String = "",
    val dirnamePattern: String = "",
    val filenamePattern: String = "",
    val titlePattern: String = "",
    val sanitizePaths: Boolean = false,
    val userAgent: String = "",
    val maxConnectionAttempts: Int = 3,
    val requestTimeout: Double = 300.0,
    val iphoneSupport: Boolean = true,
    val enableResume: Boolean = true,
    val resumePrefix: String = "iterator",
    val checkResumeBbd: Boolean = true,
    val slide: String = "",
) {
    companion object {
        val Default = DownloadOptions()
    }
}
