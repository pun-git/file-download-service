package com.agoda.dm.tests;

import com.agoda.dm.FileDownloadService;
import com.agoda.dm.constant.DownloadStatus;
import com.agoda.dm.exception.UnableToDownloadFileException;
import com.agoda.dm.model.ApplicationConfiguration;
import com.agoda.dm.model.Configuration;
import com.agoda.dm.model.Configuration.ConfigurationBuilder;
import com.agoda.dm.model.DownloadProgress;
import com.agoda.dm.utilities.FileDownloadServiceUtility;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ApplicationConfiguration.class})
public class FileDownloadServiceIntegrationTests {

  @Autowired
  private FileDownloadService fileDownloadService;
  private List<String> httpSourceUrls = new ArrayList<>();
  private List<String> ftpSourceUrls = new ArrayList<>();
  private List<String> dummySourceUrls = new ArrayList<>();
  private List<String> invalidSourceUrls = new ArrayList<>();
  private String targetPath;

  @Before
  public void setup() {
    httpSourceUrls.add("http://www.w3.org/TR/PNG/iso_8859-1.txt");
    httpSourceUrls.add("http://sample-videos.com/video123/3gp/144/big_buck_bunny_144p_10mb.3gp");
    ftpSourceUrls.add("ftp://speedtest.tele2.net/1MB.zip");
    ftpSourceUrls.add("ftp://speedtest.tele2.net/10MB.zip");
    dummySourceUrls.add("dummy://www.w3.org/TR/PNG/iso_8859-1.txt");
    targetPath = "./targetFile/";
    invalidSourceUrls.add("http://www.url123.org/TR/PNG/iso_8859-1.txt");
  }

  @Test
  public void testDownloadFileForInvalidProtocol() throws Exception{
    Map<String, DownloadProgress> sourceUrlToDownloadProgress = fileDownloadService
        .downloadFile(dummySourceUrls, targetPath);
    for (String sourceUrl : sourceUrlToDownloadProgress.keySet()) {
      waitForStatus(sourceUrl, sourceUrlToDownloadProgress.get(sourceUrl));
      Assert.assertEquals(DownloadStatus.FAILED, sourceUrlToDownloadProgress.get(sourceUrl).getDownloadStatus());
    }
  }

  @Test
  public void testDownloadFileForHttp() throws Exception {
    Map<String, DownloadProgress> sourceUrlToDownloadProgress = fileDownloadService
        .downloadFile(httpSourceUrls, targetPath);
    for (String sourceUrl : sourceUrlToDownloadProgress.keySet()) {
      waitForStatus(sourceUrl, sourceUrlToDownloadProgress.get(sourceUrl));
      Assert.assertEquals(sourceUrlToDownloadProgress.get(sourceUrl).getCurrentFileSize(),
          sourceUrlToDownloadProgress.get(sourceUrl).getTotalRemoteFileSize());
    }
  }

  @Test
  public void testDownloadFileForFtp() throws Exception {
    Map<String, DownloadProgress> sourceUrlToDownloadProgress = fileDownloadService
        .downloadFile(ftpSourceUrls, targetPath);
    for (String sourceUrl : sourceUrlToDownloadProgress.keySet()) {
      waitForStatus(sourceUrl, sourceUrlToDownloadProgress.get(sourceUrl));
      Assert.assertNotEquals(0, sourceUrlToDownloadProgress.get(sourceUrl).getCurrentFileSize());
    }
  }

  @Test(expected = UnknownHostException.class)
  public void testDownloadFileForInvalidHost() throws Throwable{
    Map<String, DownloadProgress> sourceUrlToDownloadProgress = fileDownloadService
        .downloadFile(invalidSourceUrls, targetPath);
    for (String sourceUrl : sourceUrlToDownloadProgress.keySet()) {
      waitForStatus(sourceUrl, sourceUrlToDownloadProgress.get(sourceUrl));
      Assert.assertEquals(DownloadStatus.FAILED, sourceUrlToDownloadProgress.get(sourceUrl).getDownloadStatus());
      throw sourceUrlToDownloadProgress.get(sourceUrl).getExceptionsRecorded().getCause();
    }
  }

  @Test(expected = UnableToDownloadFileException.class)
  public void testDownloadFileWithConnectionTimeOut() throws Throwable{
    List<Configuration> listOfConfiguration = new ArrayList<>();
    listOfConfiguration.add(buildConfigFromUrlWithConnectionTimeOut(httpSourceUrls.get(0), targetPath));
    Map<String, DownloadProgress> sourceUrlToDownloadProgress = fileDownloadService
        .downloadFile(listOfConfiguration);
    for (String sourceUrl : sourceUrlToDownloadProgress.keySet()) {
      waitForStatus(sourceUrl, sourceUrlToDownloadProgress.get(sourceUrl));
      Assert.assertEquals(DownloadStatus.FAILED, sourceUrlToDownloadProgress.get(sourceUrl).getDownloadStatus());
      throw sourceUrlToDownloadProgress.get(sourceUrl).getExceptionsRecorded().getCause();
    }
  }

  @Test(expected = UnableToDownloadFileException.class)
  public void testDownloadFileWithReadTimeOut() throws Throwable{
    List<Configuration> listOfConfiguration = new ArrayList<>();
    listOfConfiguration.add(buildConfigFromUrlWithConnectionTimeOut(httpSourceUrls.get(0), targetPath));
    Map<String, DownloadProgress> sourceUrlToDownloadProgress = fileDownloadService
        .downloadFile(listOfConfiguration);
    for (String sourceUrl : sourceUrlToDownloadProgress.keySet()) {
      waitForStatus(sourceUrl, sourceUrlToDownloadProgress.get(sourceUrl));
      Assert.assertEquals(DownloadStatus.FAILED, sourceUrlToDownloadProgress.get(sourceUrl).getDownloadStatus());
      throw sourceUrlToDownloadProgress.get(sourceUrl).getExceptionsRecorded().getCause();
    }
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

  public static Configuration buildConfigFromUrlWithConnectionTimeOut(String sourceFileUrl, String targetFolder)
      throws MalformedURLException {
    ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
    configurationBuilder.buildProtocol(FileDownloadServiceUtility.getProtocolFromSourceUrl(sourceFileUrl));
    configurationBuilder.buildHostName(FileDownloadServiceUtility.getHostNameFromSourceUrl(sourceFileUrl));
    configurationBuilder.buildPort(FileDownloadServiceUtility.getPortFromSourceUrl(sourceFileUrl));
    configurationBuilder.buildFilePath(FileDownloadServiceUtility.getPathFromSourceUrl(sourceFileUrl));
    configurationBuilder.buildDownloadProgress(new DownloadProgress());
    configurationBuilder.buildTargetFilePath(targetFolder);
    configurationBuilder.buildTargetFileName(FileDownloadServiceUtility.getTargetFileName(sourceFileUrl));
    configurationBuilder.buildConnectionTimeOut((int)TimeUnit.SECONDS.toSeconds(-1));
    FileDownloadServiceUtility.buildQueryParameter(FileDownloadServiceUtility.getQueryParameter(sourceFileUrl)).forEach((key, value) -> {
      configurationBuilder.buildAuthKeyValue(key, value);
    });
    return configurationBuilder.build();
  }

  public static Configuration buildConfigFromUrlWithReadTimeOut(String sourceFileUrl, String targetFolder)
      throws MalformedURLException {
    ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
    configurationBuilder.buildProtocol(FileDownloadServiceUtility.getProtocolFromSourceUrl(sourceFileUrl));
    configurationBuilder.buildHostName(FileDownloadServiceUtility.getHostNameFromSourceUrl(sourceFileUrl));
    configurationBuilder.buildPort(FileDownloadServiceUtility.getPortFromSourceUrl(sourceFileUrl));
    configurationBuilder.buildFilePath(FileDownloadServiceUtility.getPathFromSourceUrl(sourceFileUrl));
    configurationBuilder.buildDownloadProgress(new DownloadProgress());
    configurationBuilder.buildTargetFilePath(targetFolder);
    configurationBuilder.buildTargetFileName(FileDownloadServiceUtility.getTargetFileName(sourceFileUrl));
    configurationBuilder.buildReadTimeOut((int)TimeUnit.SECONDS.toSeconds(-1));
    FileDownloadServiceUtility.buildQueryParameter(FileDownloadServiceUtility.getQueryParameter(sourceFileUrl)).forEach((key, value) -> {
      configurationBuilder.buildAuthKeyValue(key, value);
    });
    return configurationBuilder.build();
  }
}
