package com.alphacorp.instaloader.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.alphacorp.instaloader.data.model.DownloadOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.optionsDataStore: DataStore<Preferences> by preferencesDataStore(name = "download_options")

class OptionsStore(private val context: Context) {

    val options: Flow<DownloadOptions> = context.optionsDataStore.data.map { prefs ->
        DownloadOptions(
            quiet = prefs[Keys.QUIET] ?: DownloadOptions.Default.quiet,
            downloadPictures = prefs[Keys.DOWNLOAD_PICTURES] ?: DownloadOptions.Default.downloadPictures,
            downloadVideos = prefs[Keys.DOWNLOAD_VIDEOS] ?: DownloadOptions.Default.downloadVideos,
            downloadVideoThumbnails = prefs[Keys.DOWNLOAD_VIDEO_THUMBNAILS] ?: DownloadOptions.Default.downloadVideoThumbnails,
            downloadGeotags = prefs[Keys.DOWNLOAD_GEOTAGS] ?: DownloadOptions.Default.downloadGeotags,
            downloadComments = prefs[Keys.DOWNLOAD_COMMENTS] ?: DownloadOptions.Default.downloadComments,
            saveMetadata = prefs[Keys.SAVE_METADATA] ?: DownloadOptions.Default.saveMetadata,
            compressJson = prefs[Keys.COMPRESS_JSON] ?: DownloadOptions.Default.compressJson,
            saveCaptions = prefs[Keys.SAVE_CAPTIONS] ?: DownloadOptions.Default.saveCaptions,
            postMetadataTxtPattern = prefs[Keys.POST_METADATA_TXT_PATTERN] ?: DownloadOptions.Default.postMetadataTxtPattern,
            storyitemMetadataTxtPattern = prefs[Keys.STORYITEM_METADATA_TXT_PATTERN] ?: DownloadOptions.Default.storyitemMetadataTxtPattern,
            profilePic = prefs[Keys.PROFILE_PIC] ?: DownloadOptions.Default.profilePic,
            posts = prefs[Keys.POSTS] ?: DownloadOptions.Default.posts,
            tagged = prefs[Keys.TAGGED] ?: DownloadOptions.Default.tagged,
            igtv = prefs[Keys.IGTV] ?: DownloadOptions.Default.igtv,
            highlights = prefs[Keys.HIGHLIGHTS] ?: DownloadOptions.Default.highlights,
            stories = prefs[Keys.STORIES] ?: DownloadOptions.Default.stories,
            reels = prefs[Keys.REELS] ?: DownloadOptions.Default.reels,
            fastUpdate = prefs[Keys.FAST_UPDATE] ?: DownloadOptions.Default.fastUpdate,
            maxCount = prefs[Keys.MAX_COUNT]?.takeIf { it > 0 },
            postFilter = prefs[Keys.POST_FILTER] ?: DownloadOptions.Default.postFilter,
            storyitemFilter = prefs[Keys.STORYITEM_FILTER] ?: DownloadOptions.Default.storyitemFilter,
            dirnamePattern = prefs[Keys.DIRNAME_PATTERN] ?: DownloadOptions.Default.dirnamePattern,
            filenamePattern = prefs[Keys.FILENAME_PATTERN] ?: DownloadOptions.Default.filenamePattern,
            titlePattern = prefs[Keys.TITLE_PATTERN] ?: DownloadOptions.Default.titlePattern,
            sanitizePaths = prefs[Keys.SANITIZE_PATHS] ?: DownloadOptions.Default.sanitizePaths,
            userAgent = prefs[Keys.USER_AGENT] ?: DownloadOptions.Default.userAgent,
            maxConnectionAttempts = prefs[Keys.MAX_CONNECTION_ATTEMPTS] ?: DownloadOptions.Default.maxConnectionAttempts,
            requestTimeout = prefs[Keys.REQUEST_TIMEOUT]?.toDouble() ?: DownloadOptions.Default.requestTimeout,
            iphoneSupport = prefs[Keys.IPHONE_SUPPORT] ?: DownloadOptions.Default.iphoneSupport,
            enableResume = prefs[Keys.ENABLE_RESUME] ?: DownloadOptions.Default.enableResume,
            resumePrefix = prefs[Keys.RESUME_PREFIX] ?: DownloadOptions.Default.resumePrefix,
            checkResumeBbd = prefs[Keys.CHECK_RESUME_BBD] ?: DownloadOptions.Default.checkResumeBbd,
            slide = prefs[Keys.SLIDE] ?: DownloadOptions.Default.slide,
        )
    }

    suspend fun save(options: DownloadOptions) {
        context.optionsDataStore.edit { prefs ->
            prefs[Keys.QUIET] = options.quiet
            prefs[Keys.DOWNLOAD_PICTURES] = options.downloadPictures
            prefs[Keys.DOWNLOAD_VIDEOS] = options.downloadVideos
            prefs[Keys.DOWNLOAD_VIDEO_THUMBNAILS] = options.downloadVideoThumbnails
            prefs[Keys.DOWNLOAD_GEOTAGS] = options.downloadGeotags
            prefs[Keys.DOWNLOAD_COMMENTS] = options.downloadComments
            prefs[Keys.SAVE_METADATA] = options.saveMetadata
            prefs[Keys.COMPRESS_JSON] = options.compressJson
            prefs[Keys.SAVE_CAPTIONS] = options.saveCaptions
            prefs[Keys.POST_METADATA_TXT_PATTERN] = options.postMetadataTxtPattern
            prefs[Keys.STORYITEM_METADATA_TXT_PATTERN] = options.storyitemMetadataTxtPattern
            prefs[Keys.PROFILE_PIC] = options.profilePic
            prefs[Keys.POSTS] = options.posts
            prefs[Keys.TAGGED] = options.tagged
            prefs[Keys.IGTV] = options.igtv
            prefs[Keys.HIGHLIGHTS] = options.highlights
            prefs[Keys.STORIES] = options.stories
            prefs[Keys.REELS] = options.reels
            prefs[Keys.FAST_UPDATE] = options.fastUpdate
            prefs[Keys.MAX_COUNT] = options.maxCount ?: -1
            prefs[Keys.POST_FILTER] = options.postFilter
            prefs[Keys.STORYITEM_FILTER] = options.storyitemFilter
            prefs[Keys.DIRNAME_PATTERN] = options.dirnamePattern
            prefs[Keys.FILENAME_PATTERN] = options.filenamePattern
            prefs[Keys.TITLE_PATTERN] = options.titlePattern
            prefs[Keys.SANITIZE_PATHS] = options.sanitizePaths
            prefs[Keys.USER_AGENT] = options.userAgent
            prefs[Keys.MAX_CONNECTION_ATTEMPTS] = options.maxConnectionAttempts
            prefs[Keys.REQUEST_TIMEOUT] = options.requestTimeout.toInt()
            prefs[Keys.IPHONE_SUPPORT] = options.iphoneSupport
            prefs[Keys.ENABLE_RESUME] = options.enableResume
            prefs[Keys.RESUME_PREFIX] = options.resumePrefix
            prefs[Keys.CHECK_RESUME_BBD] = options.checkResumeBbd
            prefs[Keys.SLIDE] = options.slide
        }
    }

    suspend fun reset() {
        save(DownloadOptions.Default)
    }

    private object Keys {
        val QUIET = booleanPreferencesKey("quiet")
        val DOWNLOAD_PICTURES = booleanPreferencesKey("download_pictures")
        val DOWNLOAD_VIDEOS = booleanPreferencesKey("download_videos")
        val DOWNLOAD_VIDEO_THUMBNAILS = booleanPreferencesKey("download_video_thumbnails")
        val DOWNLOAD_GEOTAGS = booleanPreferencesKey("download_geotags")
        val DOWNLOAD_COMMENTS = booleanPreferencesKey("download_comments")
        val SAVE_METADATA = booleanPreferencesKey("save_metadata")
        val COMPRESS_JSON = booleanPreferencesKey("compress_json")
        val SAVE_CAPTIONS = booleanPreferencesKey("save_captions")
        val POST_METADATA_TXT_PATTERN = stringPreferencesKey("post_metadata_txt_pattern")
        val STORYITEM_METADATA_TXT_PATTERN = stringPreferencesKey("storyitem_metadata_txt_pattern")
        val PROFILE_PIC = booleanPreferencesKey("profile_pic")
        val POSTS = booleanPreferencesKey("posts")
        val TAGGED = booleanPreferencesKey("tagged")
        val IGTV = booleanPreferencesKey("igtv")
        val HIGHLIGHTS = booleanPreferencesKey("highlights")
        val STORIES = booleanPreferencesKey("stories")
        val REELS = booleanPreferencesKey("reels")
        val FAST_UPDATE = booleanPreferencesKey("fast_update")
        val MAX_COUNT = intPreferencesKey("max_count")
        val POST_FILTER = stringPreferencesKey("post_filter")
        val STORYITEM_FILTER = stringPreferencesKey("storyitem_filter")
        val DIRNAME_PATTERN = stringPreferencesKey("dirname_pattern")
        val FILENAME_PATTERN = stringPreferencesKey("filename_pattern")
        val TITLE_PATTERN = stringPreferencesKey("title_pattern")
        val SANITIZE_PATHS = booleanPreferencesKey("sanitize_paths")
        val USER_AGENT = stringPreferencesKey("user_agent")
        val MAX_CONNECTION_ATTEMPTS = intPreferencesKey("max_connection_attempts")
        val REQUEST_TIMEOUT = intPreferencesKey("request_timeout")
        val IPHONE_SUPPORT = booleanPreferencesKey("iphone_support")
        val ENABLE_RESUME = booleanPreferencesKey("enable_resume")
        val RESUME_PREFIX = stringPreferencesKey("resume_prefix")
        val CHECK_RESUME_BBD = booleanPreferencesKey("check_resume_bbd")
        val SLIDE = stringPreferencesKey("slide")
    }
}
