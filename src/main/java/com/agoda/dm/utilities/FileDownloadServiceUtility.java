package com.agoda.dm.utilities;

import com.agoda.dm.model.Configuration;
import com.agoda.dm.model.Configuration.ConfigurationBuilder;
import com.agoda.dm.model.DownloadProgress;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.springframework.util.StringUtils;

/**
 * File Download Service Utilities
 */
final public class FileDownloadServiceUtility {

  private static final Logger logger = Logger.getLogger(FileDownloadServiceUtility.class.getName());

  /**
   * Get protocol from input url
   */
  public static final String getProtocolFromSourceUrl(String inputUrl)
      throws MalformedURLException {
    URL url = new URL(inputUrl);
    return url.getProtocol();
  }

  public static final String getHostNameFromSourceUrl(String inputUrl)
      throws MalformedURLException {
    URL url = new URL(inputUrl);
    return url.getHost();
  }

  public static final int getPortFromSourceUrl(String inputUrl) throws MalformedURLException {
    URL url = new URL(inputUrl);
    return url.getPort();
  }

  public static final String getPathFromSourceUrl(String inputUrl) throws MalformedURLException {
    URL url = new URL(inputUrl);
    return url.getPath();
  }

  public static final String getQueryParameter(String inputUrl) throws MalformedURLException {
    URL url = new URL(inputUrl);
    return url.getQuery();
  }


  /**
   * To check Host is accessible
   *
   * @param inputUrl InputUrl
   * @param timeOut Time Out in seconds
   */
  public static boolean isHostAccessible(String inputUrl, long timeOut)
      throws IOException {
    URL url = new URL(inputUrl);
    try {
      URLConnection urlConnection = url.openConnection();
      urlConnection.setConnectTimeout((int)timeOut);
      urlConnection.connect();
    } catch (UnknownHostException e) {
      throw new UnknownHostException("Host is not reachable");
    }
    return true;
  }

  public static String getURL(Configuration configuration) {
    return
        configuration.getProtocol() + "://" + configuration.getHostName() + configuration
            .getPath();
  }

  public static Configuration buildConfigFromUrl(String sourceFileUrl, String targetFolder)
      throws MalformedURLException {
    ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
    configurationBuilder.buildProtocol(getProtocolFromSourceUrl(sourceFileUrl));
    configurationBuilder.buildHostName(getHostNameFromSourceUrl(sourceFileUrl));
    configurationBuilder.buildPort(getPortFromSourceUrl(sourceFileUrl));
    configurationBuilder.buildFilePath(getPathFromSourceUrl(sourceFileUrl));
    configurationBuilder.buildDownloadProgress(new DownloadProgress());
    configurationBuilder.buildTargetFilePath(targetFolder);
    configurationBuilder.buildTargetFileName(getTargetFileName(sourceFileUrl));
    buildQueryParameter(getQueryParameter(sourceFileUrl)).forEach((key, value) -> {
      configurationBuilder.buildAuthKeyValue(key, value);
    });
    return configurationBuilder.build();
  }

  public static Map<String, String> buildQueryParameter(String queryParameter) {
    Map<String, String> keyToValues = new HashMap<>();
    if (StringUtils.isEmpty(queryParameter)) {
      return keyToValues;
    }
    String[] queryParameters = queryParameter.split("&");
    if (queryParameters.length == 0) {
      return keyToValues;
    }
    for (String query : queryParameters) {
      String[] keyToValue = query.split("=");
      if (keyToValue.length < 2) {
        continue;
      }
      keyToValues.put(keyToValue[0], keyToValue[1]);
    }
    return keyToValues;
  }

  public static String getTargetFileName(String sourceFile) throws MalformedURLException {
    return new File(getPathFromSourceUrl(sourceFile)).getName();
  }

}
