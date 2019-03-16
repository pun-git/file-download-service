package com.agoda.dm.tests;


import com.agoda.dm.utilities.FileDownloadServiceUtility;
import java.net.MalformedURLException;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FileDownloadServiceUtilitiesTest {

  private final String invalidUrl = "://host";
  private final String urlWithInvalidHostName = "http";
  private final String urlWithValidHostName = "http://host/source";
  private final String validUrl = "http://host/source";
  private final String validProtocol = "http";
  private final String urlWithValidPort = "http://host:1009";
  private final String urlWithInValidPort = "http://host:1009a";
  String dummyUserName = "dummyuserName";
  String dummyUserPassword = "dummyuserPassword";
  String query = "userName="+dummyUserName+"&userPassword="+dummyUserPassword;
  String urlWithQueryParameter = urlWithValidPort+"?userName="+dummyUserName+"&userPassword="+dummyUserPassword;

  @Test(expected = MalformedURLException.class)
  public void testInvalidUrl() throws Exception {
    FileDownloadServiceUtility.getProtocolFromSourceUrl(invalidUrl);
  }

  @Test
  public void testValidUrl() throws Exception {
    String protocol = FileDownloadServiceUtility.getProtocolFromSourceUrl(validUrl);
    Assert.assertEquals(validProtocol, protocol);
  }

  @Test(expected = MalformedURLException.class)
  public void testInvalidHostName() throws Exception {
    FileDownloadServiceUtility.getHostNameFromSourceUrl(urlWithInvalidHostName);
  }

  @Test
  public void testValidHostName() throws Exception {
    String hostName = FileDownloadServiceUtility.getHostNameFromSourceUrl(urlWithValidHostName);
    Assert.assertEquals("host", hostName);
  }

  @Test(expected = MalformedURLException.class)
  public void testInvalidPortNo() throws Exception {
    FileDownloadServiceUtility.getPortFromSourceUrl(urlWithInValidPort);
  }

  @Test
  public void testValidPortNo() throws Exception {
    int portNo = FileDownloadServiceUtility.getPortFromSourceUrl(urlWithValidPort);
    Assert.assertEquals(1009, portNo);
  }

  @Test
  public void testValidHostWithPath() throws Exception{
      String path = FileDownloadServiceUtility.getPathFromSourceUrl(urlWithValidHostName);
      Assert.assertEquals("/source",path);
  }

  @Test
  public void testGetQueryParameter() throws Exception{
    Assert.assertEquals(query, FileDownloadServiceUtility.getQueryParameter(urlWithQueryParameter));
  }

  @Test
  public void testBuildQueryParameterWithValidInput() throws Exception{
    Map<String,String> keyToValue = FileDownloadServiceUtility.buildQueryParameter(query);
    Assert.assertEquals(dummyUserName, keyToValue.get("userName"));
    Assert.assertEquals(dummyUserPassword, keyToValue.get("userPassword"));
  }
}
