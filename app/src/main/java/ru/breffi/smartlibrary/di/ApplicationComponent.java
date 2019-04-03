package ru.breffi.smartlibrary.di;


import dagger.Component;
import dagger.android.AndroidInjectionModule;
import dagger.android.AndroidInjector;
import dagger.android.support.AndroidSupportInjectionModule;
import ru.breffi.smartlibrary.Application;

import javax.inject.Singleton;

@Singleton
@Component(modules = {DataModule.class,
        AndroidSupportInjectionModule.class,
        AndroidInjectionModule.class,
        ContributorsModule.class,
        ImplementationsModule.class,
        ContextModule.class})
public interface ApplicationComponent extends AndroidInjector<Application> {

    @Component.Builder
    abstract class Builder extends AndroidInjector.Builder<Application> {
        public abstract Builder contextModule(ContextModule contextModule);
    }
}
