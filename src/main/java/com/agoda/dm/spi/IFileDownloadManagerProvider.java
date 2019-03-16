package com.agoda.dm.spi;

import com.agoda.dm.exception.InvalidConfigurationException;
import com.agoda.dm.exception.UnableToDownloadFileException;
import com.agoda.dm.model.Configuration;
import com.agoda.dm.model.DownloadProgress;
import javax.annotation.Nonnull;

/**
 * Service Provider Interface for File download service provider
 */
public interface IFileDownloadManagerProvider {

  /**
   * Service provider will validate the configuration and if some input is required then will throw
   * exception
   */
  @Nonnull
  public void validateConfiguration(@Nonnull Configuration configuration)
      throws InvalidConfigurationException;

  /**
   * Service provider will provide download File Functionality
   */
  @Nonnull
  public DownloadProgress downloadFile(@Nonnull Configuration configuration)
      throws UnableToDownloadFileException;

  /**
   * return supported protocol by service provider
   */
  @Nonnull
  public String getSupportedProtocol();

}
