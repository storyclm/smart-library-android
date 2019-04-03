package ru.breffi.smartlibrary;

import dagger.android.AndroidInjector;
import dagger.android.support.DaggerApplication;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import ru.breffi.smartlibrary.di.ContextModule;
import ru.breffi.smartlibrary.di.DaggerApplicationComponent;
import ru.breffi.story.data.database.DataRealmModule;

public class Application extends DaggerApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        initRealm();
    }

    private void initRealm() {
        Realm.init(this);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .schemaVersion(1)
                .name("story.realm")
                .modules(new DataRealmModule())
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.compactRealm(realmConfiguration);
        Realm.setDefaultConfiguration(realmConfiguration);
    }

    @Override
    protected AndroidInjector<Application> applicationInjector() {
        return DaggerApplicationComponent.builder()
                .contextModule(new ContextModule(this))
                .create(this);
    }
}
