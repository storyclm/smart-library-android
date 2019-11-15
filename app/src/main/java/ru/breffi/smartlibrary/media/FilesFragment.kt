package ru.breffi.smartlibrary.media

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.android.support.AndroidSupportInjection
import ru.breffi.smartlibrary.R
import ru.breffi.smartlibrary.feed.ProgressUpdateListener
import ru.breffi.smartlibrary.media.MediaFilesActivity.Companion.EMPTY_ID
import ru.breffi.story.domain.models.MediaFileEntity
import java.io.File
import javax.inject.Inject


class FilesFragment : Fragment(), FilesView, MediaClickListener {

    @Inject
    lateinit var filesPresenter: FilesPresenter
    lateinit var adapter: MediaRecyclerAdapter
    lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var fragmentView: View
    lateinit var mediaRecyclerView: RecyclerView

    companion object {
        const val MEDIA_TAG = "MEDIA_TAG"
        const val PRES_ID = "PRES_ID"
        fun newInstance(mediaFiles: ArrayList<MediaFileEntity>?, presentationId: Int) =
            FilesFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(MEDIA_TAG, mediaFiles)
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
        fragmentView = inflater.inflate(R.layout.fragment_media_files, container, false)
        filesPresenter.initView(this)
        initViews()
        arguments?.let {
            filesPresenter.setupMediaFiles(
                it.getParcelableArrayList<MediaFileEntity>(MEDIA_TAG),
                it.getInt(PRES_ID, EMPTY_ID)
            )
        }
        return fragmentView
    }

    private fun initViews() {
        adapter = MediaRecyclerAdapter(this)
        initLayoutManager()
        mediaRecyclerView = fragmentView.findViewById(R.id.mediaRecyclerView)
        mediaRecyclerView.adapter = adapter
        mediaRecyclerView.layoutManager = layoutManager
    }

    private fun initLayoutManager() {
        val isTablet = resources.getBoolean(R.bool.isTablet)
        layoutManager = if (isTablet) {
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                GridLayoutManager(context, 4)
            } else {
                GridLayoutManager(context, 3)
            }
        } else {
            if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                GridLayoutManager(context, 3)
            } else {
                GridLayoutManager(context, 2)
            }
        }
    }

    override fun showMediaFiles(mediaFiles: ArrayList<MediaFileEntity>) {
        adapter.data = mediaFiles
        adapter.notifyDataSetChanged()
    }

    override fun showContentLoadingProgress(
        totalProgress: Int,
        calculateProgress: Int,
        progressUpdateListener: ProgressUpdateListener?
    ) {
        progressUpdateListener?.progressUpdate(totalProgress, calculateProgress)
    }

    override fun onMediaClick(mediaFileEntity: MediaFileEntity, progressUpdateListener: ProgressUpdateListener) {
        filesPresenter.clickOnMedia(mediaFileEntity, progressUpdateListener)
    }

    override fun showDownloadDialog(mediaFileEntity: MediaFileEntity, progressUpdateListener: ProgressUpdateListener) {
        val builder = AlertDialog.Builder(context!!)
        builder.setTitle(R.string.download_media_dialog)
            .setMessage(
                String.format(
                    getString(R.string.download_media_message),
                    mediaFileEntity.fileSize.toDouble() / 1024.0 / 1024.0
                )
            )
            .setNegativeButton(android.R.string.cancel) { dialog, which -> dialog.dismiss() }
            .setPositiveButton(android.R.string.ok) { dialog, which ->
                filesPresenter.loadMediaFile(mediaFileEntity, progressUpdateListener)
                dialog.dismiss()
            }
            .show()
    }

    override fun updateMedia(mediaFiles: List<MediaFileEntity>, position: Int) {
        adapter.data = mediaFiles
        adapter.notifyItemChanged(position)
    }

    override fun playFile(path: String, fileName: String) {
        var file = File(path, fileName)

        var uri = FileProvider.getUriForFile(context!!, context!!.getApplicationContext().getPackageName() + ".ru.breffi.smartlibrary.provider", file)
        var mime = activity?.getContentResolver()?.getType(uri)

        // Open file with user selected app
        var intent = Intent()
        intent.setAction(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, mime)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(intent)

    }

}
