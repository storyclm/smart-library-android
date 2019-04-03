package ru.breffi.smartlibrary.content;



import io.reactivex.Observable;

import java.util.List;

public interface WebViewListener {
    void open(String url);
    void showError(String text);
    void openMedia(String path);
    Observable<List<String>> getBackForvardList();
    Observable<String> getCurrentSlideName();
}