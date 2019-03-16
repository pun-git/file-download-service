package com.agoda.dm.tests;

import com.agoda.dm.constant.DownloadStatus;
import com.agoda.dm.exception.InvalidConfigurationException;
import com.agoda.dm.model.ApplicationConfiguration;
import com.agoda.dm.model.Configuration;
import com.agoda.dm.model.Configuration.ConfigurationBuilder;
import com.agoda.dm.model.DownloadProgress;
import com.agoda.dm.provider.ftp.FtpFileDownloadManagerProvider;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ApplicationConfiguration.class})
public class FtpFileDownloadManagerProviderTest {

  private FtpFileDownloadManagerProvider ftpFileDownloadManagerProvider;

  @Before
  public void setUp(){
    ftpFileDownloadManagerProvider = Mockito.spy(new FtpFileDownloadManagerProvider());
  }

  @Test(expected = InvalidConfigurationException.class)
  public void testValidateConfigurationWithInvalidConfiguration(){
    Configuration configuration = new ConfigurationBuilder().build();
    ftpFileDownloadManagerProvider.validateConfiguration(configuration);
  }

  @Test
  public void testValidateConfigurationWithValidConfiguration(){
    ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
    Configuration configuration = configurationBuilder.buildHostName("DummyHostName").build();
    ftpFileDownloadManagerProvider.validateConfiguration(configuration);
  }

  @Test
  public void testDownloadFile() throws Exception{
    ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
    configurationBuilder.buildHostName("dummyHostName");
    configurationBuilder.buildDownloadProgress(new DownloadProgress());
    configurationBuilder.buildPort(1000);
    Configuration configuration = configurationBuilder.build();

    InputStream byteInputStream = new ByteArrayInputStream("This is content of a file".getBytes());
    OutputStream byteOutputStream = new ByteArrayOutputStream();
    Mockito.doReturn(byteInputStream).when(ftpFileDownloadManagerProvider).getInputStream(configuration);
    Mockito.doReturn(byteOutputStream).when(ftpFileDownloadManagerProvider).getOuputStream(configuration);
    DownloadProgress downloadProgress = ftpFileDownloadManagerProvider.downloadFile(configuration);
    Assert.assertEquals(DownloadStatus.DOWNLOADED, downloadProgress.getDownloadStatus());
    Assert.assertEquals(byteOutputStream.toString(), byteOutputStream.toString());
  }

  @Test
  public void testGetSupportedProtocol(){
    Assert.assertEquals("FTP",ftpFileDownloadManagerProvider.getSupportedProtocol());
  }

}
