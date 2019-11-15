package ru.breffi.smartlibrary.media

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_media_files.*
import ru.breffi.smartlibrary.R
import ru.breffi.smartlibrary.main.FragmentPagerAdapter
import ru.breffi.story.domain.models.MediaFileEntity
import javax.inject.Inject

class MediaFilesActivity : AppCompatActivity(), MediaFilesView {

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

        fun getIntent(context: Context, presentationId: Int): Intent {
            var intent = Intent(context, MediaFilesActivity::class.java)
            intent.putExtra(PRESENTATION_ID_TAG, presentationId)
            return intent
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
        setContentView(R.layout.activity_media_files)
        mediaFilesPresenter.mediaFilesView = this
        initViews()
        mediaFilesPresenter.retrieveMediaFiles(intent.getIntExtra(PRESENTATION_ID_TAG, EMPTY_ID))
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
        backButton.setOnClickListener { v -> finish() }
    }

    private fun initViewPager(mediaFiles: List<MediaFileEntity>) {
        fragmentPagerAdapter = FragmentPagerAdapter(supportFragmentManager)
        allFilesFragment =
            FilesFragment.newInstance(ArrayList(mediaFiles), intent.getIntExtra(PRESENTATION_ID_TAG, EMPTY_ID))
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
