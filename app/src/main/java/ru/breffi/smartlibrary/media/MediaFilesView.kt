package ru.breffi.smartlibrary.media

import ru.breffi.story.domain.models.MediaFileEntity

interface MediaFilesView {
    fun showMediaFiles(mediaFiles: List<MediaFileEntity>)

}