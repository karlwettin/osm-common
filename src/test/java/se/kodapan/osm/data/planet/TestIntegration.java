package se.kodapan.osm.data.planet;

import junit.framework.TestCase;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import se.kodapan.osm.data.planet.changesetstore.ChangesetStore;
import se.kodapan.osm.data.planet.changesetstore.ChangesetStoreState;
import se.kodapan.osm.data.planet.parser.xml.instantiated.InstantiatedOsmXmlParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author kalle
 * @since 2013-05-04 15:05
 */
public class TestIntegration extends TestCase {

  public void test() throws Exception {

    InstantiatedOsmXmlParser parser = new InstantiatedOsmXmlParser();

    HttpClient httpClient = new DefaultHttpClient();

    HttpResponse httpResponse;
    Date dateDaysAgo;
    int daysAgo = 3;
    while (true) {

      daysAgo++;
      dateDaysAgo = new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 24 * daysAgo));

      StringBuilder urlBuilder = new StringBuilder();
      urlBuilder.append("http://download.geofabrik.de/europe/malta-");
      urlBuilder.append(new SimpleDateFormat("yyMMdd").format(dateDaysAgo));
      urlBuilder.append(".osm.bz2");

      HttpGet get = new HttpGet(urlBuilder.toString());
      httpResponse = httpClient.execute(get);
      if (httpResponse.getStatusLine().getStatusCode() != 200) {
        if (daysAgo > 10) {
          fail("Server does not contain enough old data for test to run.");
        }
      }

      Reader reader;
      try {
        reader = new InputStreamReader(new BZip2CompressorInputStream(httpResponse.getEntity().getContent()), "UTF8");
        parser.parse(reader);
        reader.close();
        break;
      } catch (IOException e) {
        e.printStackTrace();
        continue;
      } finally {
        EntityUtils.consume(httpResponse.getEntity());
      }

    }


    ChangesetStore changesetStore = new ChangesetStore();
    changesetStore.setBaseURL("http://download.geofabrik.de/europe/malta-updates");
    changesetStore.setHttpClient(httpClient);

    for (ChangesetStoreState state : changesetStore.findChangesetStatesSince(dateDaysAgo.getTime())) {
      Reader changeset = changesetStore.getChangeset(state.getSequenceNumber());
      try {
        parser.parse(changeset);
      } finally {
        changeset.close();
      }
    }

    System.currentTimeMillis();

  }

}
