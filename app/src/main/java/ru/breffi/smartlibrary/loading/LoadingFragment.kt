package ru.breffi.smartlibrary.loading

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_loading.*
import ru.breffi.clm.ui.library.host.BackConsumer
import ru.breffi.clm.ui.loading.LoadingView
import ru.breffi.smartlibrary.R
import ru.breffi.smartlibrary.host.Navigation
import javax.inject.Inject


class LoadingFragment : Fragment(), LoadingView, BackConsumer {

    @Inject
    lateinit var loadingPresenter: LoadingPresenter


    companion object {
        const val PRESENTATIONS_IDS = "presentation_ids"
        const val TAG = "LoadingFragment"

        fun newInstance(presentationIds : ArrayList<Int>) = LoadingFragment().apply {
            arguments = Bundle().apply {
                putIntegerArrayList(PRESENTATIONS_IDS, presentationIds)
            }
        }
    }

    override fun showProgress(progress: Float) {
        val progressPercent = (progress * 100).toInt()
        loadingProgressTextView.text = "$progressPercent%"
    }

    override fun close() {
        if (activity is Navigation) {
            (activity as Navigation).back(true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_loading, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadingPresenter.attach(this)

        arguments?.getIntegerArrayList(PRESENTATIONS_IDS)?.let {
            loadingPresenter.loadPresentationsContent(it)
        }
    }

    override fun onDestroyView() {
        loadingPresenter.detach()
        super.onDestroyView()
    }

    override fun onBackPressed(): Boolean {
        return true
    }
}
