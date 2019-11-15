package ru.breffi.smartlibrary.main;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import ru.breffi.smartlibrary.BuildConfig;
import ru.breffi.smartlibrary.R;
import ru.breffi.smartlibrary.content.ContentActivity;
import ru.breffi.smartlibrary.feed.FeedFragment;
import ru.breffi.smartlibrary.views.NonSwipeableViewPager;
import ru.breffi.story.data.bridge.sync.SyncService;
import ru.breffi.story.domain.models.PresentationEntity;

public class MainActivity extends AppCompatActivity implements MainView {

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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (viewPager != null) {
            outState.putInt(CURRENT_ITEM, viewPager.getCurrentItem());
        }
        outState.putBoolean(ROTATE_TAG, true);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        syncClmData();
//        setScreenOrientation();
        setContentView(R.layout.activity_main);
        mainPresenter.initView(this);
        mainPresenter.initDefaultStoryClmUser(this);
        if (savedInstanceState == null) {
//            FeedFragment feedFragment = FeedFragment.newInstance();
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.fragmentContainer, feedFragment, FeedFragment.TAG)
//                    .commit();

        }
        initViews();
        initViewPager();
    }

    private void syncClmData() {
        Intent intent = new Intent(this, SyncService.class);
        intent.putExtra(SyncService.APP_NAME, getString(R.string.app_name));
        intent.putExtra(SyncService.VERSION_NAME, BuildConfig.VERSION_NAME);
        startService(intent);
    }

    private void setScreenOrientation() {
        if (!getResources().getBoolean(R.bool.isTablet)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);
    }

    private void initViews() {
        tabLayout = findViewById(R.id.tabs);
        viewPager = findViewById(R.id.pager);
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
        fragmentAdapter = new FragmentPagerAdapter(getSupportFragmentManager());
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

    public void showPresentation(PresentationEntity presentationEntity) {
//        getSupportFragmentManager()
//                .beginTransaction()
//                .add(R.id.fragmentContainer, ContentFragment.newInstance(getFilesDir() + "/storyCLM/" + presentationId + "/index.html"), ContentFragment.TAG)
//                .addToBackStack(ContentFragment.TAG)
//                .commit();
        startActivity(ContentActivity.getIntent(this, presentationEntity));
    }
}
