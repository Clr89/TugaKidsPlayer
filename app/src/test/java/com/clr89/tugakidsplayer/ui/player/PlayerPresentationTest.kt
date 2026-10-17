package com.clr89.tugakidsplayer.ui.player

import androidx.media3.common.PlaybackException
import androidx.media3.ui.AspectRatioFrameLayout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotNull
import org.junit.Test

class PlayerPresentationTest {
    @Test
    fun presentationModesUseMedia3ResizeModes() {
        assertEquals(AspectRatioFrameLayout.RESIZE_MODE_FIT, VideoPresentation.FIT.resizeMode)
        assertEquals(AspectRatioFrameLayout.RESIZE_MODE_ZOOM, VideoPresentation.FILL.resizeMode)
        assertEquals(AspectRatioFrameLayout.RESIZE_MODE_FILL, VideoPresentation.STRETCH.resizeMode)
    }

    @Test
    fun presentationButtonCyclesFitFillStretchAndBackToFit() {
        assertEquals(VideoPresentation.FILL, VideoPresentation.FIT.next())
        assertEquals(VideoPresentation.STRETCH, VideoPresentation.FILL.next())
        assertEquals(VideoPresentation.FIT, VideoPresentation.STRETCH.next())
    }

    @Test
    fun externalPlaybackAcceptsOnlyValidHttpsUrls() {
        assertEquals("video/*", EXTERNAL_VIDEO_MIME_TYPE)
        assertEquals("https://example.org/video.mp4", externalPlaybackUrl("https://example.org/video.mp4"))
        assertEquals("https://example.org/video.mp4?token=private", externalPlaybackUrl("https://example.org/video.mp4?token=private"))
        assertEquals(null, externalPlaybackUrl("http://example.org/video.mp4"))
        assertEquals(null, externalPlaybackUrl("https://user:password@example.org/video.mp4"))
        assertEquals(null, externalPlaybackUrl("not a URL"))
    }

    @Test
    fun playbackUrlsMustBeAbsoluteHttpOrHttpsUrls() {
        assertNull(validatePlaybackUrl("https://example.org/video.mp4?key=secret"))
        assertNull(validatePlaybackUrl("http://example.org/video.mp4"))
        assertNotNull(validatePlaybackUrl("not a URL"))
        assertNotNull(validatePlaybackUrl("file:///video.mp4"))
        assertNotNull(validatePlaybackUrl("https://user:password@example.org/video.mp4"))
    }

    @Test
    fun media3ErrorCodesMapToUsefulFailureCategories() {
        assertEquals(PlaybackFailureKind.HTTP, classifyPlaybackFailure(PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS))
        assertEquals(PlaybackFailureKind.DECODING, classifyPlaybackFailure(PlaybackException.ERROR_CODE_DECODING_FAILED))
        assertEquals(PlaybackFailureKind.FORMAT_OR_CODEC, classifyPlaybackFailure(PlaybackException.ERROR_CODE_DECODING_FORMAT_UNSUPPORTED))
        assertEquals(PlaybackFailureKind.NETWORK, classifyPlaybackFailure(PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED))
        assertEquals(PlaybackFailureKind.OTHER, classifyPlaybackFailure(-1))
    }
}
