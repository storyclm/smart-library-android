package ru.breffi.smartlibrary.feed;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import dagger.android.support.AndroidSupportInjection;
import ru.breffi.smartlibrary.R;
import ru.breffi.smartlibrary.content.ContentActivity;
import ru.breffi.smartlibrary.feed.menu.PresentationMenuFragment;
import ru.breffi.story.domain.models.PresentationEntity;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class FeedFragment extends Fragment implements FeedView,
        PresentationClickListener,
        PresentationMenuFragment.PresentationMenuItemClickListener {
    private static final String SAVED_LAYOUT_MANAGER = "SAVED_LAYOUT_MANAGER";
    private static final String PRESENTATIONS_LOADING = "PRESENTATIONS_LOADING";
    public static final String TAG = "FeedFragment";
    private RecyclerView feedRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;

    private RecyclerView.LayoutManager layoutManager;
    private Parcelable layoutManagerState;
    private PresentationAdapter adapter;

    @Inject
    FeedPresenter feedPresenter;
    private Bundle savedInstanceState;
    private boolean isPresentationsLoading;

    public static FeedFragment newInstance() {
        Bundle args = new Bundle();
        FeedFragment fragment = new FeedFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.e("onCreate", "onCreate");
        super.onCreate(savedInstanceState);
        AndroidSupportInjection.inject(this);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SAVED_LAYOUT_MANAGER, layoutManager.onSaveInstanceState());
        outState.putBoolean(PRESENTATIONS_LOADING, isPresentationsLoading);
        Log.e("onSaveInstanceState", "onSaveInstanceState");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e("onCreateView", "onCreateView");
        View view = inflater.inflate(R.layout.fragment_feed, container, false);
        initViews(view);
        feedPresenter.initView(this);
        initRecyclerView();
        this.savedInstanceState = savedInstanceState;
        if (getArguments() != null) {
//            if (savedInstanceState != null) {
//                layoutManagerState = savedInstanceState.getParcelable(SAVED_LAYOUT_MANAGER);
////                feedPresenter.getPresentations(false);
//            } else {
////                feedPresenter.getPresentations(true);
//            }


            if (savedInstanceState != null) {
                layoutManagerState = savedInstanceState.getParcelable(SAVED_LAYOUT_MANAGER);
                if (savedInstanceState.getBoolean(PRESENTATIONS_LOADING)) {
                    feedPresenter.getPresentations(true);
                    isPresentationsLoading = true;
                } else {
                    feedPresenter.getPresentations(false);
                }
            } else {
                feedPresenter.getPresentations(true);
                isPresentationsLoading = true;
            }
        }
        return view;
    }

    private void initRecyclerView() {
        adapter = new PresentationAdapter(new ArrayList<>(), this, getContext());
        initLayoutManager();
        feedRecyclerView.setLayoutManager(layoutManager);
        feedRecyclerView.setAdapter(adapter);
        ((SimpleItemAnimator) feedRecyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
    }

    private void initLayoutManager() {
        boolean isTablet = getResources().getBoolean(R.bool.isTablet);
        if (isTablet) {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                layoutManager = new GridLayoutManager(getContext(), 3);
            } else {
                layoutManager = new GridLayoutManager(getContext(), 2);
            }
        } else {
            layoutManager = new LinearLayoutManager(getContext());
        }
    }

    private void initViews(View view) {
        feedRecyclerView = view.findViewById(R.id.feed_recycler_view);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(() -> feedPresenter.getPresentations(true));
    }

    @Override
    public void onPresentationClicked(PresentationEntity presentationEntity, ProgressUpdateListener progressUpdateListener) {
        feedPresenter.retrievePresentation(presentationEntity, progressUpdateListener);
    }

    @Override
    public void onPresentationMenuClick(PresentationEntity presentationEntity, ProgressUpdateListener progressUpdateListener) {
        feedPresenter.setChangablePresentation(presentationEntity);
        feedPresenter.setChangableProgressUpdateListener(progressUpdateListener);
        if (getFragmentManager() != null) {
            PresentationMenuFragment.newInstance(presentationEntity.getId(), this)
                    .show(getFragmentManager(), PresentationMenuFragment.TAG);
        }
    }

    @Override
    public void onPresentationUpdateClick(PresentationEntity presentation, ProgressUpdateListener progressUpdateListener) {
        feedPresenter.setChangablePresentation(presentation);
        feedPresenter.setChangableProgressUpdateListener(progressUpdateListener);
        feedPresenter.tryToUpdatePresentationContent();
    }

    @Override
    public void onPresentationStopClick(PresentationEntity presentationEntity, ProgressUpdateListener progressUpdateListener) {
        feedPresenter.stopPresentationLoading(presentationEntity, progressUpdateListener);
    }

    @Override
    public void showPresentations(List<PresentationEntity> presentations) {
        adapter.setData(presentations);
        adapter.notifyDataSetChanged();
        Log.e("showPresentations", presentations.toString());
        if (layoutManagerState != null) {
            layoutManager.onRestoreInstanceState(layoutManagerState);
            layoutManagerState = null;
        }
        isPresentationsLoading = false;
        feedPresenter.restoreDownloadingProcess(feedRecyclerView, adapter, layoutManager);
    }

    @Override
    public void showProgress() {
        swipeRefreshLayout.setRefreshing(true);
    }

    @Override
    public void hideProgress() {
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void showContentLoadingProgress(int total, int progress, ProgressUpdateListener presentationViewHolder, int position) {
        if (presentationViewHolder != null) {
            presentationViewHolder.progressUpdate(total, progress);
        } else {
            adapter.notifyItemChanged(position);
        }
    }

    @Override
    public void showPresentation(PresentationEntity presentationEntity) {
        if (getActivity() != null) {
            startActivityForResult(ContentActivity.getIntent(getActivity(), presentationEntity), ContentActivity.REQUEST_CODE);
        }
    }

    @Override
    public void showDownloadDialog(PresentationEntity presentationEntity, ProgressUpdateListener progressUpdateListener) {
        AlertDialog.Builder builder
                = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.download_dialog)
                .setMessage(String.format(getString(R.string.download_message), (double) presentationEntity.getSourceFolderEntity().getFileSize() / 1024 / 1024))
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                    feedPresenter.isPresentationClicked = false;
                    dialog.dismiss();
                })
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    feedPresenter.loadPresentation(presentationEntity, progressUpdateListener);
                    feedPresenter.isPresentationClicked = false;
                    dialog.dismiss();
                })
                .show();
    }

    @Override
    public void showUpdateDialog(PresentationEntity presentationEntity, ProgressUpdateListener progressUpdateListener) {
        AlertDialog.Builder builder
                = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.update_dialog)
                .setMessage(String.format(getString(R.string.update_message), (double) presentationEntity.getSourceFolderEntity().getFileSize() / 1024 / 1024))
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    feedPresenter.updatePresentationContent(presentationEntity, progressUpdateListener);
                    dialog.dismiss();
                })
                .show();
    }

    @Override
    public void showDeleteDialog(int presentationId) {
        AlertDialog.Builder builder
                = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.delete_dialog)
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> dialog.dismiss())
                .setPositiveButton(R.string.remove, (dialog, which) -> {
                    feedPresenter.deletePresentationContent(presentationId);
                    dialog.dismiss();
                })
                .show();
    }

    @Override
    public void updatePresentation(List<PresentationEntity> presentationEntities, int position) {
        adapter.setData(presentationEntities);
        adapter.notifyItemChanged(position);
    }

    @Override
    public void refreshPresentation(List<PresentationEntity> presentationEntities, int position) {
//        adapter.setData(presentationEntities);
        adapter.notifyItemChanged(position);
    }

    @Override
    public void onMenuItemClick(PresentationMenuFragment.MessageMenuItem item, int presentationId) {
        switch (item) {
            case SHARE:
                break;
            case DELETE:
                showDeleteDialog(presentationId);
                break;
            case UPDATE:
                feedPresenter.tryToUpdatePresentationContent();
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(getActivity(), ContentService.class);
        if (getActivity() != null) {
            try {
                getActivity().startService(intent);
                getActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        feedPresenter.destroy();
        if (feedPresenter.isBound() && getActivity() != null) {
            getActivity().unbindService(serviceConnection);
            feedPresenter.unbindContentService(serviceConnection);
            feedPresenter.setBound(false);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case ContentActivity.REQUEST_CODE:
                feedPresenter.updatePresentationAfterViewing(requestCode, resultCode, data);
                break;
        }
    }

    /**
     * Defines callbacks for service binding, passed to bindService()
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            ContentService.LocalBinder binder = (ContentService.LocalBinder) service;
            feedPresenter.setBound(true);
            feedPresenter.bindContentService(binder.getService());
//            if (savedInstanceState != null) {
//                if (savedInstanceState.getBoolean(PRESENTATIONS_LOADING)) {
//                    feedPresenter.getPresentations(true);
//                } else {
//                    feedPresenter.getPresentations(false);
//                }
//            } else {
//                feedPresenter.getPresentations(true);
//                isPresentationsLoading = true;
//            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            feedPresenter.setBound(false);
        }
    };
}

