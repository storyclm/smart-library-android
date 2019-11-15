package ru.breffi.smartlibrary.slides

import android.content.Context
import android.util.Log
import com.annimon.stream.Optional
import de.blox.graphview.Graph
import de.blox.graphview.Node
import ru.breffi.story.domain.interactors.PresentationInteractor
import ru.breffi.story.domain.models.PresentationEntity
import ru.breffi.story.domain.models.SlideEntity
import javax.inject.Inject

class SlidesTreePresenter @Inject constructor(
    var context: Context,
    var presentationInteractor: PresentationInteractor
) {
    lateinit var view: SlidesTreeView
    var graph = Graph()
    lateinit var presentation: PresentationEntity

    fun initView(view: SlidesTreeView) {
        this.view = view
    }

    fun initSlidesTree(presId: Int?) {
        presId?.let {
            presentationInteractor.getPresentation(presId)
                .doOnNext { presentationOptional ->
                    if (presentationOptional.isPresent) {
                        presentation = presentationOptional.get()
                    }
                }
                .map { presentationOptional -> buildTree(presentationOptional) }
                .subscribe({ graph -> view.showSlidesTree(graph) }, { t: Throwable -> t.printStackTrace() })
        }
    }

    private fun buildTree(presentationOptional: Optional<PresentationEntity>): Graph {
        if (presentationOptional.isPresent) {
            buildTreeNode(findFirstSlide(presentationOptional.get().slides))
        }
        return graph
    }

    private fun findFirstSlide(slides: List<SlideEntity>): SlideEntity {
        var slideEntity = SlideEntity()
        slides.forEach { slide ->
            if (slide.name == "index.html") {
                slideEntity = slide
            }
        }
        return slideEntity
    }

    fun buildTreeNode(slideEntity: SlideEntity) {
        if(slideEntity.name != null) {
            var node = Node(SlideGraphModel(slideEntity.name, "imageUrl"))
            Log.e("edge", "parent node ${slideEntity.name}")
            graph.addNode(node)
            val linkedSlideList = slideEntity.linkedSlides.split(",").toList()
            if (linkedSlideList.size > 1) {
                for (slideName in linkedSlideList) {
                    Log.e("edge", "child node $slideName")
                    if (graph.getEdge(node, Node(SlideGraphModel(slideName, "imageUrl"))) == null && graph.getEdge(
                            Node(
                                SlideGraphModel(slideName, "imageUrl")
                            ), node
                        ) == null
                    ) {
                        val edge = graph.addEdge(node, Node(SlideGraphModel(slideName, "imageUrl")))
                        Log.e("edge", "${edge.source.data} -> ${edge.destination.data}")
                        buildTreeNode(findSlideByName(slideName))
                    }
                }
            }
        }
    }

    private fun findSlideByName(slideName: String): SlideEntity {
        var slideEntity = SlideEntity()
        for (slide in presentation.slides) {
            if (slide.name == slideName) {
                slideEntity = slide
            }
        }
        return slideEntity
    }
}