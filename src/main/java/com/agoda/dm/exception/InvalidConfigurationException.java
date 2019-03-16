package com.agoda.dm.exception;

/**
 * throw when invalid configuration provided
 */
public class InvalidConfigurationException extends RuntimeException {

  public InvalidConfigurationException(String message) {
    super(message);
  }

}
