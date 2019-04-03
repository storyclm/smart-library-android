package ru.breffi.smartlibrary.media

import android.content.Context
import android.util.Log
import android.widget.Toast
import io.reactivex.disposables.CompositeDisposable
import ru.breffi.smartlibrary.R
import ru.breffi.smartlibrary.feed.ProgressUpdateListener
import ru.breffi.story.domain.interactors.MediaContentInteractor
import ru.breffi.story.domain.models.DownloadEntity
import ru.breffi.story.domain.models.MediaFileEntity
import java.util.*
import javax.inject.Inject

class FilesPresenter @Inject constructor(
    var mediaContentInteractor: MediaContentInteractor,
    var context: Context
) {
    lateinit var view: FilesView
    var mediaFiles: List<MediaFileEntity>? = null
    var presentationId: Int = 0
    var compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val progressUpdateListeners = HashMap<MediaFileEntity, ProgressUpdateListener>()

    fun initView(view: FilesView) {
        this.view = view
    }

    fun setupMediaFiles(mediaFiles: ArrayList<MediaFileEntity>?, presentationId: Int) {
        Log.e("setupMediaFiles", mediaFiles.toString())
        this.mediaFiles = mediaFiles
        this.presentationId = presentationId
        mediaFiles?.let {
            view.showMediaFiles(mediaFiles)
            listenContentLoading()
            listenDownloadFinish()
        }
    }

    fun loadMediaFile(mediaEntity: MediaFileEntity, progressUpdateListener: ProgressUpdateListener) {
        progressUpdateListeners[mediaEntity] = progressUpdateListener
        mediaContentInteractor.getMediaFileContent(mediaEntity, presentationId)
    }

    fun listenContentLoading() {
        Log.e("listenContentLoading", "listenContentLoading")
        compositeDisposable.add(
            mediaContentInteractor.listenMediaContentLoading()
                .subscribe(
                    { downloadEntity -> showCurrentProgress(downloadEntity) },
                    { t: Throwable -> handleError(t) })
        )
    }

    private fun showCurrentProgress(downloadEntity: DownloadEntity<MediaFileEntity>) {
        var total: Long = 0
        var progress: Long = 0
        for (i in 0 until downloadEntity.downloadsSparseArray.size()) {
            val key = downloadEntity.downloadsSparseArray.keyAt(i)
            val progressEntity = downloadEntity.downloadsSparseArray.get(key)
            Log.e(
                "progress",
                " total = [" + progressEntity.total + "], progress = [" + progressEntity.progress + "], downloadId = [" + i + "]"
            )
            total += progressEntity.total
            progress += progressEntity.progress
        }
        view.showContentLoadingProgress(
            100,
            calculateProgress(total, progress),
            progressUpdateListeners[downloadEntity.entity]
        )
    }

    private fun calculateProgress(total: Long, progress: Long): Int {
        val percent = (progress.toDouble() / total.toDouble() * 100).toInt()
        Log.e("progress", " percent = $percent")
        return percent
    }

    fun clickOnMedia(mediaFileEntity: MediaFileEntity, progressUpdateListener: ProgressUpdateListener) {
        if (mediaFileEntity.withContent) {
            view.playFile(context.getFilesDir()
                .toString()
                .plus("/storyCLM/")
                .plus(presentationId)
                .plus("/mediafiles"), mediaFileEntity.fileName)
        } else {
            view.showDownloadDialog(mediaFileEntity, progressUpdateListener)
        }
    }

    fun listenDownloadFinish() {
        compositeDisposable.add(
            mediaContentInteractor.listenMediaContentLoadingFinish()
                .subscribe({ mediaEntity ->
                    Log.e("listenDownloadFinish", " mediaEntity = " + mediaEntity.title)
                    mediaFiles?.let { view.updateMedia(it, it.indexOf(mediaEntity)) }
                }, { t: Throwable -> handleError(t) })
        )
    }

    private fun handleError(throwable: Throwable) {
        throwable.printStackTrace()
        Toast.makeText(context, context.getString(R.string.default_error), Toast.LENGTH_SHORT).show()
    }
}