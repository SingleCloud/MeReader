/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.genlan.mereader.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.genlan.mereader.R;
import com.genlan.mereader.model.Constant;
import com.genlan.mereader.model.FileInfo;
import com.genlan.mereader.util.BitmapUtil;
import com.genlan.mereader.util.ShareUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This fragment has a big {ImageView} that shows PDF pages, and 2
 * {@link Button}s to move between pages. We use a
 * {@link PdfRenderer} to render PDF pages as
 * {@link Bitmap}s.
 */
public class PdfRendererBasicFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = PdfRendererBasicFragment.class.getSimpleName();

    /**
     * The filename of the PDF.
     */
    private static final String FILENAME = "体检报告_代华宇.pdf";

    /**
     * File descriptor of the PDF.
     */
    private ParcelFileDescriptor mFileDescriptor;

    /**
     * {@link PdfRenderer} to render the PDF.
     */
    private PdfRenderer mPdfRenderer;

    /**
     * Page that is currently shown on the screen.
     */
    private PdfRenderer.Page mCurrentPage;

    /**
     * {@link ImageView} that shows a PDF page as a {@link Bitmap}
     */
    private ImageView mImageView;

    /**
     * {@link Button} to move to the previous page.
     */
    private Button mButtonPrevious;

    /**
     * {@link Button} to move to the next page.
     */
    private Button mButtonNext;

    /**
     * PDF page index
     */
    private int mPageIndex;

    /**
     * The file of user selected
     */
    private FileInfo mFileInfo;

    /**
     * The touch info
     */
    private TextView tvTouchInfo;

    private Thread mSaveThread;

    private static Bitmap sBitmap;

    public PdfRendererBasicFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mFileInfo = (FileInfo) getActivity().getIntent().getSerializableExtra(MainActivity.INTENT_FILE_INFO);
        if (mFileInfo == null) {
            Toast.makeText(getContext(), "文件有误，将展示默认PDF", Toast.LENGTH_LONG).show();
        } else {
            if (!TextUtils.equals(mFileInfo.getFileAbsolutePath(), ShareUtil.getInstance().getString(Constant.SAVE_PDF_ABSOLUTE_PATH))) {
                ShareUtil.getInstance().put(Constant.SAVE_INDEX, 0);
            }
            ShareUtil.getInstance().put(Constant.SAVE_PDF_PARENT, mFileInfo.getFileParent());
            ShareUtil.getInstance().put(Constant.SAVE_PDF_ABSOLUTE_PATH, mFileInfo.getFileAbsolutePath());
            ShareUtil.getInstance().put(Constant.SAVE_PDF_NAME, mFileInfo.getFileName());
        }
        return inflater.inflate(R.layout.fragment_pdf_renderer_basic, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Retain view references.
        mImageView = (ImageView) view.findViewById(R.id.image);
        mButtonPrevious = (Button) view.findViewById(R.id.btn_previous);
        mButtonNext = (Button) view.findViewById(R.id.btn_next);
        tvTouchInfo = (TextView) view.findViewById(R.id.tv_touch_info);
        tvTouchInfo.bringToFront();
        // Bind events.
        mButtonPrevious.setOnClickListener(this);
        mButtonNext.setOnClickListener(this);
        view.findViewById(R.id.btn_back).setOnClickListener(this);
        mSaveThread = new Thread(() -> ShareUtil.getInstance().put(Constant.SAVE_INDEX, mPageIndex));
        mImageView.setOnTouchListener((v, event) -> {
            int x = (int) event.getX();
            int y = (int) event.getY();
            tvTouchInfo.setText(getContext().getString(R.string.text_touch_info, x, y));
            return true;
        });

        mPageIndex = 0;
//        tvTouchInfo = new TextView(getActivity());
//        tvTouchInfo.setBackgroundColor(Color.TRANSPARENT);
//        tvTouchInfo.setTextColor(Color.BLACK);

    }

    @Override
    public void onStart() {
        super.onStart();
        mPageIndex = ShareUtil.getInstance().getInt(Constant.SAVE_INDEX);
        Log.d(TAG, "onStart: padge index:" + mPageIndex);
        try {
            openRenderer(getActivity(), mFileInfo);
            showPage(mPageIndex);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Error! " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onStop() {
//        if (null != mCurrentPage){
//            mPageIndex = ShareUtil.getInstance().getInt(SAVE_INDEX);
//        }
        ShareUtil.getInstance().put(Constant.SAVE_INDEX, mCurrentPage.getIndex());
        try {
            closeRenderer();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (sBitmap !=null){
            if (!sBitmap.isRecycled()){
                sBitmap.recycle();
            }
        }
        super.onStop();
    }

    /**
     * Sets up a {@link PdfRenderer} and related resources.
     */
    private void openRenderer(Context context, FileInfo info) throws IOException {
        // In this sample, we read a PDF from all dir.
        File file;
        if (mFileInfo == null) {
            file = new File(context.getCacheDir(), FILENAME);
        } else {
            file = new File(info.getFileParent(), info.getFileName());
        }
        if (!file.exists()) {
            // Since PdfRenderer cannot handle the compressed asset file directly, we copy it into
            // the cache directory.
            InputStream asset = context.getAssets().open(FILENAME);
            FileOutputStream output = new FileOutputStream(file);
            final byte[] buffer = new byte[1024];
            int size;
            while ((size = asset.read(buffer)) != -1) {
                output.write(buffer, 0, size);
            }
            asset.close();
            output.close();
        }
        mFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
        // This is the PdfRenderer we use to render the PDF.
        if (mFileDescriptor != null) {
            mPdfRenderer = new PdfRenderer(mFileDescriptor);
        }
    }

    /**
     * Closes the {@link PdfRenderer} and related resources.
     *
     * @throws IOException When the PDF file cannot be closed.
     */
    private void closeRenderer() throws IOException {
        if (null != mCurrentPage) {
            mCurrentPage.close();
        }
        mPdfRenderer.close();
        mFileDescriptor.close();
    }

    /**
     * Shows the specified page of PDF to the screen.
     *
     * @param index The page index.
     */
    private void showPage(int index) {
        if (mPdfRenderer.getPageCount() <= index) {
            return;
        }
        // Make sure to close the current page before opening another one.
        if (null != mCurrentPage) {
            try {
                mCurrentPage.close();
            } catch (IllegalStateException ignore) {
            }
        }
        // Use `openPage` to open a specific page in PDF.
        mCurrentPage = mPdfRenderer.openPage(index);
        // Important: the destination bitmap must be ARGB (not RGB).
        sBitmap = Bitmap.createBitmap(mCurrentPage.getWidth(), mCurrentPage.getHeight(),
                Bitmap.Config.ALPHA_8);
        sBitmap = BitmapUtil.compressImage(sBitmap);
        // Here, we render the page onto the Bitmap.
        // To render a portion of the page, use the second and third parameter. Pass nulls to get
        // the default result.
        // Pass either RENDER_MODE_FOR_DISPLAY or RENDER_MODE_FOR_PRINT for the last parameter.
        mCurrentPage.render(sBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        // We are ready to show the Bitmap to user.
        mImageView.setImageBitmap(sBitmap);
        updateUi();
    }

    /**
     * Updates the state of 2 control buttons in response to the current page index.
     */
    private void updateUi() {
        int index = mCurrentPage.getIndex();
        int pageCount = mPdfRenderer.getPageCount();
        mButtonPrevious.setEnabled(0 != index);
        mButtonNext.setEnabled(index + 1 < pageCount);
        getActivity().setTitle(getString(R.string.app_name_with_index, index + 1, pageCount));
    }

    private void savePage() {
        mSaveThread.start();
    }

    @Override
    public void onClick(View view) {
        savePage();
        switch (view.getId()) {
            case R.id.btn_previous: {
                // Move to the previous page
                showPage(mCurrentPage.getIndex() - 1);
                break;
            }
            case R.id.btn_next: {
                // Move to the next page
                showPage(mCurrentPage.getIndex() + 1);
                break;
            }
            case R.id.btn_back: {
                ShareUtil.getInstance().put(Constant.SAVE_REREAD, true);
                Intent intent = new Intent(getActivity(), SearchFileActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        }
    }

}
