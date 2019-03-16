package com.agoda.dm.provider.http;

import com.agoda.dm.constant.DownloadStatus;
import com.agoda.dm.exception.InvalidConfigurationException;
import com.agoda.dm.exception.UnableToDownloadFileException;
import com.agoda.dm.model.Configuration;
import com.agoda.dm.model.DownloadProgress;
import com.agoda.dm.spi.IFileDownloadManagerProvider;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 *
 * Implementation of http protocol for file download
 */
public class HttpFileDownloadManagerProvider implements IFileDownloadManagerProvider {

  private final String protocol = "HTTP";
  private Logger logger = LoggerFactory.getLogger(HttpFileDownloadManagerProvider.class);

  @Nonnull
  @Override
  public String getSupportedProtocol() {
    return protocol;
  }

  @Nonnull
  @Override
  public void validateConfiguration(@Nonnull Configuration configuration)
      throws InvalidConfigurationException {
    if (StringUtils.isEmpty(configuration.getHostName())) {
      throw new InvalidConfigurationException("Invalid host name .");
    } else if (StringUtils.isEmpty(configuration.getProtocol()) ||
        !protocol.equals(configuration.getProtocol().toUpperCase())) {
      throw new InvalidConfigurationException("Invalid protocol - " + configuration.getProtocol());
    }
  }

  @Nonnull
  @Override
  public DownloadProgress downloadFile(@Nonnull Configuration configuration)
      throws UnableToDownloadFileException {
    return downloadWithResumeCapability(configuration);
  }

  private File getTargetFileName(Configuration configuration) {
    return new File(
        configuration.getTargetFilePath() + File.separator + configuration.getTargetFileName());
  }

  private long getRemoteFileSize(Configuration configuration) throws Exception {
    HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(
        configuration.getProtocol() + "://" + configuration.getHostName() + configuration
            .getPath()).openConnection();
    httpURLConnection.setRequestMethod("HEAD");
    httpURLConnection.setDoInput(true);
    httpURLConnection.connect();
    return httpURLConnection.getContentLengthLong();
  }

  private DownloadProgress downloadWithResumeCapability(Configuration configuration)
      throws UnableToDownloadFileException {
    int currentNoOfTry = configuration.getMaxNoOfTry();
    List<String> listOfErrorMessage = new ArrayList<>();
    long remoteFileSize = 0;
    try {
      remoteFileSize = getRemoteFileSize(configuration);
      configuration.getDownloadProgress().setTotalRemoteFileSize(remoteFileSize);
    } catch (Exception e) {

    }

    HttpURLConnection httpURLConnection = null;
    while (currentNoOfTry > 0) {

      try {
        String sourceUrl =
            configuration.getProtocol() + "://" + configuration.getHostName() + configuration
                .getPath();
        if (httpURLConnection != null) {
          sourceUrl = callIfMovedToNewLocation(httpURLConnection);
          logger.warn("Resource moved to a new location - {}", sourceUrl);
        }
        long currentFileSize =
            (configuration.isResumeSupported()) ? getTargetFileName(configuration).length() : 0;
        httpURLConnection = (HttpURLConnection) new URL(
            sourceUrl).openConnection();
        httpURLConnection.setRequestMethod("GET");
        if (remoteFileSize != 0) {
          httpURLConnection.setRequestProperty("Range",
              "bytes=" + currentFileSize + "-" + configuration.getDownloadProgress()
                  .getTotalRemoteFileSize());
        }
        httpURLConnection.setDoInput(true);
        httpURLConnection.setConnectTimeout(configuration.getConnectionTimeOut());
        httpURLConnection.setReadTimeout(configuration.getReadTimeOut());
        httpURLConnection.connect();
        verifyResponseCode(httpURLConnection);
        configuration.getDownloadProgress()
            .setTotalRemoteFileSize(httpURLConnection.getContentLengthLong());
        configuration.getDownloadProgress().setDownloadStatus(DownloadStatus.DOWNLOADING);
        configuration.getDownloadProgress().setCurrentFileSize(currentFileSize);
        readFromNetwork(httpURLConnection, configuration);
        configuration.getDownloadProgress().setDownloadStatus(DownloadStatus.DOWNLOADED);
        System.out.println("File - " + configuration.getPath() + " downloaded successfully");
        return configuration.getDownloadProgress();
      } catch (Exception e) {
        currentNoOfTry--;
        listOfErrorMessage.add(e.toString());
      }
    }
    System.out.println("File - " + configuration.getPath() + " downloaded failed, "+listOfErrorMessage.toString());
    throw new UnableToDownloadFileException(
        "Download failed with all tries, reason - " + listOfErrorMessage.toString());
  }

  private String callIfMovedToNewLocation(HttpURLConnection httpURLConnection) throws IOException {
    if ((httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM) || (
        httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP)) {
      return httpURLConnection.getHeaderField("Location");
    }
    return "";
  }

  private File readFromNetwork(HttpURLConnection httpURLConnection, Configuration configuration)
      throws IOException {
    InputStream inputStream = httpURLConnection.getInputStream();
    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
        new FileOutputStream(getTargetFileName(configuration), true));
    byte[] buffer = new byte[configuration.getInMemoryBufferSize()];
    int bytesRead = 0;
    int in = 0;
    while ((bytesRead = inputStream.read(buffer)) != -1) {
      logger.info("Read bytes {}", bytesRead);
      in += bytesRead;
      bufferedOutputStream.write(buffer, 0, bytesRead);
      configuration.getDownloadProgress().setCurrentFileSize(in);
    }
    bufferedOutputStream.flush();
    bufferedOutputStream.close();
    return new File(configuration.getTargetFilePath());
  }

  private void verifyResponseCode(HttpURLConnection httpURLConnection)
      throws UnableToDownloadFileException {
    try {
      if (httpURLConnection.getResponseCode() != HttpURLConnection.HTTP_OK
          && httpURLConnection.getResponseCode() != HttpURLConnection.HTTP_PARTIAL) {
        throw new UnableToDownloadFileException("Http Response failed - Response code : " +
            httpURLConnection.getResponseCode() + " Message : " + httpURLConnection
            .getResponseMessage());
      }
    } catch (IOException e) {
      throw new UnableToDownloadFileException(e.getMessage());
    }
  }
}
