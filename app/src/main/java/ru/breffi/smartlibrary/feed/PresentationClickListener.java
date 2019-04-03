package ru.breffi.smartlibrary.feed;

import ru.breffi.story.domain.models.PresentationEntity;

public interface PresentationClickListener {

    void onPresentationClicked(PresentationEntity presentationEntity, ProgressUpdateListener progressUpdateListener);
    void onPresentationMenuClick(PresentationEntity presentationEntity, ProgressUpdateListener progressUpdateListener);
    void onPresentationUpdateClick(PresentationEntity presentation, ProgressUpdateListener progressUpdateListener);
    void onPresentationStopClick(PresentationEntity presentationEntity, ProgressUpdateListener progressUpdateListener);
}
