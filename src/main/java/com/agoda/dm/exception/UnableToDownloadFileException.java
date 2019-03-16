package com.agoda.dm.exception;

/**
 * throw when service is unable to download the file
 */
public class UnableToDownloadFileException extends RuntimeException {

  public UnableToDownloadFileException(Throwable throwable) {
    super(throwable);
  }

  public UnableToDownloadFileException(String errorMessage) {
    super(errorMessage);
  }

}
