package com.alphacorp.instaloader.domain.usecase

import com.alphacorp.instaloader.data.model.DownloadError
import com.alphacorp.instaloader.data.model.DownloadTarget
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ParseInstagramInputUseCaseTest {

    private val useCase = ParseInstagramInputUseCase()

    @Test
    fun profileUsername_isParsed() {
        val target = useCase("instagram")
        assertTrue(target is DownloadTarget.Profile)
        assertEquals("instagram", (target as DownloadTarget.Profile).username)
    }

    @Test
    fun profileWithAtPrefix_isParsed() {
        val target = useCase("@nasa")
        assertEquals("nasa", (target as DownloadTarget.Profile).username)
    }

    @Test
    fun profileUrl_isParsed() {
        val target = useCase("https://www.instagram.com/nasa/")
        assertTrue(target is DownloadTarget.Profile)
        assertEquals("nasa", (target as DownloadTarget.Profile).username)
    }

    @Test
    fun postUrl_isParsed() {
        val target = useCase("https://www.instagram.com/p/ABC123/")
        assertTrue(target is DownloadTarget.Post)
        assertEquals("ABC123", (target as DownloadTarget.Post).shortcode)
    }

    @Test
    fun reelUrl_isParsed() {
        val target = useCase("https://www.instagram.com/reel/XYZ789/")
        assertEquals("XYZ789", (target as DownloadTarget.Post).shortcode)
    }

    @Test
    fun hashtag_isParsed() {
        val target = useCase("#cats")
        assertTrue(target is DownloadTarget.Hashtag)
        assertEquals("cats", (target as DownloadTarget.Hashtag).tag)
    }

    @Test(expected = DownloadError.InvalidTarget::class)
    fun blankInput_throws() {
        useCase("   ")
    }
}
