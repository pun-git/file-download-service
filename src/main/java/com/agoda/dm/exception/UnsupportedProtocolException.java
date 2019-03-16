package com.agoda.dm.exception;

/**
 * throw if plugin for download file is unavailable
 */
public class UnsupportedProtocolException extends RuntimeException {

  public UnsupportedProtocolException(String message) {
    super(message);
  }

}
