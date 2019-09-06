package ru.breffi.smartlibrary.di;


import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import ru.breffi.smartlibrary.BuildConfig;
import ru.breffi.story.data.network.AuthRetrofitService;
import ru.breffi.story.data.network.Communicator;
import ru.breffi.story.data.network.PresentationRetrofitService;

@Module
public class DataModule {

    @Provides
    @Singleton
    public PresentationRetrofitService providePresentationRetrofitService() {
        return Communicator.getPresentationRetrofitService(BuildConfig.CLM_API);
    }

    @Provides
    @Singleton
    public AuthRetrofitService provideAuthRetrofitService() {
        return Communicator.getAuthRetrofitService(BuildConfig.CLM_AUTH);
    }
}
