package com.agoda.dm.model;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Details required to configure plugin to download file
 */
public class Configuration {

  private String protocol;
  private String hostName;
  private int port;
  private String path;
  private String targetFilePath;
  private String targetFileName;
  private int connectionTimeOut = (int)TimeUnit.SECONDS.toSeconds(0);
  private int readTimeOut = (int)TimeUnit.SECONDS.toSeconds(0);
  private boolean isResumeSupported = true;
  private int maxNoOfTry = 6;
  private DownloadProgress downloadProgress;
  private int inMemoryBufferSizeInBytes = 256;
  private long timeOutInMin = TimeUnit.MINUTES.toMinutes(0);

  private Configuration(){}

  private Map<String, String> authConfig = new HashMap<>();

  public String getProtocol() {
    return protocol;
  }

  public int getPort() {
    return port;
  }

  public String getHostName() {
    return hostName;
  }

  public String getAuthParamValue(String authParamKey) {
    return authConfig.get(authParamKey);
  }

  public String getPath() {
    return path;
  }

  public String getTargetFilePath() {
    return targetFilePath;
  }

  public int getConnectionTimeOut() {
    return connectionTimeOut;
  }

  public int getReadTimeOut() {
    return readTimeOut;
  }

  public boolean isResumeSupported() {
    return isResumeSupported;
  }

  public int getMaxNoOfTry() {
    return maxNoOfTry;
  }

  public String getTargetFileName() {
    return targetFileName;
  }

  public DownloadProgress getDownloadProgress() {
    return downloadProgress;
  }

  public int getInMemoryBufferSize() {
    return inMemoryBufferSizeInBytes;
  }

  public void setInMemoryBufferSize(int inMemoryBufferSize) {
    this.inMemoryBufferSizeInBytes = inMemoryBufferSize;
  }

  public long getTimeOutInMin() {
    return timeOutInMin;
  }

  public void setTimeOutInMin(long timeOutInMin) {
    this.timeOutInMin = timeOutInMin;
  }

  static public class ConfigurationBuilder {

    private Configuration configuration = new Configuration();


    public ConfigurationBuilder buildProtocol(String protocol) {
      configuration.protocol = protocol;
      return this;
    }

    public ConfigurationBuilder buildHostName(String host) {
      configuration.hostName = host;
      return this;
    }

    public ConfigurationBuilder buildPort(int port) {
      configuration.port = port;
      return this;
    }

    public ConfigurationBuilder buildFilePath(String filePath) {
      configuration.path = filePath;
      return this;
    }

    public ConfigurationBuilder buildTargetFilePath(String targetFilePath) {
      configuration.targetFilePath = targetFilePath;
      return this;
    }

    public ConfigurationBuilder isResumeSupported(boolean isResumeSupported) {
      configuration.isResumeSupported = isResumeSupported;
      return this;
    }

    public ConfigurationBuilder buildAuthKeyValue(String key, String value) {
      configuration.authConfig.put(key, value);
      return this;
    }

    public ConfigurationBuilder buildMaxNoOfTry(int maxNoOfTry) {
      configuration.maxNoOfTry = maxNoOfTry;
      return this;
    }

    public ConfigurationBuilder buildDownloadProgress(DownloadProgress downloadProgress) {
      configuration.downloadProgress = downloadProgress;
      return this;
    }

    public ConfigurationBuilder buildTargetFileName(String targetFileName) {
      configuration.targetFileName = targetFileName;
      return this;
    }

    public ConfigurationBuilder buildInMemoryBuffer(int inMemoryBufferSize){
      configuration.inMemoryBufferSizeInBytes = inMemoryBufferSize;
      return this;
    }

    public ConfigurationBuilder buildReadTimeOut(int readTimeOut){
      configuration.readTimeOut = readTimeOut;
      return this;
    }

    public ConfigurationBuilder buildConnectionTimeOut(int connectionTimeOut){
      configuration.connectionTimeOut = connectionTimeOut;
      return this;
    }

    public Configuration build() {
      return configuration;
    }
  }

}
