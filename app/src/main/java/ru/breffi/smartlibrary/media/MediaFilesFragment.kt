package ru.breffi.smartlibrary.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_media_files.*
import ru.breffi.clm.ui.library.host.Navigation
import ru.breffi.smartlibrary.R
import ru.breffi.smartlibrary.main.FragmentPagerAdapter
import ru.breffi.story.domain.models.MediaFileEntity
import javax.inject.Inject

class MediaFilesFragment : Fragment(), MediaFilesView {

    private val ROTATE_TAG = "rotate"
    private val CURRENT_ITEM = "current_item"
    private var mCurrentPage = -1
    lateinit var fragmentPagerAdapter: FragmentPagerAdapter
    var allFilesFragment: FilesFragment? = null
    var pdfFilesFragment: FilesFragment? = null
    var movFilesFragment: FilesFragment? = null

    @Inject
    lateinit var mediaFilesPresenter: MainFilesPresenter

    companion object {
        const val PRESENTATION_ID_TAG = "PRESENTATION_ID_TAG"
        const val EMPTY_ID = -1

        fun newInstance(presentationId: Int): MediaFilesFragment {
            val args = Bundle()
            args.putInt(PRESENTATION_ID_TAG, presentationId)
            val fragment = MediaFilesFragment()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_media_files, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mediaFilesPresenter.mediaFilesView = this
        initViews()
        mediaFilesPresenter.retrieveMediaFiles(arguments?.getInt(PRESENTATION_ID_TAG, EMPTY_ID)?: EMPTY_ID)
    }

    private fun initViews() {
        if (mCurrentPage != -1 && pager != null) {
            pager.post {
                if (pager != null) {
                    pager.setCurrentItem(mCurrentPage, false)
                }
            }
        }
        if (pager != null) {
            pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

                }

                override fun onPageSelected(position: Int) {
                    mCurrentPage = position
                }

                override fun onPageScrollStateChanged(state: Int) {

                }
            })
        }
        backButton.setOnClickListener {
            if (activity is Navigation) {
                (activity as Navigation).back()
            }
        }
    }

    private fun initViewPager(mediaFiles: List<MediaFileEntity>) {
        fragmentPagerAdapter = FragmentPagerAdapter(childFragmentManager)
        allFilesFragment = FilesFragment.newInstance(ArrayList(mediaFiles), arguments?.getInt(PRESENTATION_ID_TAG, EMPTY_ID)?:EMPTY_ID)
        pdfFilesFragment = FilesFragment.newInstance(null, EMPTY_ID)
        movFilesFragment = FilesFragment.newInstance(null, EMPTY_ID)
        fragmentPagerAdapter.add(getString(R.string.all_files_tab), allFilesFragment)
        fragmentPagerAdapter.add(getString(R.string.pdf_files_tab), pdfFilesFragment)
        fragmentPagerAdapter.add(getString(R.string.mov_files_tab), movFilesFragment)
        pager.adapter = fragmentPagerAdapter
        tabs.setupWithViewPager(pager)
        pager.currentItem = 0
    }

    override fun showMediaFiles(mediaFiles: List<MediaFileEntity>) {
        initViewPager(mediaFiles)
    }
}
