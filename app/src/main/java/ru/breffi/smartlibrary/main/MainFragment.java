package ru.breffi.smartlibrary.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import ru.breffi.smartlibrary.BuildConfig;
import ru.breffi.smartlibrary.R;
import ru.breffi.smartlibrary.feed.FeedFragment;
import ru.breffi.smartlibrary.views.NonSwipeableViewPager;
import ru.breffi.story.data.bridge.sync.SyncService;

public class MainFragment extends Fragment implements MainView {

    @Inject
    MainPresenter mainPresenter;
    private String ROTATE_TAG = "rotate";
    private static final String CURRENT_ITEM = "current_item";
    private static int mCurrentPage = -1;
    private NonSwipeableViewPager viewPager;
    private TabLayout tabLayout;
    private FragmentPagerAdapter fragmentAdapter;
    private FeedFragment feedFragment;
    private Fragment catalogFragment;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (viewPager != null) {
            outState.putInt(CURRENT_ITEM, viewPager.getCurrentItem());
        }
        outState.putBoolean(ROTATE_TAG, true);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidSupportInjection.inject(this);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        syncClmData();
        mainPresenter.initView(this);
        mainPresenter.initDefaultStoryClmUser(requireContext());
        if (savedInstanceState == null) {
//            FeedFragment feedFragment = FeedFragment.newInstance();
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.fragmentContainer, feedFragment, FeedFragment.TAG)
//                    .commit();

        }
        initViews(view);
        initViewPager();
    }


    private void syncClmData() {
        Intent intent = new Intent(requireContext(), SyncService.class);
        intent.putExtra(SyncService.APP_NAME, getString(R.string.app_name));
        intent.putExtra(SyncService.VERSION_NAME, BuildConfig.VERSION_NAME);
        requireActivity().startService(intent);
    }

    private void initViews(@NonNull View view) {
        tabLayout = view.findViewById(R.id.tabs);
        viewPager = view.findViewById(R.id.pager);
        if (mCurrentPage != -1 && viewPager != null) {
            viewPager.post(() -> {
                if (viewPager != null) {
                    viewPager.setCurrentItem(mCurrentPage, false);
                }
            });
        }
        if (viewPager != null) {
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    mCurrentPage = position;
                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
        }
    }

    private void initViewPager() {
        fragmentAdapter = new FragmentPagerAdapter(getChildFragmentManager());
        feedFragment = FeedFragment.newInstance();
        catalogFragment = new Fragment();
        fragmentAdapter.add(getString(R.string.feed_tab), feedFragment);
        fragmentAdapter.add(getString(R.string.catalog_tab), catalogFragment);
        viewPager.setAdapter(fragmentAdapter);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.setCurrentItem(0);
        disableTabClick();
    }

    private void disableTabClick() {
        LinearLayout tabStrip = ((LinearLayout) tabLayout.getChildAt(0));
        for (int i = 0; i < tabStrip.getChildCount(); i++) {
            if (i == 1) {
                tabStrip.getChildAt(i).setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return true;
                    }
                });
            }
        }
    }
}
