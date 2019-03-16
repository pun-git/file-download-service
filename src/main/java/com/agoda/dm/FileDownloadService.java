package com.agoda.dm;


import com.agoda.dm.constant.DownloadStatus;
import com.agoda.dm.exception.InvalidConfigurationException;
import com.agoda.dm.exception.UnableToDownloadFileException;
import com.agoda.dm.exception.UnsupportedProtocolException;
import com.agoda.dm.model.Configuration;
import com.agoda.dm.model.Configuration.ConfigurationBuilder;
import com.agoda.dm.model.DownloadProgress;
import com.agoda.dm.providor.locator.FileDownloadServiceProviderLocator;
import com.agoda.dm.spi.IFileDownloadManagerProvider;
import com.agoda.dm.utilities.FileDownloadServiceUtility;
import com.google.common.annotations.VisibleForTesting;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Service (Singleton) to download file
 */
@Component
public class FileDownloadService {

  @Autowired
  private FileDownloadServiceProviderLocator fileDownloadServiceProviderLocator;
  private Logger logger = LoggerFactory.getLogger(FileDownloadService.class);

  /**
   * Download File from source with default configuration
   */
  public Map<String, DownloadProgress> downloadFile(List<String> sourceFileUrl,
      String targetFolderPath) {

    Map<String, DownloadProgress> sourceUrlToDownloadProgress = new ConcurrentHashMap<>();
    sourceFileUrl.forEach((sourceUrl) -> {
      try {
        Configuration configuration = buildConfigForSource(sourceUrl, targetFolderPath);
        executeFileDownloadFlowForSource(configuration);
        sourceUrlToDownloadProgress.put(sourceUrl, configuration.getDownloadProgress());
      }catch (UnableToDownloadFileException exception){
        sourceUrlToDownloadProgress.put(sourceUrl, getDownloadProgressForFailure(exception));
      }
    });

    return sourceUrlToDownloadProgress;
  }

  /**
   * Download File from source with user defined configuration
   */
  public Map<String, DownloadProgress> downloadFile(List<Configuration> configurationOfSourceFiles) {

    Map<String, DownloadProgress> sourceUrlToDownloadProgress = new ConcurrentHashMap<>();
    configurationOfSourceFiles.forEach((configuration) -> {
      try {
        executeFileDownloadFlowForSource(configuration);
        sourceUrlToDownloadProgress.put(FileDownloadServiceUtility.getURL(configuration), configuration.getDownloadProgress());
      }catch (UnableToDownloadFileException exception){
        sourceUrlToDownloadProgress.put(FileDownloadServiceUtility.getURL(configuration), getDownloadProgressForFailure(exception));
      }
    });

    return sourceUrlToDownloadProgress;
  }

  @VisibleForTesting
  public Configuration buildConfigForSource(String sourceFileUrl, String targetFilePath) {
      try {
        Configuration configuration = FileDownloadServiceUtility.buildConfigFromUrl(sourceFileUrl, targetFilePath);
        FileDownloadServiceUtility.isHostAccessible(FileDownloadServiceUtility.getURL(configuration), configuration.getTimeOutInMin());
        return configuration;
      } catch (IOException e) {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        Configuration configuration = configurationBuilder.buildDownloadProgress(new DownloadProgress()).build();
        handleFailure(configuration.getDownloadProgress(), e);
        return configuration;
      }
  }

  @VisibleForTesting
  public void executeFileDownloadFlowForSource(Configuration configuration){

        CompletableFuture.supplyAsync(() ->  getDownloadManagerServiceProvider(configuration.getProtocol().toUpperCase())).
        whenCompleteAsync((iFileDownloadManagerProvider, throwable) -> handleFailure(configuration.getDownloadProgress(), throwable)).

        thenApplyAsync(iFileDownloadManagerProvider -> validateConfiguration(iFileDownloadManagerProvider,configuration)).
        whenCompleteAsync((iFileDownloadManagerProvider, throwable) -> handleFailure(configuration.getDownloadProgress(), throwable)).

        thenApplyAsync(iFileDownloadManagerProvider -> downloadFileForSource(iFileDownloadManagerProvider,configuration)).
        whenCompleteAsync((iFileDownloadManagerProvider, throwable) -> handleFailure(configuration.getDownloadProgress(), throwable));

  }

  @VisibleForTesting
  public IFileDownloadManagerProvider getDownloadManagerServiceProvider(String protocol) throws UnsupportedProtocolException {
      logger.info("Running getDownloadManager Task .");
      return fileDownloadServiceProviderLocator
          .getFileDownloadServiceProvider(protocol);
  }

  @VisibleForTesting
  public IFileDownloadManagerProvider validateConfiguration(
      IFileDownloadManagerProvider fileDownloadServiceProvider, Configuration configuration) throws InvalidConfigurationException {

      fileDownloadServiceProvider.validateConfiguration(configuration);
      return fileDownloadServiceProvider;
  }

  private DownloadProgress downloadFileForSource(IFileDownloadManagerProvider fileDownloadServiceProvider, Configuration configuration) throws UnableToDownloadFileException{
    File targetFile = new File(
        configuration.getTargetFilePath() + File.separator + configuration.getTargetFileName());
    try {
      recreateTargetFile(targetFile);
      return fileDownloadServiceProvider.downloadFile(configuration);
    } catch (UnableToDownloadFileException| IOException e) {
      deleteIfExists(targetFile);
      throw new UnableToDownloadFileException(e.getMessage());
    }
  }

  private void recreateTargetFile(File targetFile) throws IOException {
    deleteIfExists(targetFile);
    createTargetFolderIfNotExist(targetFile);
    targetFile.createNewFile();
  }

  private void createTargetFolderIfNotExist(File targetFile) {
    if (!targetFile.getParentFile().exists()) {
      targetFile.getParentFile().mkdirs();
    }
  }

  private void deleteIfExists(File targetFile) {
    if (targetFile.exists()) {
      targetFile.delete();
    }
  }

  private void handleFailure(DownloadProgress downloadProgress, Throwable e){
    if(e != null) {
      logFailure(downloadProgress, e);
      throw new UnableToDownloadFileException(e);
    }
  }

  private void logFailure(DownloadProgress downloadProgress, Throwable e) {
    if(e != null) {
      downloadProgress.setDownloadStatus(DownloadStatus.FAILED);
      downloadProgress.setExceptionsRecorded(e);
      logger.error("File Download Failed with error ,", e);
    }
  }

  private DownloadProgress getDownloadProgressForFailure(Exception exception){
    DownloadProgress downloadProgress = new DownloadProgress();
    downloadProgress.setDownloadStatus(DownloadStatus.FAILED);
    downloadProgress.setExceptionsRecorded(exception);
    return  downloadProgress;
  }
}
