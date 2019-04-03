package ru.breffi.smartlibrary.di;


import dagger.Module;
import dagger.Provides;
import ru.breffi.story.data.network.AuthRetrofitService;
import ru.breffi.story.data.network.Communicator;
import ru.breffi.story.data.network.PresentationRetrofitService;

import javax.inject.Singleton;

@Module
public class DataModule {

    @Provides
    @Singleton
    public PresentationRetrofitService providePresentationRetrofitService() {
        return Communicator.getPresentationRetrofitService();
    }

    @Provides
    @Singleton
    public AuthRetrofitService provideAuthRetrofitService() {
        return Communicator.getAuthRetrofitService();
    }
}
