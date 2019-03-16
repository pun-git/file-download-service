package com.agoda.dm.providor.locator;

import com.agoda.dm.exception.UnsupportedProtocolException;
import com.agoda.dm.spi.IFileDownloadManagerProvider;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * To Locate File Download Service Provider
 */
@Component
final public class FileDownloadServiceProviderLocator {

  final private static ServiceLoader serviceLoader;
  final private static Map<String, IFileDownloadManagerProvider> mapOfProtocolAndServiceProvider = new ConcurrentHashMap<String, IFileDownloadManagerProvider>();

  static {
    serviceLoader = ServiceLoader.load(IFileDownloadManagerProvider.class);
    Iterator<IFileDownloadManagerProvider> fileDownloadServiceProviderListIterator = serviceLoader
        .iterator();

    while (fileDownloadServiceProviderListIterator.hasNext()) {
      IFileDownloadManagerProvider fileDownloadServiceProvider = fileDownloadServiceProviderListIterator
          .next();
      mapOfProtocolAndServiceProvider
          .put(fileDownloadServiceProvider.getSupportedProtocol().toUpperCase(),
              fileDownloadServiceProvider);
    }

  }

  /**
   * To return file download service provider based on input protocol
   */
  public IFileDownloadManagerProvider getFileDownloadServiceProvider(String protocol)
      throws UnsupportedProtocolException {
    if(StringUtils.isEmpty(protocol)) throw new RuntimeException("Invalid Protocol .");
    protocol = protocol.toUpperCase();
    if (mapOfProtocolAndServiceProvider.containsKey(protocol)) {
      return mapOfProtocolAndServiceProvider.get(protocol);
    } else {
      refreshServiceProviderCache();
      if (mapOfProtocolAndServiceProvider.containsKey(protocol)) {
        return mapOfProtocolAndServiceProvider.get(protocol);
      } else {
        throw new UnsupportedProtocolException(
            "Service provider for protocol - " + protocol + " is unavailable .");
      }
    }
  }

  /**
   * To clear the internal cache of ServiceLoader and reload available service provider details
   */
  private void refreshServiceProviderCache() {
    Iterator<IFileDownloadManagerProvider> fileDownloadServiceProviderListIterator = ServiceLoader
        .load(IFileDownloadManagerProvider.class).iterator();
    serviceLoader.reload();

    while (fileDownloadServiceProviderListIterator.hasNext()) {
      IFileDownloadManagerProvider fileDownloadServiceProvider = fileDownloadServiceProviderListIterator
          .next();
      mapOfProtocolAndServiceProvider
          .put(fileDownloadServiceProvider.getSupportedProtocol(), fileDownloadServiceProvider);
    }
  }


}
