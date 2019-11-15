package ru.breffi.smartlibrary.slides

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import dagger.android.support.AndroidSupportInjection
import de.blox.graphview.BaseGraphAdapter
import de.blox.graphview.Graph
import de.blox.graphview.ViewHolder
import de.blox.graphview.tree.BuchheimWalkerAlgorithm
import de.blox.graphview.tree.BuchheimWalkerConfiguration
import kotlinx.android.synthetic.main.fragment_slides_tree.*
import kotlinx.android.synthetic.main.graph_node.view.*
import ru.breffi.smartlibrary.R
import java.io.File
import javax.inject.Inject

class SlidesTreeFragment : Fragment(), SlidesTreeView {

    @Inject
    lateinit var presenter: SlidesTreePresenter

    companion object {
        const val PRES_ID = "PRES_ID"
        const val TAG = "SlidesTreeFragment"
        fun newInstance(presentationId: Int) =
            SlidesTreeFragment().apply {
                arguments = Bundle().apply {
                    putInt(PRES_ID, presentationId)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        var fragmentView = inflater.inflate(R.layout.fragment_slides_tree, container, false)
        return fragmentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.initView(this)
        presenter.initSlidesTree(arguments?.getInt(PRES_ID))
    }

    override fun showSlidesTree(graphData: Graph) {
// you can set the graph via the constructor or use the adapter.setGraph(Graph) method
        val adapter = object : BaseGraphAdapter<ViewHolder>(graphData) {
            lateinit var context: Context

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                context = parent.context
                val view = LayoutInflater.from(context).inflate(R.layout.graph_node, parent, false)
                return SimpleViewHolder(view)
            }

            override fun onBindViewHolder(viewHolder: ViewHolder, data: Any, position: Int) {
                (viewHolder as SimpleViewHolder).itemView.slideName.text = (data as SlideGraphModel).name

                Glide.with(context)
                    .load(Uri.fromFile(File(activity!!.filesDir.toString() + "/storyCLM/" + arguments?.getInt(PRES_ID) + "/screenshots" + "/${data.name.replace(".html", "")}.png")))
                    .apply( RequestOptions().override(192, 108))
                    .into(viewHolder.itemView.slideImage)
            }

            inner class SimpleViewHolder(view: View) : ViewHolder(view) {

            }
        }
        graph.adapter = adapter

        // set the algorithm here
        val configuration = BuchheimWalkerConfiguration.Builder()
            .setSiblingSeparation(100)
            .setLevelSeparation(300)
            .setSubtreeSeparation(300)
            .setOrientation(BuchheimWalkerConfiguration.ORIENTATION_TOP_BOTTOM)
            .build()
        adapter.setAlgorithm(BuchheimWalkerAlgorithm(configuration))
    }
}