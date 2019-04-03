package ru.breffi.smartlibrary.main;

import android.content.Context;
import ru.breffi.story.domain.interactors.AccountInteractor;
import ru.breffi.story.domain.interactors.PresentationInteractor;

import javax.inject.Inject;

class MainPresenter {

    private AccountInteractor accountInteractor;
    private MainView view;
    private PresentationInteractor presentationInteractor;

    @Inject
    public MainPresenter(PresentationInteractor presentationInteractor,
                         AccountInteractor accountInteractor) {
        this.presentationInteractor = presentationInteractor;
        this.accountInteractor = accountInteractor;
    }

    public void initDefaultStoryClmUser(Context context) {
//        PreferenceManager.getDefaultSharedPreferences(context)
//                .edit()
//                .putString(CLM_ID, "client_3_2")
//                .putString(CLM_SECRET, "3b924f6ec5b14ffeb66316de34edaa448e03f71dab934b99a4bb50d8c4b6aee7")
//                .putString(CLM_USER, "79312709549")
//                .putString(CLM_PASS, "6479")
//                .apply();
    }

    public void initView(MainView view) {
        this.view = view;
    }
}
