package ru.breffi.smartlibrary.content;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import ru.breffi.smartlibrary.host.BackConsumer;
import ru.breffi.smartlibrary.BuildConfig;
import ru.breffi.smartlibrary.PresentationCache;
import ru.breffi.smartlibrary.R;
import ru.breffi.smartlibrary.host.Navigation;
import ru.breffi.story.data.bridge.StoryBridge;
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

import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;
import static android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
import static android.view.View.SYSTEM_UI_FLAG_IMMERSIVE;


public class ContentFragment extends Fragment implements ContentView,
        OnBackPressedListener,
        WebViewListener,
        MediaLibraryBridgeView,
        UiModuleBridgeView,
        SessionModule.RestoreSessionListener,
        PresentationModuleView,
        StoryBridgeListener,
        BaseModuleView,
        MapModuleBridgeView,
        BackConsumer {

    public static final String TAG = "ContentFragment";
    ContentPresenter mPresenter;
    ObservableWebView mWebView;
    private static final String PRESENTATION_ID = "PRESENTATION_ID";
    private static final int REQUEST_GPS_PERMISSION_CONSTANT = 15;
    private String gpsCommand;
    private String mUrl;
    private StoryBridge storyBridge;

    public static final int REQUEST_PERMISSIONS_LOCATION = 99;
    private String path;
    private ImageView mediaButton;
    private ImageView closeButton;
    private ImageView mapButton;
    private PresentationEntity presentationEntity;
    public boolean isFinishedSession = false;
    private boolean isSlideRestored;

    public static ContentFragment newInstance(int presentationId) {
        Bundle args = new Bundle();
        args.putInt(PRESENTATION_ID, presentationId);
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
        return inflater.inflate(R.layout.fragment_content, container, false);
    }

    @Override
    public void onDestroyView() {
        storyBridge.dispose();
        super.onDestroyView();
    }

    private void initBridge() {
        storyBridge = StoryBridgeFactory.create(mWebView,
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
                this);
        storyBridge.init();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mWebView = view.findViewById(R.id.contentView);
        mediaButton = view.findViewById(R.id.media_button);
        closeButton = view.findViewById(R.id.close_button);
        mapButton = view.findViewById(R.id.map_button);
        int id = getArguments().getInt(PRESENTATION_ID, -1);
        mediaButton.setOnClickListener(v -> openMediaLibrary());
        closeButton.setOnClickListener(v -> showFinishSessionDialog());
        mapButton.setOnClickListener(v -> showSlidesTree());
        if (savedInstanceState == null) {
            presentationEntity = PresentationCache.pop(id);
            path = getActivity().getFilesDir() + "/storyCLM/" + presentationEntity.getId() + "/index.html";
        }
        initBridge();
        initView();
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    private void showSlidesTree() {
        if (getActivity() instanceof Navigation) {
            ((Navigation) getActivity()).showSlides(presentationEntity.getId());
        }
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
        if (getActivity() instanceof Navigation) {
            ((Navigation) getActivity()).showMedia(presentationEntity.getId());
        }
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
            int uiOptions = SYSTEM_UI_FLAG_IMMERSIVE | SYSTEM_UI_FLAG_FULLSCREEN | SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    public void showFinishSessionDialog() {
        Log.e("showFinishSessionDialog", presentationEntity.isNeedConfirmation() + " ");
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity())
                .setTitle(R.string.close_dialog_title)
                .setMessage(R.string.close_dialog_message)
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    saveSession(Session.State.COMPLETED);
                    close();
                })
                .setNeutralButton(R.string.cancel, null);
        if (presentationEntity.isNeedConfirmation()) {
            builder.setNegativeButton(R.string.no, (dialog, which) -> {
                saveSession(Session.State.USER_NOS_SAVE);
                close();
            });
        }
        builder.show();
    }

    private void close() {
        if (getActivity() instanceof Navigation) {
            ((Navigation) getActivity()).back(false);
        }
    }

    private void saveSession(Session.State state) {
        storyBridge.completeSession(state);
        isFinishedSession = true;
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

    @Override
    public boolean onBackPressed() {
        if (!isFinishedSession) {
            showFinishSessionDialog();
            return true;
        }
        return false;
    }
}
