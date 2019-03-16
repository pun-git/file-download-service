package com.agoda.dm.tests;

import com.agoda.dm.exception.UnsupportedProtocolException;
import com.agoda.dm.model.ApplicationConfiguration;
import com.agoda.dm.providor.locator.FileDownloadServiceProviderLocator;
import com.agoda.dm.spi.IFileDownloadManagerProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ApplicationConfiguration.class})
public class FileDownloadServiceProviderLocatorTest {

  @Autowired
  private FileDownloadServiceProviderLocator fileDownloadServiceProviderLocator;
  private String validProtocol = "http";
  private String invalidProtocol = "dummyProtocol";

  @Before
  public void setUp(){
  }

  @Test(expected = UnsupportedProtocolException.class)
  public void testForInvalidProtocol(){
    fileDownloadServiceProviderLocator.getFileDownloadServiceProvider(invalidProtocol);
  }

  @Test
  public void testForValidProtocol(){
    IFileDownloadManagerProvider fileDownloadManagerProvider = fileDownloadServiceProviderLocator.getFileDownloadServiceProvider(validProtocol);
    Assert.assertNotNull(fileDownloadManagerProvider);
  }
}
