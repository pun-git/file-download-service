package com.agoda.dm.provider.ftp;

import com.agoda.dm.constant.DownloadStatus;
import com.agoda.dm.exception.InvalidConfigurationException;
import com.agoda.dm.exception.UnableToDownloadFileException;
import com.agoda.dm.model.Configuration;
import com.agoda.dm.model.DownloadProgress;
import com.agoda.dm.spi.IFileDownloadManagerProvider;
import com.google.common.annotations.VisibleForTesting;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * Implementation for Ftp protocol for file download
 */
public class FtpFileDownloadManagerProvider implements IFileDownloadManagerProvider {

  private final String protocol = "FTP";
  private final String urlFormat = "ftp://%s/%s;type=i";
  private Logger logger = LoggerFactory.getLogger(FtpFileDownloadManagerProvider.class);

  @Nonnull
  @Override
  public String getSupportedProtocol() {
    return protocol;
  }

  @Nonnull
  @Override
  public void validateConfiguration(@Nonnull Configuration configuration)
      throws InvalidConfigurationException {
    String host = configuration.getHostName();
    if (StringUtils.isEmpty(host)) {
      throw new InvalidConfigurationException("Invalid Host Name .");
    }
  }

  @Nonnull
  @Override
  public DownloadProgress downloadFile(@Nonnull Configuration configuration)
      throws UnableToDownloadFileException {
    try {
      InputStream inputStream = getInputStream(configuration);
      OutputStream outputStream = getOuputStream(configuration);
      configuration.getDownloadProgress().setDownloadStatus(DownloadStatus.DOWNLOADING);
      byte[] buffer = new byte[configuration.getInMemoryBufferSize()];
      int bytesRead = 0;
      int in = 0;
      while ((bytesRead = inputStream.read(buffer)) != -1) {
        logger.info("Read bytes {}", bytesRead);
        in += bytesRead;
        outputStream.write(buffer, 0, bytesRead);
        configuration.getDownloadProgress().setCurrentFileSize(in);
      }
      outputStream.flush();
      outputStream.close();

    } catch (IOException e) {
      System.out.println("File - " + configuration.getPath() + " downloaded failed, "+e.toString());
      configuration.getDownloadProgress().setDownloadStatus(DownloadStatus.FAILED);
      throw new UnableToDownloadFileException(e.getMessage());
    }
    System.out.println("File - " + configuration.getPath() + " downloaded successfully");
    configuration.getDownloadProgress().setDownloadStatus(DownloadStatus.DOWNLOADED);
    return configuration.getDownloadProgress();
  }

  @VisibleForTesting
  public InputStream getInputStream(Configuration configuration) throws IOException {
    String ftpUrl = String
        .format(urlFormat, configuration.getHostName(), configuration.getPath());
    URL url = new URL(ftpUrl);
    URLConnection urlConnection = url.openConnection();
    urlConnection.setConnectTimeout(configuration.getConnectionTimeOut());
    urlConnection.setReadTimeout(configuration.getReadTimeOut());
    urlConnection.connect();
    return urlConnection.getInputStream();
  }

  @VisibleForTesting
  public OutputStream getOuputStream(Configuration configuration) throws FileNotFoundException {
        return  new FileOutputStream(
            configuration.getTargetFilePath() + File.separator + configuration.getTargetFileName());
  }

}
