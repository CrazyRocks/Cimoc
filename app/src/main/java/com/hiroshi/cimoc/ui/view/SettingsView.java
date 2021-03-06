package com.hiroshi.cimoc.ui.view;

/**
 * Created by Hiroshi on 2016/8/21.
 */
public interface SettingsView extends BaseView {

    void onRestoreSuccess(int count);

    void onBackupSuccess(int count);

    void onBackupFail();

    void onFilesLoadSuccess(String[] files);

    void onFilesLoadFail();

    void onCacheClearSuccess();

}
