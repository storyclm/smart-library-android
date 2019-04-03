package ru.breffi.smartlibrary.media

import ru.breffi.smartlibrary.feed.ProgressUpdateListener
import ru.breffi.story.domain.models.MediaFileEntity

interface FilesView {

    fun showMediaFiles(mediaFiles : ArrayList<MediaFileEntity>)
    fun showContentLoadingProgress(
        totalProgress: Int,
        calculateProgress: Int,
        progressUpdateListener: ProgressUpdateListener?
    )

    fun showDownloadDialog(mediaFileEntity: MediaFileEntity, progressUpdateListener: ProgressUpdateListener)
    fun updateMedia(mediaFiles: List<MediaFileEntity>, position: Int)
    fun playFile(path: String, fileName: String)
}