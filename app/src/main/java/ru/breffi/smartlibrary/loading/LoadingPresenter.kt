package ru.breffi.smartlibrary.loading

import android.content.Context
import android.widget.Toast
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import ru.breffi.clm.ui.loading.LoadingView
import ru.breffi.smartlibrary.BuildConfig
import ru.breffi.smartlibrary.R
import ru.breffi.story.data.network.NetworkUtil
import ru.breffi.story.domain.interactors.PresentationContentInteractor
import ru.breffi.story.domain.interactors.PresentationInteractor
import ru.breffi.story.domain.models.PresentationEntity
import javax.inject.Inject

class LoadingPresenter @Inject constructor(
    var context: Context,
    var presentationInteractor: PresentationInteractor,
    var contentInteractor: PresentationContentInteractor
) {

    lateinit var loadingView: LoadingView
    var compositeDisposable = CompositeDisposable()
    var presentationIds = listOf<Int>()
    var canceled = false

    fun attach(view: LoadingView) {
        loadingView = view
    }

    fun detach() {
        compositeDisposable.dispose()
    }

    fun loadPresentationsContent(presentationIds: List<Int>) {
        this.presentationIds = presentationIds
        if (NetworkUtil.getInstance().isConnected(context)) {
            val disposable: Disposable =
                presentationInteractor.getLocalPresentations(presentationIds)
                    .flatMap {
                        contentInteractor.loadOrUpdatePresentationsContent(
                            it,
                            BuildConfig.WITH_FULL_CONTENT
                        )
                    }
                    .subscribe(
                        {
                            if (!canceled) {
                                loadingView.showProgress(1f)
                            }
                            finishLoading(it)
                        },
                        { it.printStackTrace() }
                    )
            compositeDisposable.add(disposable)

            val progressDisposable = contentInteractor.listenDownloadProgress(presentationIds)
                .startWith(0f)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        if (!canceled) {
                            loadingView.showProgress(it)
                        }
                    },
                    { it.printStackTrace() }
                )
            compositeDisposable.add(progressDisposable)
        } else {
            Toast.makeText(context, context.getString(R.string.network_error), Toast.LENGTH_SHORT)
                .show()
            loadingView.close()
        }
    }

    fun cancelLoading() {
        if (!canceled) {
            canceled = true
            val disposable: Disposable =
                presentationInteractor.getLocalPresentations(presentationIds)
                    .flatMap { contentInteractor.stopPresentationsContentLoading(it) }
                    .subscribe(
                        {},
                        {
                            it.printStackTrace()
                            finishLoading()
                        }
                    )
            compositeDisposable.add(disposable)
        }
    }

    private fun finishLoading(presentationEntities: List<PresentationEntity> = listOf()) {
        contentInteractor.setContentUpdated(presentationEntities)
        loadingView.close()
    }
}