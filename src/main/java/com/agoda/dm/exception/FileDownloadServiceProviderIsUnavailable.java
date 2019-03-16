package com.agoda.dm.exception;

/**
 * throw when service provider is not available
 */
public class FileDownloadServiceProviderIsUnavailable extends Exception {

  public FileDownloadServiceProviderIsUnavailable(String message) {
    super(message);
  }

}
