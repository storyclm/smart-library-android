package ru.breffi.clm.ui.library.host

import ru.breffi.story.domain.models.PresentationEntity

interface Navigation {

    fun showMain()

    fun showContent(presentationEntity: PresentationEntity)

    fun showMedia(presentationId: Int)

    fun showSlides(presentationId: Int)

    fun back()
}