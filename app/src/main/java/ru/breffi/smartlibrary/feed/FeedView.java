package ru.breffi.smartlibrary.feed;

import ru.breffi.story.domain.models.PresentationEntity;

import java.util.List;

public interface FeedView {
    void showPresentations(List<PresentationEntity> presentations);

    void showProgress();

    void hideProgress();

    void showContentLoadingProgress(int total, int progress, ProgressUpdateListener presentationViewHolder, int position);

    void showPresentation(PresentationEntity presentationEntity);

    void showDownloadDialog(PresentationEntity presentationEntity, ProgressUpdateListener progressUpdateListener);

    void showUpdateDialog(PresentationEntity presentationEntity, ProgressUpdateListener progressUpdateListener);

    void showDeleteDialog(int presentationId);

    void updatePresentation(List<PresentationEntity> presentationEntities, int position);

    void refreshPresentation(List<PresentationEntity> presentationEntities, int position);
}
