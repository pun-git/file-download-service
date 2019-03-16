import com.agoda.dm.FileDownloadService;
import com.agoda.dm.constant.DownloadStatus;
import com.agoda.dm.model.ApplicationConfiguration;
import com.agoda.dm.model.DownloadProgress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Client {

  public static void main(String args[]) {
    AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(
        ApplicationConfiguration.class);
    FileDownloadService fileDownloadService = applicationContext.getBean(FileDownloadService.class);
    try {
      List<String> listOfSources = new ArrayList<>();
      listOfSources.add("http://sample-videos.com/video123/3gp/144/big_buck_bunny_144p_10mb.3gp");
      listOfSources.add("ftp://speedtest.tele2.net/1MB.zip");
      Map<String, DownloadProgress> sourceUrlToDownloadProgress = fileDownloadService.downloadFile(listOfSources, "./target");
      for (String sourceUrl : sourceUrlToDownloadProgress.keySet()) {
        waitForStatus(sourceUrl, sourceUrlToDownloadProgress.get(sourceUrl));
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  private static void waitForStatus(String sourceUrl, DownloadProgress sourceUrlToDownloadProgress)
      throws Exception {
    while (sourceUrlToDownloadProgress.getDownloadStatus() != DownloadStatus.FAILED
        && sourceUrlToDownloadProgress.getDownloadStatus() != DownloadStatus.DOWNLOADED) {
      Thread.sleep(1000);
    }
    if (sourceUrlToDownloadProgress.getExceptionsRecorded() != null) {
      System.out.println(sourceUrlToDownloadProgress.getExceptionsRecorded().getMessage());
    }
  }

}
