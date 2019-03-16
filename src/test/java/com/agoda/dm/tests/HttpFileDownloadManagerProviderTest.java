package com.agoda.dm.tests;

import com.agoda.dm.exception.InvalidConfigurationException;
import com.agoda.dm.model.ApplicationConfiguration;
import com.agoda.dm.model.Configuration;
import com.agoda.dm.model.Configuration.ConfigurationBuilder;
import com.agoda.dm.provider.http.HttpFileDownloadManagerProvider;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ApplicationConfiguration.class})
public class HttpFileDownloadManagerProviderTest {

  private HttpFileDownloadManagerProvider httpFileDownloadManagerProvider;

  @Before
  public void setUp(){
      httpFileDownloadManagerProvider = Mockito.spy(new HttpFileDownloadManagerProvider());
  }


  @Test(expected = InvalidConfigurationException.class)
  public void testValidateConfigurationWithInvalidConfiguration(){
    Configuration configuration = new ConfigurationBuilder().build();
    httpFileDownloadManagerProvider.validateConfiguration(configuration);
  }

  @Test
  public void testValidateConfigurationWithValidConfiguration(){
    ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
    Configuration configuration = configurationBuilder.buildHostName("DummyHostName").buildProtocol("HTTP").build();
    httpFileDownloadManagerProvider.validateConfiguration(configuration);
  }

  @Test
  public void testGetSupportedProtocol(){
    Assert.assertEquals("HTTP",httpFileDownloadManagerProvider.getSupportedProtocol());
  }

}
