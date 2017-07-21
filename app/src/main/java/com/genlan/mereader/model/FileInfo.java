package com.genlan.mereader.model;

import java.io.Serializable;

/**
 * Description The file info bean
 * Author Genlan
 * Date 2017/7/18
 */

public class FileInfo implements Serializable{

    private String fileName;

    private String fileAbsolutePath;

    private String fileParent;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileAbsolutePath() {
        return fileAbsolutePath;
    }

    public void setFileAbsolutePath(String fileAbsolutePath) {
        this.fileAbsolutePath = fileAbsolutePath;
    }

    public String getFileParent() {
        return fileParent;
    }

    public void setFileParent(String fileParent) {
        this.fileParent = fileParent;
    }
}
