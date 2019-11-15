package ru.breffi.smartlibrary.media

import ru.breffi.story.domain.interactors.PresentationInteractor
import ru.breffi.story.domain.models.MediaFileEntity
import ru.breffi.story.domain.models.PresentationEntity
import javax.inject.Inject

class MainFilesPresenter @Inject constructor(var presentationInteractor : PresentationInteractor) {

    lateinit var mediaFilesView: MediaFilesView
    lateinit var mediaFiles: List<MediaFileEntity>

    fun retrieveMediaFiles(presentationId : Int){
        presentationInteractor.getPresentation(presentationId)
            .subscribe(
                {presentationOptional ->
                    if (presentationOptional.isPresent) {
                        handleMediaFiles(presentationOptional.get())
                    }
                },
                { t: Throwable -> t.printStackTrace() }
            )
    }

    private fun handleMediaFiles(presentation: PresentationEntity) {
        mediaFiles = presentation.mediaFiles
        mediaFilesView.showMediaFiles(mediaFiles)
    }
}