package com.alphacorp.instaloader.domain.usecase

import com.alphacorp.instaloader.data.model.DownloadError
import com.alphacorp.instaloader.data.model.DownloadTarget

class ParseInstagramInputUseCase {

    operator fun invoke(rawInput: String): DownloadTarget {
        val input = rawInput.trim()
        if (input.isBlank()) {
            throw DownloadError.InvalidTarget("Enter a username, hashtag, or Instagram URL.")
        }

        if (input.startsWith("#")) {
            return DownloadTarget.Hashtag(input.removePrefix("#").trim())
        }

        if (input.contains("instagram.com", ignoreCase = true)) {
            extractShortcode(input)?.let { return DownloadTarget.Post(it) }
            extractProfileUsername(input)?.let { return DownloadTarget.Profile(it) }
            throw DownloadError.InvalidTarget("Could not parse Instagram URL.")
        }

        val username = input.removePrefix("@").trim().lowercase()
        if (username.isBlank() || username.contains(" ") || !USERNAME_PATTERN.matches(username)) {
            throw DownloadError.InvalidTarget("Enter a valid Instagram username.")
        }
        return DownloadTarget.Profile(username)
    }

    fun detectLabel(rawInput: String): String {
        return runCatching { invoke(rawInput) }
            .map { target ->
                when (target) {
                    is DownloadTarget.Profile -> "Profile"
                    is DownloadTarget.Post -> "Post / Reel"
                    is DownloadTarget.Hashtag -> "Hashtag"
                }
            }
            .getOrDefault("Unknown")
    }

    private fun extractShortcode(url: String): String? {
        val patterns = listOf(
            Regex("instagram\\.com/p/([^/?#]+)", RegexOption.IGNORE_CASE),
            Regex("instagram\\.com/reel/([^/?#]+)", RegexOption.IGNORE_CASE),
            Regex("instagram\\.com/reels/([^/?#]+)", RegexOption.IGNORE_CASE),
            Regex("instagram\\.com/tv/([^/?#]+)", RegexOption.IGNORE_CASE),
        )
        for (pattern in patterns) {
            val match = pattern.find(url) ?: continue
            return match.groupValues[1]
        }
        return null
    }

    private fun extractProfileUsername(url: String): String? {
        val match = PROFILE_URL_PATTERN.find(url) ?: return null
        val segment = match.groupValues[1].lowercase()
        if (segment in RESERVED_PATHS) return null
        if (!USERNAME_PATTERN.matches(segment)) return null
        return segment
    }

    companion object {
        private val USERNAME_PATTERN = Regex("^[A-Za-z0-9._]+\$")
        private val PROFILE_URL_PATTERN = Regex(
            "instagram\\.com/([^/?#]+)/?(?:[?#].*)?\$",
            RegexOption.IGNORE_CASE,
        )
        private val RESERVED_PATHS = setOf(
            "p", "reel", "reels", "tv", "stories", "explore", "accounts",
            "direct", "about", "legal", "api", "developer", "privacy",
        )
    }
}
