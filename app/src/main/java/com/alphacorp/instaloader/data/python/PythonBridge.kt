package com.alphacorp.instaloader.data.python

import com.alphacorp.instaloader.data.model.DownloadOptions
import com.alphacorp.instaloader.data.model.DownloadProgress
import com.alphacorp.instaloader.data.model.DownloadTargetType
import com.chaquo.python.PyObject
import com.chaquo.python.Python

class PythonBridge {

    private val python by lazy { Python.getInstance() }
    private val module by lazy { python.getModule("bridge") }
    private val builtins by lazy { python.getModule("builtins") }

    fun downloadWithOptions(
        targetType: DownloadTargetType,
        targetValue: String,
        options: DownloadOptions,
        baseDir: String,
        sessionUsername: String?,
        sessionDir: String?,
        onProgress: (DownloadProgress) -> Unit,
    ) {
        val callback = onProgress.toPythonCallback()
        module.callAttr(
            "download_with_options",
            targetType.pythonValue,
            targetValue,
            options.toPyDict(baseDir, sessionUsername, sessionDir),
            callback,
        )
    }

    fun getPostCount(username: String, sessionUsername: String?, sessionDir: String?): Int {
        return module.callAttr("get_post_count", username, sessionUsername, sessionDir)
            .toInt()
    }

    fun login(username: String, password: String, sessionDir: String) {
        module.callAttr("login", username, password, sessionDir)
    }

    fun twoFactorLogin(code: String, username: String, sessionDir: String) {
        module.callAttr("two_factor_login", code, username, sessionDir)
    }

    fun testSession(username: String, sessionDir: String): Boolean {
        return module.callAttr("test_session", username, sessionDir).toBoolean()
    }

    fun cancelDownload() {
        module.callAttr("cancel_download")
    }

    private fun ((DownloadProgress) -> Unit).toPythonCallback(): (
        String,
        String,
        String,
        String,
        String,
        String,
        String,
        String,
    ) -> Unit = { phase, total, completed, failed, skipped, remaining, current, message ->
        this(
            DownloadProgress(
                phase = phase,
                total = total.toIntOrNull(),
                completed = completed.toIntOrNull() ?: 0,
                failed = failed.toIntOrNull() ?: 0,
                skipped = skipped.toIntOrNull() ?: 0,
                remaining = remaining.toIntOrNull(),
                current = current,
                message = message,
            ),
        )
    }

    private fun DownloadOptions.toPyDict(
        baseDir: String,
        sessionUsername: String?,
        sessionDir: String?,
    ): PyObject {
        val entries = buildList<Pair<String, Any>> {
            add("quiet" to quiet)
            add("download_pictures" to downloadPictures)
            add("download_videos" to downloadVideos)
            add("download_video_thumbnails" to downloadVideoThumbnails)
            add("download_geotags" to downloadGeotags)
            add("download_comments" to downloadComments)
            add("save_metadata" to saveMetadata)
            add("compress_json" to compressJson)
            add("save_captions" to saveCaptions)
            add("post_metadata_txt_pattern" to postMetadataTxtPattern)
            add("storyitem_metadata_txt_pattern" to storyitemMetadataTxtPattern)
            add("profile_pic" to profilePic)
            add("posts" to posts)
            add("tagged" to tagged)
            add("igtv" to igtv)
            add("highlights" to highlights)
            add("stories" to stories)
            add("reels" to reels)
            add("fast_update" to fastUpdate)
            maxCount?.let { add("max_count" to it) }
            if (postFilter.isNotBlank()) add("post_filter" to postFilter)
            if (storyitemFilter.isNotBlank()) add("storyitem_filter" to storyitemFilter)
            if (dirnamePattern.isNotBlank()) add("dirname_pattern" to dirnamePattern)
            if (filenamePattern.isNotBlank()) add("filename_pattern" to filenamePattern)
            if (titlePattern.isNotBlank()) add("title_pattern" to titlePattern)
            add("sanitize_paths" to sanitizePaths)
            if (userAgent.isNotBlank()) add("user_agent" to userAgent)
            add("max_connection_attempts" to maxConnectionAttempts)
            add("request_timeout" to requestTimeout.toFloat())
            add("iphone_support" to iphoneSupport)
            add("enable_resume" to enableResume)
            add("resume_prefix" to resumePrefix)
            add("check_resume_bbd" to checkResumeBbd)
            if (slide.isNotBlank()) add("slide" to slide)
            add("base_dir" to baseDir)
            sessionUsername?.let { add("session_username" to it) }
            sessionDir?.let { add("session_dir" to it) }
        }
        val pairs: Array<Array<Any>> = entries
            .map { (key, value) -> arrayOf<Any>(key, value) }
            .toTypedArray()

        return builtins.callAttr("dict", pairs)
    }
}
