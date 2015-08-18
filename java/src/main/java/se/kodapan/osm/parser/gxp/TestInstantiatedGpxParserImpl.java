package se.kodapan.osm.parser.gxp;

import java.io.FileReader;

/**
 * @author kalle
 * @since 2015-08-11 19:42
 */
public class TestInstantiatedGpxParserImpl {

  public static void main(String[] args) throws Exception {

    Gpx gpx = new InstantiatedGpxParserImpl().parse(new FileReader("/Users/kalle/projekt/kodapan/gitlab.kodapan.se/fm-gps/java/rtl_power/gpx/11_aug_2015_16;15;32_2015-08-11_16-15-32.gpx"));
    System.currentTimeMillis();

  }

}
