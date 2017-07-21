package com.genlan.mereader.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.genlan.mereader.R;
import com.genlan.mereader.controller.FilesAdapter;
import com.genlan.mereader.model.Constant;
import com.genlan.mereader.model.FileInfo;
import com.genlan.mereader.util.FileSearchUtil;
import com.genlan.mereader.util.ShareUtil;

import java.util.ArrayList;

/**
 * Description
 * Author Genlan
 * Date 2017/7/18
 */

public class SearchFileActivity extends AppCompatActivity {

    /**
     * for Android 6.0 +
     */
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * The list for show files info
     */
    private RecyclerView rvFiles;

    /**
     * for rvFiles
     */
    private RecyclerView.Adapter mAdapter;

    /**
     * The button for search files
     */
    private Button btnSearch;

    /**
     * files info
     */
    private ArrayList<FileInfo> mFilesInfo;

    /**
     * The file info that on clicked
     */
    private FileInfo mFile;

    private FileSearchUtil mSearchUtil;

    private boolean mSearchState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activty_search_file);
        if (!TextUtils.isEmpty(ShareUtil.getInstance().getString(Constant.SAVE_PDF_ABSOLUTE_PATH))){
            findViewById(R.id.ll_search).setVisibility(View.VISIBLE);
            TextView tvName,tvParent;
            FileInfo info = new FileInfo();
            info.setFileParent(ShareUtil.getInstance().getString(Constant.SAVE_PDF_PARENT));
            info.setFileName(ShareUtil.getInstance().getString(Constant.SAVE_PDF_NAME));
            info.setFileAbsolutePath(ShareUtil.getInstance().getString(Constant.SAVE_PDF_ABSOLUTE_PATH));
            tvName = (TextView) findViewById(R.id.tv_search_name);
            tvParent = (TextView) findViewById(R.id.tv_search_parent);
            tvName.setText(getString(R.string.text_file_info,"文件名称",info.getFileName()));
            tvParent.setText(getString(R.string.text_file_info,"文件路径",info.getFileParent()));
            findViewById(R.id.btn_read).setOnClickListener((v -> {
                Intent intent = new Intent();
                intent.setClass(SearchFileActivity.this, MainActivity.class);
                intent.putExtra(MainActivity.INTENT_FILE_INFO, info);
                startActivity(intent);
                this.finish();
            }));
        }
        mFilesInfo = new ArrayList<>();
        btnSearch = (Button) findViewById(R.id.btn_search);
        rvFiles = (RecyclerView) findViewById(R.id.rv_files);
        rvFiles.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mAdapter = new FilesAdapter(mFilesInfo, this, (p, v) -> {
            mFile = mFilesInfo.get(p);
            Intent intent = new Intent();
            intent.setClass(SearchFileActivity.this, MainActivity.class);
            intent.putExtra(MainActivity.INTENT_FILE_INFO, mFile);
            startActivity(intent);
            this.finish();
        });
        rvFiles.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        rvFiles.setItemAnimator(new DefaultItemAnimator());
        rvFiles.setAdapter(mAdapter);
        mSearchState = false;
        mSearchUtil = new FileSearchUtil(new FileSearchUtil.OnSearchFileListener() {
            @Override
            public void onStart() {
                Log.d("Thread", "onStart: ");
                mSearchState = true;
                runOnUiThread(() -> {
                    mFilesInfo.clear();
                    mAdapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onNext(FileInfo info) {
                Log.d("Thread", "onNext: ");
                runOnUiThread(() -> {
                    mFilesInfo.add(info);
                    mAdapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onComplete() {
                Log.d("Thread", "onComplete: ");
                mSearchState = false;
            }
        });

        btnSearch.setOnClickListener((v) -> {
            if (mSearchState) {
                Toast.makeText(SearchFileActivity.this, "已经在搜索了，请稍候", Toast.LENGTH_SHORT).show();
                return;
            }
            mFilesInfo.clear();
            mSearchUtil.startSearch();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mSearchState)
            mSearchUtil.stopSearch();
    }
}
