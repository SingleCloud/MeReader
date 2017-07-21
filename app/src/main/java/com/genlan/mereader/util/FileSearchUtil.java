package com.genlan.mereader.util;

import android.os.Environment;
import android.util.Log;


import com.genlan.mereader.model.FileInfo;

import java.io.File;

/**
 * Description
 * Author Genlan
 * Date 2017/7/18
 */

public class FileSearchUtil {

    private final Thread mSearchThread;
    private OnSearchFileListener mListener;

    public FileSearchUtil(OnSearchFileListener listener) {
        this.mListener = listener;
        this.mSearchThread = new Thread(this::searchPDF);
    }

    public void startSearch() {
        mSearchThread.start();
    }

    public void stopSearch() {
        try {
            mSearchThread.interrupt();
        } catch (Exception ignore) {
        }
    }

    private void searchPDF() {
        mListener.onStart();
        showAllFiles(new File(Environment.getExternalStorageDirectory().getPath()), ".pdf");
        mListener.onComplete();
    }

    private void showAllFiles(File dir, String matcher) {
        File[] fs = dir.listFiles();
        if (fs == null) {
            Log.e("FileSearchUtil", "No such file");
            mListener.onComplete();
            return;
        }
        for (File f : fs) {
            if (f.isDirectory()) {
                try {
                    showAllFiles(f, matcher);
                } catch (Exception ignored) {
                }
            } else if (f.getName().contains(matcher)) {
                FileInfo info = new FileInfo();
                info.setFileAbsolutePath(f.getAbsolutePath());
                info.setFileName(f.getName());
                info.setFileParent(f.getParent());
                mListener.onNext(info);
            }
        }
    }

    public interface OnSearchFileListener {

        void onStart();

        void onNext(FileInfo info);

        void onComplete();

    }

}
