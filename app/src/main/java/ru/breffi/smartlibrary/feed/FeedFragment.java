package ru.breffi.smartlibrary.feed;

import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.annimon.stream.Stream;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import ru.breffi.smartlibrary.R;
import ru.breffi.smartlibrary.feed.menu.PresentationMenuFragment;
import ru.breffi.smartlibrary.host.Navigation;
import ru.breffi.story.domain.models.PresentationEntity;

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
        setAdapterData(presentations);
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
        if (getActivity() instanceof Navigation) {
            ((Navigation) getActivity()).showContent(presentationEntity);
        }
    }

    @Override
    public void showDownloadDialog(PresentationEntity presentationEntity, ProgressUpdateListener progressUpdateListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.download_dialog)
                .setMessage(String.format(getString(R.string.download_message), (double) presentationEntity.getSourceFolderEntity().getFileSize() / 1024 / 1024))
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    feedPresenter.loadPresentation(presentationEntity, progressUpdateListener);
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
    public void launchLoading(List<PresentationEntity> presentationsToLoad) {
        if (getActivity() instanceof Navigation) {
            List<Integer> presentationIds = Stream.of(presentationsToLoad)
                    .map(PresentationEntity::getId)
                    .toList();
            ((Navigation) getActivity()).showLoading(new ArrayList<>(presentationIds));
        }
    }

    @Override
    public void updatePresentation(List<PresentationEntity> presentationEntities, int position) {
        setAdapterData(presentationEntities);
        adapter.notifyItemChanged(position);
    }

    @Override
    public void refreshPresentation(List<PresentationEntity> presentationEntities, int position) {
//        setAdapterData(presentationEntities);
        adapter.notifyItemChanged(position);
    }

    @Override
    public void showPresentationError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
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
    public void onDestroy() {
        super.onDestroy();
        feedPresenter.destroy();
    }

    private void setAdapterData(List<PresentationEntity> presentationEntities) {
        List<PresentationEntity> data = Stream.of(presentationEntities)
                .filter(PresentationEntity::isVisible)
                .toList();
        adapter.setData(data);
    }
}

