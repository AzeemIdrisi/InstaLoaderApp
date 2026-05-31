package com.alphacorp.instaloader.data.model

data class DownloadProgress(
    val phase: String = "idle",
    val total: Int? = null,
    val completed: Int = 0,
    val failed: Int = 0,
    val skipped: Int = 0,
    val remaining: Int? = null,
    val current: String = "",
    val message: String = "",
) {
    val isActive: Boolean
        get() = phase !in setOf("idle", "finished", "cancelled", "error")

    val progressFraction: Float?
        get() {
            val totalCount = total ?: return null
            if (totalCount <= 0) return null
            return (completed + failed + skipped).toFloat() / totalCount.toFloat()
        }
}
