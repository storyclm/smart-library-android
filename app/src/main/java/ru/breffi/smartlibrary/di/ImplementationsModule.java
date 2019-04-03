package ru.breffi.smartlibrary.di;

import dagger.Binds;
import dagger.Module;
import ru.breffi.story.data.repositories.*;
import ru.breffi.story.domain.repositories.*;

@Module
abstract class ImplementationsModule {

    @Binds
    abstract PresentationRepository bindPresentationRepository(PresentationRepositorySource remotePresentationRepository);

    @Binds
    abstract AccountRepository bindAccountRepository(AccountRepositorySource accountRepositorySource);

    @Binds
    abstract ClientRepository bindClientRepository(ClientRepositorySource clientRepositorySource);

    @Binds
    abstract ContentRepository bindContentRepository(ContentRepositorySource contentRepositorySource);

    @Binds
    abstract MediaContentRepository bindMediaContentRepository(MediaContentRepositorySource mediaContentRepositorySource);

}
