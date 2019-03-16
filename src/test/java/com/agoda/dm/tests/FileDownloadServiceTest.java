package com.agoda.dm.tests;

import com.agoda.dm.FileDownloadService;
import com.agoda.dm.constant.DownloadStatus;
import com.agoda.dm.exception.InvalidConfigurationException;
import com.agoda.dm.exception.UnableToDownloadFileException;
import com.agoda.dm.exception.UnsupportedProtocolException;
import com.agoda.dm.model.ApplicationConfiguration;
import com.agoda.dm.model.Configuration;
import com.agoda.dm.model.Configuration.ConfigurationBuilder;
import com.agoda.dm.model.DownloadProgress;
import com.agoda.dm.spi.IFileDownloadManagerProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ApplicationConfiguration.class})
public class FileDownloadServiceTest {

  @Autowired
  private FileDownloadService fileDownloadService;

  @Before
  public void setUp(){
    fileDownloadService = Mockito.spy(fileDownloadService);
  }


  public void testDownloadFile(){
      Configuration configuration = getConfiguration();
      Mockito.doReturn(configuration).when(fileDownloadService).buildConfigForSource("SourceURL", "TargetUrl");
      Mockito.doNothing().when(fileDownloadService).executeFileDownloadFlowForSource(configuration);
      List<String> sourceUrl = new ArrayList<>();
      sourceUrl.add("SourceURL");
      configuration.getDownloadProgress().setDownloadStatus(DownloadStatus.DOWNLOADED);
      Map<String, DownloadProgress> sourceUrlToDownloadProgress = fileDownloadService.downloadFile(sourceUrl, "TargetUrl");
      Assert.assertEquals(1, sourceUrlToDownloadProgress.size());
      Assert.assertEquals(DownloadStatus.DOWNLOADED, sourceUrlToDownloadProgress.get("SourceURL").getDownloadStatus());
  }

  @Test(expected = UnsupportedProtocolException.class)
  public void testDownloadFileForUnsupportedProtocol() throws Throwable{
    Mockito.doReturn(getConfiguration()).when(fileDownloadService).buildConfigForSource("sourceUrl", "targetUrl");
    List<String> sourceUrl = new ArrayList<>();
    sourceUrl.add("sourceUrl");
    Map<String, DownloadProgress> downloadProgress = fileDownloadService.downloadFile(sourceUrl, "targetUrl");
    DownloadProgress downloadProgress1 = downloadProgress.get("sourceUrl");
    waitForStatus("sourceUrl", downloadProgress1);
    throw downloadProgress1.getExceptionsRecorded().getCause();
  }

  @Test(expected = InvalidConfigurationException.class)
  public void testValidateConfiguration() throws Throwable{
    Mockito.doReturn(new DummyFileDownloadManagerProvider1()).when(fileDownloadService).getDownloadManagerServiceProvider("DUMMYPROTOCOL");
    Mockito.doReturn(getConfiguration()).when(fileDownloadService).buildConfigForSource("dummyProtocol://www.w3.org/TR/PNG/iso_8859-1.txt", "targetUrl");
    List<String> sourceUrl = new ArrayList<>();
    sourceUrl.add("dummyProtocol://www.w3.org/TR/PNG/iso_8859-1.txt");
    Map<String, DownloadProgress> downloadProgress = fileDownloadService.downloadFile(sourceUrl, "targetUrl");
    DownloadProgress downloadProgress1 = downloadProgress.get("dummyProtocol://www.w3.org/TR/PNG/iso_8859-1.txt");
    waitForStatus("dummyProtocol://www.w3.org/TR/PNG/iso_8859-1.txt", downloadProgress1);
    throw downloadProgress1.getExceptionsRecorded().getCause();
  }

  @Test(expected = UnableToDownloadFileException.class)
  public void testDownloadFileFailedWithException() throws Throwable {
    Mockito.doReturn(new DummyFileDownloadManagerProvider()).when(fileDownloadService).getDownloadManagerServiceProvider("DUMMYPROTOCOL");
    Mockito.doReturn(getConfiguration()).when(fileDownloadService).buildConfigForSource("dummyProtocol://www.w3.org/TR/PNG/iso_8859-1.txt", "targetUrl");
    List<String> sourceUrl = new ArrayList<>();
    sourceUrl.add("dummyProtocol://www.w3.org/TR/PNG/iso_8859-1.txt");
    Map<String, DownloadProgress> downloadProgress = fileDownloadService.downloadFile(sourceUrl, "targetUrl");
    DownloadProgress downloadProgress3 = downloadProgress.get("dummyProtocol://www.w3.org/TR/PNG/iso_8859-1.txt");
    waitForStatus("dummyProtocol://www.w3.org/TR/PNG/iso_8859-1.txt", downloadProgress3);
    throw downloadProgress3.getExceptionsRecorded().getCause();
  }

  @Test
  public void testFileDroppedWhenDownloadFileFailed(){


  }

  private void waitForStatus(String sourceUrl, DownloadProgress sourceUrlToDownloadProgress)
      throws Exception {
    while (sourceUrlToDownloadProgress.getDownloadStatus() != DownloadStatus.FAILED
        && sourceUrlToDownloadProgress.getDownloadStatus() != DownloadStatus.DOWNLOADED) {
      Thread.sleep(1000);
    }
    if (sourceUrlToDownloadProgress.getExceptionsRecorded() != null) {
      System.out.println(sourceUrlToDownloadProgress.getExceptionsRecorded().getMessage());
    }
  }

  private Configuration getConfiguration(){
    return new ConfigurationBuilder().
        buildHostName("dummyHostName").
        buildPort(1000).
        buildProtocol("dummyProtocol").
        buildFilePath("config").
        buildDownloadProgress(new DownloadProgress()).
        build();
  }

  static class DummyFileDownloadManagerProvider implements IFileDownloadManagerProvider{

    @Nonnull
    @Override
    public String getSupportedProtocol() {
      return "dummyProtocol";
    }

    @Nonnull
    @Override
    public DownloadProgress downloadFile(@Nonnull Configuration configuration)
        throws UnableToDownloadFileException {
      throw new UnableToDownloadFileException("Unable to download file");
    }

    @Nonnull
    @Override
    public void validateConfiguration(@Nonnull Configuration configuration)
        throws InvalidConfigurationException {
    }
  }

  static class DummyFileDownloadManagerProvider1 implements IFileDownloadManagerProvider{

    @Nonnull
    @Override
    public String getSupportedProtocol() {
      return "dummyProtocol";
    }

    @Nonnull
    @Override
    public DownloadProgress downloadFile(@Nonnull Configuration configuration)
        throws UnableToDownloadFileException {
      throw new UnableToDownloadFileException("Unable to download file");
    }

    @Nonnull
    @Override
    public void validateConfiguration(@Nonnull Configuration configuration){
        throw new InvalidConfigurationException("Invalid configuration .");
    }
  }
}
