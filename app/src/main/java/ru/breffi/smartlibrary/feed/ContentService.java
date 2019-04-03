package ru.breffi.smartlibrary.feed;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import dagger.android.AndroidInjection;
import ru.breffi.story.domain.interactors.PresentationContentInteractor;
import ru.breffi.story.domain.interactors.PresentationInteractor;
import ru.breffi.story.domain.models.PresentationEntity;

import javax.inject.Inject;
import java.util.List;

public class ContentService extends Service {

    public static final String TAG = "ContentService";
    LocalBinder localBinder = new LocalBinder();

    @Inject
    PresentationContentInteractor presentationContentInteractor;
    @Inject
    PresentationInteractor presentationInteractor;

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate() called");
        AndroidInjection.inject(this);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand() called with: intent = [" + intent + "], flags = [" + flags + "], startId = [" + startId + "]");
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind() called with: intent = [" + intent + "]");
        return localBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG, "onUnbind() called with: intent = [" + intent + "]");
        return true;
    }

    public class LocalBinder extends Binder {
        ContentService getService() {
            return ContentService.this;
        }
    }

    public List<PresentationEntity> getDownloadingPresentations() {
        return presentationContentInteractor.getDownloadingPresentations();
    }
}
