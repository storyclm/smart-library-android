package ru.breffi.smartlibrary.di;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.Module;
import ru.breffi.story.data.repositories.*;
import ru.breffi.story.domain.repositories.*;

@Module
abstract class ImplementationsModule {

    @Binds
    @Singleton
    abstract PresentationRepository bindPresentationRepository(PresentationRepositorySource remotePresentationRepository);

    @Binds
    @Singleton
    abstract AccountRepository bindAccountRepository(AccountRepositorySource accountRepositorySource);

    @Binds
    @Singleton
    abstract ClientRepository bindClientRepository(ClientRepositorySource clientRepositorySource);

    @Binds
    @Singleton
    abstract ContentRepository bindContentRepository(ContentRepositorySource contentRepositorySource);

    @Binds
    @Singleton
    abstract MediaContentRepository bindMediaContentRepository(MediaContentRepositorySource mediaContentRepositorySource);

}
