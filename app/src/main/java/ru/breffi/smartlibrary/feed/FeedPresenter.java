package ru.breffi.smartlibrary.feed;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.annimon.stream.Stream;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ru.breffi.smartlibrary.BuildConfig;
import ru.breffi.smartlibrary.R;
import ru.breffi.story.data.network.NetworkUtil;
import ru.breffi.story.domain.interactors.AccountInteractor;
import ru.breffi.story.domain.interactors.PresentationContentInteractor;
import ru.breffi.story.domain.interactors.PresentationInteractor;
import ru.breffi.story.domain.models.AccountEntity;
import ru.breffi.story.domain.models.DownloadEntity;
import ru.breffi.story.domain.models.PresentationEntity;
import ru.breffi.story.domain.models.ProgressEntity;

public class FeedPresenter {

    private final Context context;
    private FeedView view;
    private AccountInteractor accountInteractor;
    private PresentationInteractor presentationInteractor;
    private PresentationContentInteractor presentationContentInteractor;
    private AccountEntity account;
    private CompositeDisposable compositeDisposable;
    private HashMap<PresentationEntity, ProgressUpdateListener> progressUpdateListeners = new HashMap<>();
    private List<PresentationEntity> presentationEntities = new ArrayList<>();
    private PresentationEntity changablePresentation;
    private ProgressUpdateListener changableProgressUpdateListener;
    private Disposable downloadFinishObservable;
    private Map<PresentationEntity, Disposable> loadingDisposables = new HashMap<>();

    private Runnable pendingContentAction = null;

    @Inject
    public FeedPresenter(PresentationInteractor presentationInteractor,
                         AccountInteractor accountInteractor,
                         Context context,
                         PresentationContentInteractor presentationContentInteractor) {
        this.presentationInteractor = presentationInteractor;
        this.accountInteractor = accountInteractor;
        this.context = context;
        this.presentationContentInteractor = presentationContentInteractor;
        compositeDisposable = new CompositeDisposable();
    }

    public void initView(FeedView view) {
        this.view = view;

        Disposable disposable = presentationContentInteractor.observeContentUpdated()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        presentations -> {
                            if (pendingContentAction != null) {
                                pendingContentAction.run();
                                pendingContentAction = null;
                            }
                        },
                        this::handleError
                );
        compositeDisposable.add(disposable);
    }

    public void getPresentations(boolean loadFromServer) {
//        if (isContentServiceValid()) {
        Log.e("getPresentations", context.getString(R.string.CLIENT_ID) + " " +
                context.getString(R.string.CLIENT_SECRET));
        compositeDisposable.add(accountInteractor.getAccount(context.getString(R.string.CLIENT_ID), context.getString(R.string.CLIENT_SECRET),
                context.getString(R.string.USERNAME), context.getString(R.string.PASSWORD), context.getString(R.string.GRAND_TYPE))
                .doOnNext(this::setAccount)
                .flatMap(account -> presentationInteractor.getPresentations(loadFromServer, null))
                .doOnNext(presentationEntities -> this.presentationEntities = presentationEntities)
                .doOnSubscribe((o) -> view.showProgress())
                .doOnTerminate(() -> view.hideProgress())
                .subscribe(pres -> view.showPresentations(pres), this::handleError));
//        }
    }

    void retrievePresentation(PresentationEntity presentationEntity, ProgressUpdateListener progressUpdateListener) {
        progressUpdateListeners.put(presentationEntity, progressUpdateListener);
        if (!presentationEntity.getWithContent()) {
            if (presentationEntity.getSourceFolderEntity() != null) {
                view.showDownloadDialog(presentationEntity, progressUpdateListener);
            } else {
                view.showPresentationError(context.getString(R.string.pres_folder_error));
            }
        } else {
            openPresentation(presentationEntity);
        }
    }

    private void checkRequiredContent(RequiredContentCheckCallback callback) {
        Disposable disposable = presentationInteractor.getPresentations(false, null)
                .subscribe(presentations -> {
                            List<PresentationEntity> requiredPresentations = Stream.of(presentations)
                                    .filter(value -> !value.isVisible() && (value.getNeedUpdate() || !value.getWithContent()))
                                    .toList();
                            callback.onDownloadRequired(requiredPresentations);
                        },
                        this::handleError
                );
        compositeDisposable.add(disposable);
    }

    void loadPresentation(PresentationEntity presentationEntity, ProgressUpdateListener progressUpdateListener) {
        if (NetworkUtil.getInstance().isConnected(context)) {
            checkRequiredContent(presentations -> {
                Runnable contentAction = () -> {
                    progressUpdateListeners.put(presentationEntity, progressUpdateListener);
                    Disposable disposable = presentationContentInteractor.getPresentationContent(presentationEntity, BuildConfig.WITH_FULL_CONTENT)
                            .subscribe(loaded -> {
                            }, this::handleError);
                    compositeDisposable.add(disposable);
                    loadingDisposables.put(presentationEntity, disposable);
                };
                if (presentations.isEmpty()) {
                    contentAction.run();
                } else {
                    pendingContentAction = contentAction;
                    view.launchLoading(presentations);
                }
            });
        } else {
            Toast.makeText(context, context.getString(R.string.network_error), Toast.LENGTH_SHORT).show();
        }
    }


    private void handleError(Throwable throwable) {
        Log.e("getPresentations", " handleError");
        throwable.printStackTrace();
        Toast.makeText(context, context.getString(R.string.default_error), Toast.LENGTH_SHORT).show();
    }

    void listenContentLoading() {
        compositeDisposable.add(presentationContentInteractor.listenContentLoading()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::showCurrentProgress, Throwable::printStackTrace));
    }

    void listenDownloadFinish() {
        if (downloadFinishObservable == null || downloadFinishObservable.isDisposed()) {
            downloadFinishObservable = presentationContentInteractor.listenDownloadFinish()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(presentationEntity -> {
                        Log.e("listenDownloadFinish", " presentation = " + presentationEntity.getName());
                        view.updatePresentation(presentationEntities, presentationEntities.indexOf(presentationEntity));
                    }, this::handleError);
            compositeDisposable.add(downloadFinishObservable);
        }
    }

    private void openPresentation(PresentationEntity presentationEntity) {
        if (presentationEntity.getWithContent()) {
            presentationEntity.setRead(true);
            presentationEntity.setLastOpenDate(new Date());
            view.showPresentation(presentationEntity);
            presentationInteractor.updatePresentation(presentationEntity);

            view.refreshPresentation(presentationEntities, presentationEntities.indexOf(presentationEntity));
        }
    }

    private void showCurrentProgress(DownloadEntity<PresentationEntity> downloadEntity) {
        long total = 0;
        long progress = 0;
        for (int i = 0; i < downloadEntity.getDownloadsSparseArray().size(); i++) {
            int key = downloadEntity.getDownloadsSparseArray().keyAt(i);
            ProgressEntity progressEntity = downloadEntity.getDownloadsSparseArray().get(key);
            Log.e("progress", " total = [" + progressEntity.getTotal() + "], progress = [" + progressEntity.getProgress() + "], downloadId = [" + i + "]" + " presId = " + downloadEntity.getEntity().getId());
            total = progressEntity.getTotal();
            progress += progressEntity.getProgress();
        }
        ProgressUpdateListener presentationViewHolder = progressUpdateListeners.get(downloadEntity.getEntity());
        PresentationEntity presentationEntity = findPresentationById(downloadEntity.getEntity().getId());
        presentationEntity.setTotalProgress(100);
        int currentProgress = calculateProgress(total, progress);
        presentationEntity.setCurrentProgress(currentProgress);
        view.showContentLoadingProgress(100, currentProgress, presentationViewHolder, presentationEntities.indexOf(downloadEntity.getEntity()));
    }

    private int calculateProgress(long total, long progress) {
        int percent = (int) (((double) progress / (double) total) * 100);
        Log.e("progress", " total = " + total + " progress = " + progress + " percent = " + percent);
        return percent;
    }

    public AccountEntity getAccount() {
        return account;
    }

    public void setAccount(AccountEntity account) {
        this.account = account;
    }

    public void deletePresentationContent(int presentationId) {
        PresentationEntity presForDelete = findPresentationById(presentationId);
        compositeDisposable.add(presentationContentInteractor.removePresentationContent(presForDelete)
                .doOnSubscribe((o) -> {
                    if (presForDelete != null) {
                        presForDelete.setNowDeleting(true);
                        view.refreshPresentation(presentationEntities, presentationEntities.indexOf(presForDelete));
                    }
                })
                .subscribe(deletablePresentation -> {
                    deletablePresentation.setNowDeleting(false);
                    view.updatePresentation(presentationEntities, presentationEntities.indexOf(deletablePresentation));
                    Log.e("deletePresentation", " deletablePresentation = " + deletablePresentation.getName() + " withContent = " + deletablePresentation.getWithContent());
                }, this::handleError));
    }

    public void updatePresentationContent(PresentationEntity presentationEntity, ProgressUpdateListener progressUpdateListener) {
        if (NetworkUtil.getInstance().isConnected(context)) {
            checkRequiredContent(presentations -> {
                Runnable contentAction = () -> {
                    progressUpdateListeners.put(getChangablePresentation(), progressUpdateListener);
                    getChangablePresentation().setNowUpdating(true);
                    Disposable disposable = presentationContentInteractor.updatePresentationContent(getChangablePresentation(), BuildConfig.WITH_FULL_CONTENT)
                            .doOnSubscribe((o) -> {
                                if (getChangablePresentation() != null) {
                                    Log.e("update pres", getChangablePresentation().toString());
                                    view.refreshPresentation(presentationEntities, presentationEntities.indexOf(getChangablePresentation()));
                                }
                            })
//                    .doOnTerminate(() -> {
//                        getChangablePresentation().setNowUpdating(false);
//                        Log.e("update pres", getChangablePresentation().toString());
//                        view.refreshPresentation(presentationEntities, presentationEntities.indexOf(getChangablePresentation()));
//                    })
                            .subscribe(changablePres -> {
                            }, this::handleError);
                    compositeDisposable.add(disposable);
                    loadingDisposables.put(presentationEntity, disposable);
                };
                if (presentations.isEmpty()) {
                    contentAction.run();
                } else {
                    pendingContentAction = contentAction;
                    view.launchLoading(presentations);
                }
            });
        } else {
            Toast.makeText(context, context.getString(R.string.network_error), Toast.LENGTH_SHORT).show();
        }

    }

    private PresentationEntity findPresentationById(int presentationId) {
        for (PresentationEntity presentationEntity : presentationEntities) {
            if (presentationEntity.getId() == presentationId) {
                return presentationEntity;
            }
        }
        return null;
    }

    public PresentationEntity getChangablePresentation() {
        return changablePresentation;
    }

    public void setChangablePresentation(PresentationEntity changablePresentation) {
        this.changablePresentation = changablePresentation;
    }

    public ProgressUpdateListener getChangableProgressUpdateListener() {
        return changableProgressUpdateListener;
    }

    public void setChangableProgressUpdateListener(ProgressUpdateListener changableProgressUpdateListener) {
        this.changableProgressUpdateListener = changableProgressUpdateListener;
    }

    public void tryToUpdatePresentationContent() {
        view.showUpdateDialog(getChangablePresentation(), getChangableProgressUpdateListener());
    }

    public void restoreDownloadingProcess(RecyclerView recyclerView, PresentationAdapter adapter, RecyclerView.LayoutManager layoutManager) {
        List<PresentationEntity> downloadingPresentations = presentationContentInteractor.getDownloadingPresentations();
        List<PresentationEntity> presentationsQueue = presentationContentInteractor.getPresentationsQueue();
        Log.e("restore test", "downloadingPresentations = " + downloadingPresentations.size());
        for (PresentationEntity presentationEntity : downloadingPresentations) {
            int presentationPosition = presentationEntities.indexOf(presentationEntity);
            Log.e("restore test", "presentationPosition = " + presentationPosition);
            presentationEntities.set(presentationPosition, presentationEntity);
            ProgressUpdateListener progressUpdateListener = (ProgressUpdateListener) recyclerView.findViewHolderForItemId(presentationEntity.getId());
//                ProgressUpdateListener progressUpdateListener = null;
//                for(PresentationViewHolder viewHolder : adapter.getViewHolders()){
//                    if(viewHolder.presentation.getId() == presentationEntity.getId()){
//                        progressUpdateListener = viewHolder;
//                        Log.e("restore test", "viewHolder = " + viewHolder.toString());
//                        break;
//                    }
//                }
            progressUpdateListeners.put(presentationEntity, progressUpdateListener);
        }
        for (PresentationEntity presentationEntity : presentationsQueue) {
            int presentationPosition = presentationEntities.indexOf(presentationEntity);
            presentationEntities.set(presentationPosition, presentationEntity);
            ProgressUpdateListener progressUpdateListener = (ProgressUpdateListener) recyclerView.findViewHolderForItemId(presentationEntity.getId());
            progressUpdateListeners.put(presentationEntity, progressUpdateListener);
            view.showContentLoadingProgress(100, 2, progressUpdateListener, presentationPosition);
        }
        listenContentLoading();
        listenDownloadFinish();
    }

    public void destroy() {
//        compositeDisposable.dispose();
        progressUpdateListeners.clear();
        presentationEntities.clear();
    }

    public void stopPresentationLoading(PresentationEntity presentationEntity, ProgressUpdateListener progressUpdateListener) {
        Disposable disposable = loadingDisposables.get(presentationEntity);
        if (disposable != null) {
            disposable.dispose();
//                loadingDisposables.remove(presentationEntity);
        }
        compositeDisposable.add(presentationContentInteractor.stopPresentationContentLoading(presentationEntity)
                .subscribe(changablePres -> {
                }, this::handleError));
    }

    private interface RequiredContentCheckCallback {

        void onDownloadRequired(List<PresentationEntity> presentations);
    }
}
