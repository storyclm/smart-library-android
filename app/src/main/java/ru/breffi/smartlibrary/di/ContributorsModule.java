package ru.breffi.smartlibrary.di;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import ru.breffi.clm.ui.library.host.HostActivity;
import ru.breffi.smartlibrary.feed.FeedFragment;
import ru.breffi.smartlibrary.loading.LoadingFragment;
import ru.breffi.smartlibrary.main.MainFragment;
import ru.breffi.smartlibrary.media.FilesFragment;
import ru.breffi.smartlibrary.media.MediaFilesFragment;
import ru.breffi.smartlibrary.slides.SlidesTreeFragment;

@Module
public interface ContributorsModule {

    @ContributesAndroidInjector
    MainFragment contributeMainFragment();

    @ContributesAndroidInjector
    FeedFragment contributeFeedFragment();

    @ContributesAndroidInjector
    FilesFragment contributeFilesFragment();

    @ContributesAndroidInjector
    SlidesTreeFragment contributeSlidesTreeFragment();

    @ContributesAndroidInjector
    MediaFilesFragment contributeMediaFilesFragment();

    @ContributesAndroidInjector
    LoadingFragment contributeLoadingFragment();

    @ContributesAndroidInjector
    HostActivity contributeHostActivity();
}
