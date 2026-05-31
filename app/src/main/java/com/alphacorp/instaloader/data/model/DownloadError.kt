package com.alphacorp.instaloader.data.model

sealed class DownloadError(val userMessage: String) : Exception(userMessage) {
    data class LoginRequired(val detail: String = "") :
        DownloadError("Login required. Sign in from the Login screen.")

    data class PrivateProfile(val detail: String = "") :
        DownloadError("This profile is private. Log in with an account that follows it.")

    data class RateLimited(val detail: String = "") :
        DownloadError("Instagram rate limit reached. Wait a few minutes and try again.")

    data class Network(val detail: String = "") :
        DownloadError("Network error. Check your connection and try again.")

    data class InvalidTarget(val detail: String = "") :
        DownloadError(detail.ifBlank { "Invalid username or URL." })

    data class Cancelled(val detail: String = "") :
        DownloadError("Download cancelled.")

    data class TwoFactorRequired(val detail: String = "") :
        DownloadError("Two-factor authentication code required.")

    data class BadCredentials(val detail: String = "") :
        DownloadError("Incorrect username or password.")

    data class ProfileNotFound(val detail: String = "") :
        DownloadError(
            detail.ifBlank {
                "Profile not found. If the account exists, Instagram may be blocking anonymous " +
                    "access — log in from the Login screen and try again."
            },
        )

    data class Unknown(val detail: String) :
        DownloadError(detail.ifBlank { "Something went wrong." })
}

fun Throwable.toDownloadError(): DownloadError {
    val message = generateSequence(this) { it.cause }
        .mapNotNull { it.message }
        .joinToString(" ")
        .ifBlank { toString() }

    return when {
        message.contains("LoginRequired", ignoreCase = true) ||
            message.contains("login required", ignoreCase = true) ->
            DownloadError.LoginRequired(message)

        message.contains("PrivateProfile", ignoreCase = true) ||
            message.contains("private profile", ignoreCase = true) ->
            DownloadError.PrivateProfile(message)

        message.contains("TwoFactor", ignoreCase = true) ||
            message.contains("two-factor", ignoreCase = true) ||
            message.contains("2fa", ignoreCase = true) ->
            DownloadError.TwoFactorRequired(message)

        message.contains("BadCredentials", ignoreCase = true) ||
            message.contains("wrong password", ignoreCase = true) ->
            DownloadError.BadCredentials(message)

        message.contains("429", ignoreCase = true) ||
            message.contains("TooManyRequests", ignoreCase = true) ||
            message.contains("rate limit", ignoreCase = true) ->
            DownloadError.RateLimited(message)

        message.contains("Connection", ignoreCase = true) ||
            message.contains("network", ignoreCase = true) ||
            message.contains("timeout", ignoreCase = true) ->
            DownloadError.Network(message)

        message.contains("blocked anonymous", ignoreCase = true) ||
            message.contains("blocked this request", ignoreCase = true) ->
            DownloadError.LoginRequired(message)

        message.contains("ProfileNotExists", ignoreCase = true) ||
            (message.contains("does not exist", ignoreCase = true) &&
                message.contains("profile", ignoreCase = true)) ->
            DownloadError.ProfileNotFound(message)

        message.contains("cancelled", ignoreCase = true) ||
            this is InterruptedException ->
            DownloadError.Cancelled(message)

        else -> DownloadError.Unknown(message)
    }
}
