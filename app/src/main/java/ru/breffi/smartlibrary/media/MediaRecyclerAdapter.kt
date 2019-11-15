package ru.breffi.smartlibrary.media

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.recycler_item_media.view.*
import ru.breffi.smartlibrary.R
import ru.breffi.smartlibrary.feed.ProgressUpdateListener
import ru.breffi.story.domain.models.MediaFileEntity

class MediaRecyclerAdapter constructor(var mediaClickListener: MediaClickListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    lateinit var context: Context
    var data: List<MediaFileEntity> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        Log.e("onCreateViewHolder", "onCreateViewHolder")
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.recycler_item_media, parent, false)
        return MediaHolder(view)
    }

    override fun getItemCount(): Int {
        Log.e("getItemCount", "getItemCount".plus(data.size))
        return data.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        Log.e("onBindViewHolder", "onBindViewHolder")
        (holder as MediaHolder).showMediaFile(data[position])
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
        (holder as MediaHolder).showMediaFile(data[position])
    }


    inner class MediaHolder(view: View) : RecyclerView.ViewHolder(view), ProgressUpdateListener {

        lateinit var mediaFileEntity: MediaFileEntity

        override fun progressUpdate(total: Int, progress: Int) {
            if (progress != 100) {
                itemView.progressContainer.visibility = View.VISIBLE
                itemView.progressIndicator.maxProgress = total.toDouble()
                itemView.progressIndicator.setCurrentProgress(progress.toDouble())
                mediaFileEntity.totalProgress = total
                mediaFileEntity.currentProgress = progress
            } else {
                itemView.progressContainer.visibility = View.GONE
                itemView.progressIndicator.maxProgress = 0.0
                itemView.progressIndicator.setCurrentProgress(0.0)
                mediaFileEntity.totalProgress = 0
                mediaFileEntity.currentProgress = 0
            }
        }

        fun showMediaFile(mediaFileEntity: MediaFileEntity) {
            this.mediaFileEntity = mediaFileEntity
            Glide.with(context)
                .load("https://cdn.dribbble.com/users/476251/screenshots/2619255/attachments/523315/placeholder.png")
                .into(itemView.contentPreviewImage)
            itemView.progressIndicator.setProgressTextAdapter { v -> "".plus(v.toInt()).plus("%") }
            itemView.mediaName.text = mediaFileEntity.title
            itemView.setOnClickListener { mediaClickListener.onMediaClick(mediaFileEntity, this) }
            setProgressIndicator(mediaFileEntity)
            setDownloadButton(mediaFileEntity)
        }

        private fun setDownloadButton(mediaFileEntity: MediaFileEntity) {
            if (mediaFileEntity.withContent) {
                itemView.downloadImageView.visibility = View.GONE
                itemView.playImageView.visibility = View.VISIBLE
            } else {
                itemView.downloadImageView.visibility = View.VISIBLE
                itemView.playImageView.visibility = View.GONE
            }
        }

        private fun setProgressIndicator(mediaFileEntity: MediaFileEntity) {
            if (mediaFileEntity.currentProgress != 0 && mediaFileEntity.currentProgress != 100) {
                itemView.progressContainer.visibility = View.VISIBLE
                itemView.progressIndicator.maxProgress = mediaFileEntity.totalProgress.toDouble()
                itemView.progressIndicator.setCurrentProgress(mediaFileEntity.currentProgress.toDouble())
            } else {
                itemView.progressContainer.visibility = View.GONE
            }
        }
    }
}