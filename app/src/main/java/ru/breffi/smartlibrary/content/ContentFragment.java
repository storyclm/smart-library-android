package ru.breffi.smartlibrary.content;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JsResult;
import android.webkit.MimeTypeMap;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebHistoryItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import ru.breffi.smartlibrary.BuildConfig;
import ru.breffi.smartlibrary.R;
import ru.breffi.smartlibrary.bridge.TestBridgeModule;
import ru.breffi.smartlibrary.media.MediaFilesActivity;
import ru.breffi.smartlibrary.slides.SlidesTreeFragment;
import ru.breffi.story.data.bridge.StoryBridgeFactory;
import ru.breffi.story.data.bridge.StoryBridgeListener;
import ru.breffi.story.data.bridge.modules.SessionModule;
import ru.breffi.story.data.bridge.modules.base.BaseModuleView;
import ru.breffi.story.data.bridge.modules.map.MapModuleBridgeView;
import ru.breffi.story.data.bridge.modules.media.MediaLibraryBridgeView;
import ru.breffi.story.data.bridge.modules.presentation.PresentationModuleData;
import ru.breffi.story.data.bridge.modules.presentation.PresentationModuleView;
import ru.breffi.story.data.bridge.modules.ui.UiModuleBridgeView;
import ru.breffi.story.data.models.Session;
import ru.breffi.story.data.models.ViewerModuleData;
import ru.breffi.story.domain.models.PresentationEntity;


/**
 * A simple {@link Fragment} subclass.
 */
public class ContentFragment extends Fragment implements ContentView,
        OnBackPressedListener,
        WebViewListener,
        MediaLibraryBridgeView,
        UiModuleBridgeView,
        SessionModule.RestoreSessionListener,
        PresentationModuleView,
        StoryBridgeListener,
        BaseModuleView,
        MapModuleBridgeView {

    public static final String TAG = "ContentFragment";
    ContentPresenter mPresenter;
    ObservableWebView mWebView;
    private static final String PATH_EXTRA = "path_extra";
    private static final String PRESENTATION = "PRESENTATION";
    private static final int REQUEST_GPS_PERMISSION_CONSTANT = 15;
    private String gpsCommand;
    private String mUrl;
    private ru.breffi.story.data.bridge.StoryBridge storyBridge;

    public static final int REQUEST_PERMISSIONS_LOCATION = 99;
    private String path;
    private ImageView mediaButton;
    private ImageView closeButton;
    private ImageView mapButton;
    private PresentationEntity presentationEntity;
    public boolean isFinishedSession = false;
    private boolean isSlideRestored;

    public static ContentFragment newInstance(String path) {

        Bundle args = new Bundle();
        args.putString(PATH_EXTRA, path);
        ContentFragment fragment = new ContentFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static ContentFragment newInstance(PresentationEntity presentationEntity) {
        Bundle args = new Bundle();
        args.putSerializable(PRESENTATION, presentationEntity);
        ContentFragment fragment = new ContentFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public ContentFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_content, container, false);
        mWebView = v.findViewById(R.id.contentView);
        mediaButton = v.findViewById(R.id.media_button);
        closeButton = v.findViewById(R.id.close_button);
        mapButton = v.findViewById(R.id.map_button);
        presentationEntity = ((PresentationEntity) getArguments().getSerializable(PRESENTATION));
        mediaButton.setOnClickListener(view ->
                startActivity(MediaFilesActivity.Companion.getIntent(getActivity(), presentationEntity.getId())));
        closeButton.setOnClickListener(view -> showFinishSessionDialog());
        mapButton.setOnClickListener(view -> showSlidesTree());
        if (getArguments() != null) {
            path = getActivity().getFilesDir() + "/storyCLM/" + presentationEntity.getId() + "/index.html";
        }
        initBridge();
        initView();
        return v;
    }

    @Override
    public void onDestroyView() {
        storyBridge.dispose();
        super.onDestroyView();
    }

    private void initBridge() {
        storyBridge = StoryBridgeFactory
                .create(mWebView,
                        presentationEntity,
                        this,
                        this,
                        this,
                        this,
                        this,
                        this,
                        this,
                        getContext(),
                        getString(R.string.app_name),
                        BuildConfig.VERSION_NAME,
                        this
                );
        storyBridge.addModule(new TestBridgeModule(), Collections.singletonList(TestBridgeModule.COMMAND));
        storyBridge.init();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        if (path != null) {
//            path = path.replaceAll("\\?", "%3f");
//            String path = "file://" + this.path;
//            Log.e("path", path);
//            try {
//                String decodePath = URLDecoder.decode(path, "UTF-8");
//                mWebView.loadUrl(decodePath);
//                Log.e("path", "decodePath = " + decodePath);
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//                mWebView.loadUrl(path);
//            }
//        }
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    private void showSlidesTree() {
        getActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .add(R.id.fragment_container, SlidesTreeFragment.Companion.newInstance(presentationEntity.getId()), SlidesTreeFragment.TAG)
                .addToBackStack(SlidesTreeFragment.TAG)
                .commit();
    }

    protected void initView() {
        mWebView.getSettings().setDomStorageEnabled(true);
        File dir = getContext().getCacheDir();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        mPresenter = new ContentPresenter();
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }
        });
        // mWebView.getSettings().setAppCachePath(dir.getPath());
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.getSettings().setAppCacheEnabled(false);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mWebView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        try {
            Method m = WebSettings.class.getMethod("setMixedContentMode", int.class);
            if (m == null) {
                Log.e("WebSettings", "Error getting setMixedContentMode method");
            } else {
                m.invoke(mWebView.getSettings(), 2); // 2 = MIXED_CONTENT_COMPATIBILITY_MODE
                Log.i("WebSettings", "Successfully set MIXED_CONTENT_COMPATIBILITY_MODE");
            }
        } catch (Exception ex) {
            Log.e("WebSettings", "Error calling setMixedContentMode: " + ex.getMessage(), ex);
        }
        mWebView.getSettings().setAllowFileAccessFromFileURLs(true);
        mWebView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        //  mWebView.getSettings().setAppCacheEnabled(true);
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setUserAgentString("Android");
        //  mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.setOnScrollChangedCallback(new ObservableWebView.OnScrollChangedCallback() {
            @Override
            public void onScroll(int l, int t, WebView view) {
                if (!view.canScrollVertically(1)) {
                    Map<String, Object> event = new HashMap<>();
//                    StoryUser storyUser = new StoryUser();
//                    storyUser.setSub("sub");
//                    storyUser.setRole(1);

//                    StoryUser me = new Gson().fromJson(new Gson().toJson(storyUser, storyUser.getClass()), new TypeToken<StoryUser>() {
//                    }.getType());
//                    event.put("ContentID", MetricaUtils.getContentId());
//                    event.put("SenderID", me.getSub());
//                    event.put("RecipientID", me.getSub());
//                    YandexMetrica.reportEvent("ContentViewDeep", event);
                }
            }
        });

//        setWebClient();
    }

    private void requestGPSPermission() {
        requestPermissions(
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_GPS_PERMISSION_CONSTANT);
    }

    @Override
    public boolean onBack() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
//            StoryBridge.goBack();
            return true;
        }
        return false;
    }

    @Override
    public void open(String url) {
        Observable.just("").subscribeOn(AndroidSchedulers.mainThread()).subscribe(s -> mWebView.loadUrl(url));
    }

    @Override
    public void showError(String text) {
        getActivity().runOnUiThread(() -> Toast.makeText(getActivity(), text, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void openMedia(String path) {
        File file = new File(path);

// Just example, you should parse file name for extension
        String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf(".") + 1));
        Uri apkURI = FileProvider.getUriForFile(
                getContext(),
                getContext().getApplicationContext()
                        .getPackageName() + ".provider", file);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(apkURI, mime);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        getContext().startActivity(intent);
    }

    private File getRootDir() {
        Boolean isSDPresent = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        File file;
        if (isSDPresent) {
            file = new File(Environment.getExternalStorageDirectory() + "/" + getContext().getString(R.string.app_name));
        } else {
            file = new File(Environment.getDataDirectory() + "/" + getContext().getString(R.string.app_name));
        }
        file.mkdirs();
        return file;
    }

    @Override
    public Observable<List<String>> getBackForvardList() {
        return Observable.create(new ObservableOnSubscribe<List<String>>() {
            @Override
            public void subscribe(ObservableEmitter<List<String>> emitter) throws Exception {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        List<String> list = new ArrayList<>();
                        if (mWebView == null) {
                            emitter.onNext(list);
                            return;
                        }
                        WebBackForwardList backForwardList = mWebView.copyBackForwardList();
                        for (int i = 0; i < backForwardList.getSize(); i++) {
                            WebHistoryItem item = backForwardList.getItemAtIndex(i);
                            String url = item.getUrl();
                            list.add(url.substring(url.lastIndexOf('/') + 1, url.length()));
                        }
                        emitter.onNext(list);
                    }
                });
            }
        });

    }

    @Override
    public Observable<String> getCurrentSlideName() {
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String name = "";
                        if (mWebView == null) {
                            emitter.onNext(name);
                            return;
                        }
                        WebBackForwardList backForwardList = mWebView.copyBackForwardList();
                        WebHistoryItem item = backForwardList.getCurrentItem();
                        if (item == null) {
                            emitter.onNext(name);
                            return;
                        }
                        String url = item.getUrl();
                        name = url.substring(url.lastIndexOf('/') + 1, url.length());
                        emitter.onNext(name);
                    }
                });
            }
        });

    }

    @Override
    public void showMediaFileButton(boolean isShow) {
        mediaButton.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    @Override
    public void openMediaLibrary() {
        startActivity(MediaFilesActivity.Companion.getIntent(getActivity(), presentationEntity.getId()));
    }

    @Override
    public void openMediaFile(ViewerModuleData viewerModuleData) {
        try {
            String path = getContext().getFilesDir()
                    + "/storyCLM/"
                    + presentationEntity.getId()
                    + "/mediafiles";
            File file = new File(path, viewerModuleData.getName());
            Uri uri = FileProvider.getUriForFile(getContext(), getContext().getApplicationContext().getPackageName() + ".ru.breffi.smartlibrary.provider", file);
            String mime = getActivity().getContentResolver().getType(uri);
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, mime);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), R.string.media_error, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void hideCloseButton() {
        if (closeButton != null) {
            closeButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void hideSystemButtons() {
        if (getActivity().getWindow() != null) {
            View decorView = getActivity().getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    public void showFinishSessionDialog() {
        Log.e("showFinishSessionDialog", presentationEntity.isNeedConfirmation() + " ");
        if (presentationEntity.isNeedConfirmation()) {
            AlertDialog.Builder builder
                    = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.finish_session)
                    .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
//                        storyBridge.completeSession(Session.State.USER_NOS_SAVE);
                        dialog.dismiss();
//                        isFinishedSession = true;
//                        if (getActivity() != null) {
//                            getActivity().onBackPressed();
//                        }
                    })
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        storyBridge.completeSession(Session.State.COMPLETED);
                        dialog.dismiss();
                        isFinishedSession = true;
                        if (getActivity() != null) {
                            getActivity().onBackPressed();
                        }
                    })
                    .show();
        } else {
            storyBridge.completeSession(Session.State.USER_NOS_SAVE);
            isFinishedSession = true;
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        }
    }

    @Override
    public void onSlideRestore(String slideName) {
        isSlideRestored = true;
        String slideUrl = "file://" + getActivity().getFilesDir() + "/storyCLM/" + presentationEntity.getId() + "/" + slideName;
        Log.e("onSlideRestore", slideUrl);
        mWebView.loadUrl(slideUrl);
    }

    @Override
    public void openPresentation(PresentationModuleData presentationModuleData) {
        String slideUrl = "file://" + getActivity().getFilesDir() + "/storyCLM/" + presentationModuleData.getPresId() + "/" + presentationModuleData.getSlideName();
        Log.e("openPresentation", slideUrl);
        mWebView.loadUrl(slideUrl);
    }

    @Override
    public void closePresentation() {
        showFinishSessionDialog();
    }

    @Override
    public void onBridgeInitializeCompleted() {
        if (path != null && !isSlideRestored) {
            path = path.replaceAll("\\?", "%3f");
            String path = "file://" + this.path;
            Log.e("path", path);
            try {
                String decodePath = URLDecoder.decode(path, "UTF-8");
                mWebView.loadUrl(decodePath);
                Log.e("path", "decodePath = " + decodePath);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                mWebView.loadUrl(path);
            }
        }
    }

    @Override
    public void onBridgeInitializeFailed() {

    }

    @Override
    public void openPresentationSlide(@NotNull PresentationModuleData presentationModuleData) {
        String slideUrl = "file://" + getActivity().getFilesDir() + "/storyCLM/" + presentationEntity.getId() + "/" + presentationModuleData.getSlideName();
        Log.e("openPresentationSlide", slideUrl);
        mWebView.loadUrl(slideUrl);
    }

    @Override
    public void showSlidesMapButton() {
        if (mapButton != null) {
            mapButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void hideSlidesMapButton() {
        if (mapButton != null) {
            mapButton.setVisibility(View.GONE);
        }
    }
}
