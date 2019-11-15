package ru.breffi.smartlibrary.di;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import ru.breffi.smartlibrary.feed.ContentService;
import ru.breffi.smartlibrary.feed.FeedFragment;
import ru.breffi.smartlibrary.main.MainActivity;
import ru.breffi.smartlibrary.media.FilesFragment;
import ru.breffi.smartlibrary.media.MediaFilesActivity;
import ru.breffi.smartlibrary.slides.SlidesTreeFragment;

@Module
public interface ContributorsModule {

    @ContributesAndroidInjector
    MainActivity contributeMainActivity();

    @ContributesAndroidInjector
    FeedFragment contributeFeedFragment();

    @ContributesAndroidInjector
    FilesFragment contributeFilesFragment();

    @ContributesAndroidInjector
    SlidesTreeFragment slidesTreeFragmentFragment();

    @ContributesAndroidInjector
    MediaFilesActivity contributeMediaFilesActivity();

    @ContributesAndroidInjector
    ContentService contributeContentService();
}
