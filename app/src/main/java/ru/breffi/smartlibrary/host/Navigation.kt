package ru.breffi.smartlibrary.host

import ru.breffi.story.domain.models.PresentationEntity
import java.util.*

interface Navigation {

    fun showMain()

    fun showContent(presentationEntity: PresentationEntity)

    fun showMedia(presentationId: Int)

    fun showSlides(presentationId: Int)

    fun showLoading(presentationIds: ArrayList<Int>)

    fun back(force: Boolean = false)
}