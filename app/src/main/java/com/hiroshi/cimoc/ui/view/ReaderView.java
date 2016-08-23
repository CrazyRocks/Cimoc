package com.hiroshi.cimoc.ui.view;

import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.ImageUrl;

import java.util.List;

/**
 * Created by Hiroshi on 2016/8/21.
 */
public interface ReaderView extends BaseView {

    void showMessage(int resId);

    void onParseError();

    void onNetworkError();

    void onNextLoadSuccess(List<ImageUrl> list);

    void onPrevLoadSuccess(List<ImageUrl> list);

    void onFirstLoadSuccess(List<ImageUrl> list, int progress, String title);

    void onChapterChange(Chapter chapter, boolean isNext);

    void onImageLoadSuccess(int id, String url);

}