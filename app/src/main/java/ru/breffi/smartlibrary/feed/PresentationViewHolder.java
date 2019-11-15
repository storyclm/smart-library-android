package ru.breffi.smartlibrary.feed;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.*;
import antonkozyriatskyi.circularprogressindicator.CircularProgressIndicator;
import com.bumptech.glide.Glide;
import com.github.mmin18.widget.RealtimeBlurView;
import ru.breffi.smartlibrary.R;
import ru.breffi.story.domain.models.PresentationEntity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PresentationViewHolder extends RecyclerView.ViewHolder implements ProgressUpdateListener {

    ImageView presentationImageView;
    ImageView stopLoadImageView;
    TextView presentationName;
    TextView presentationDate;
    TextView presentationText;
    TextView newTextView;
    TextView updateTextView;
    TextView downloadTextView;
    ImageView infoImageView;
    View rootView;
    CircularProgressIndicator progressIndicator;
    FrameLayout progressContainer;
    RelativeLayout container;
    RealtimeBlurView blurView;
    View itemView;
    public PresentationEntity presentation;
    ProgressBar deletingProgressBar;
    public static final String YYYY_MM_DD = "yyyy-MM-dd";

    public PresentationViewHolder(View itemView) {
        super(itemView);
        this.itemView = itemView;
        stopLoadImageView = itemView.findViewById(R.id.stop_load_image_view);
        presentationImageView = itemView.findViewById(R.id.presentation_image);
        presentationName = itemView.findViewById(R.id.presentation_name);
        presentationDate = itemView.findViewById(R.id.presentation_date);
        presentationText = itemView.findViewById(R.id.presentation_text);
        newTextView = itemView.findViewById(R.id.new_text);
        updateTextView = itemView.findViewById(R.id.update_text);
        downloadTextView = itemView.findViewById(R.id.download_text);
        infoImageView = itemView.findViewById(R.id.info_image);
        rootView = itemView.findViewById(R.id.rootView);
        progressIndicator = itemView.findViewById(R.id.progress);
        progressContainer = itemView.findViewById(R.id.progress_container);
        container = itemView.findViewById(R.id.container);
        blurView = itemView.findViewById(R.id.blur_view);
        deletingProgressBar = itemView.findViewById(R.id.deleting_progress_bar);
        progressIndicator.setProgressTextAdapter(v -> /*(int) v + "%"*/ "");
    }

    public void setPresentation(final PresentationEntity presentation,
                                final PresentationClickListener presentationClickListener,
                                final Context context) {
        this.presentation = presentation;
        presentationName.setText(presentation.getName());
        initShortDescription(presentation);
        presentationDate.setText(String.format("%s", toDate(presentation.getCreated())));
        Glide.with(context)
                .load(presentation.getImgId())
                .into(presentationImageView);
        initButtons(presentation);
        rootView.setOnClickListener(v -> {
            Log.e("pres click", presentation.toString());
            if (presentation.getCurrentProgress() == 0 /*|| presentation.getCurrentProgress() == 100*/ || presentation.isNowUpdating()) {
                presentationClickListener.onPresentationClicked(presentation, this);
            }
        });
        infoImageView.setOnClickListener(v -> presentationClickListener.onPresentationMenuClick(presentation, this));
        updateTextView.setOnClickListener(v -> presentationClickListener.onPresentationUpdateClick(presentation, this));
        stopLoadImageView.setOnClickListener(v -> presentationClickListener.onPresentationStopClick(presentation, this));
        setProgressIndicator(presentation);
        setDeletingProgressIndicator(presentation);
    }

    private void setDeletingProgressIndicator(PresentationEntity presentation) {
        if (presentation.isNowDeleting()) {
            deletingProgressBar.setVisibility(View.VISIBLE);
        } else {
            deletingProgressBar.setVisibility(View.GONE);
        }
    }

    private void setProgressIndicator(PresentationEntity presentation) {
        if (presentation.getCurrentProgress() != 0 && presentation.getCurrentProgress() != 100) {
            progressContainer.setVisibility(View.VISIBLE);
            progressIndicator.setMaxProgress(presentation.getTotalProgress());
            progressIndicator.setCurrentProgress(presentation.getCurrentProgress());
        } else {
            progressContainer.setVisibility(View.GONE);
            progressIndicator.setCurrentProgress(0);
        }
    }

    private void initShortDescription(PresentationEntity presentation) {
        if (!TextUtils.isEmpty(presentation.getShortDescription())) {
            presentationText.setVisibility(View.VISIBLE);
            presentationText.setText(presentation.getShortDescription());
        } else {
            presentationText.setVisibility(View.GONE);
        }
    }

    private void initButtons(PresentationEntity presentation) {
        if (!presentation.getWithContent()) {
            downloadTextView.setVisibility(View.VISIBLE);
//            infoImageView.setVisibility(View.INVISIBLE);
        } else {
            downloadTextView.setVisibility(View.INVISIBLE);
//            infoImageView.setVisibility(View.VISIBLE);
        }

        if (!presentation.getNeedUpdate()) {
            updateTextView.setVisibility(View.INVISIBLE);
        } else {
            updateTextView.setVisibility(View.VISIBLE);
        }

        if (presentation.isRead()) {
            newTextView.setVisibility(View.INVISIBLE);
//            infoImageView.setVisibility(View.VISIBLE);
        } else {
            newTextView.setVisibility(View.VISIBLE);
//            infoImageView.setVisibility(View.INVISIBLE);
        }

        if (presentation.isRead() && presentation.getWithContent()) {
            infoImageView.setVisibility(View.VISIBLE);
        } else {
            infoImageView.setVisibility(View.GONE);
        }
    }

    private String toDate(Date date) {
        String result = "";
        if (date != null) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
                result = sdf.format(date);
                Calendar current = Calendar.getInstance();
                Calendar temp = Calendar.getInstance();
                temp.setTime(date);
                if (current.get(Calendar.YEAR) == temp.get(Calendar.YEAR)) {
                    result = result.substring(0, result.length() - 5);
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                result = "";
            }
        }
        return result;
    }

    @Override
    public void progressUpdate(int total, int progress) {
        Log.d("restore test", "progressUpdate() called with: total = [" + total + "], progress = [" + progress + "]");
        if (progress != 100) {
            progressContainer.setVisibility(View.VISIBLE);
            progressIndicator.setMaxProgress(total);
            progressIndicator.setCurrentProgress(progress);
            presentation.setTotalProgress(total);
            presentation.setCurrentProgress(progress);
        } else {
            progressContainer.setVisibility(View.GONE);
            progressIndicator.setMaxProgress(0);
            progressIndicator.setCurrentProgress(0);
            presentation.setTotalProgress(0);
            presentation.setCurrentProgress(0);
        }
    }
}
