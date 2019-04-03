package ru.breffi.smartlibrary.media

import ru.breffi.smartlibrary.feed.ProgressUpdateListener
import ru.breffi.story.domain.models.MediaFileEntity

interface MediaClickListener {
    fun onMediaClick(mediaFileEntity: MediaFileEntity, progressUpdateListener: ProgressUpdateListener)
}