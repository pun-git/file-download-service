package com.agoda.dm.model;

import com.agoda.dm.constant.DownloadStatus;

public class DownloadProgress {

  private DownloadStatus downloadStatus;

  private long currentFileSize;

  private long totalRemoteFileSize;

  private Throwable exceptionsRecorded;

  public void setCurrentFileSize(long currentFileSize) {
    this.currentFileSize = currentFileSize;
  }

  public void setDownloadStatus(DownloadStatus downloadStatus) {
    this.downloadStatus = downloadStatus;
  }

  public void setTotalRemoteFileSize(long totalRemoteFileSize) {
    this.totalRemoteFileSize = totalRemoteFileSize;
  }

  public Throwable getExceptionsRecorded() {
    return exceptionsRecorded;
  }

  public long getTotalRemoteFileSize() {
    return totalRemoteFileSize;
  }

  public void setExceptionsRecorded(Throwable exceptionsRecorded) {
    this.exceptionsRecorded = exceptionsRecorded;
  }

  public DownloadStatus getDownloadStatus() {
    return downloadStatus;
  }

  public long getCurrentFileSize() {
    return currentFileSize;
  }


}
