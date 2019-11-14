package ru.breffi.smartlibrary;

import dagger.android.AndroidInjector;
import dagger.android.support.DaggerApplication;
import ru.breffi.smartlibrary.di.ContextModule;
import ru.breffi.smartlibrary.di.DaggerApplicationComponent;
import ru.breffi.story.StoryContent;

public class Application extends DaggerApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        StoryContent.init(this, "story.realm");
    }

    @Override
    protected AndroidInjector<Application> applicationInjector() {
        return DaggerApplicationComponent.builder()
                .contextModule(new ContextModule(this))
                .create(this);
    }
}
