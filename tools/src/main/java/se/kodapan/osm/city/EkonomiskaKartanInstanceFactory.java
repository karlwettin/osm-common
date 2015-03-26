package se.kodapan.osm.city;

import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.SMO;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Map;

/**
 * @author kalle
 * @since 2015-01-12 04:47
 */
public class EkonomiskaKartanInstanceFactory extends InstanceFactory {

  public static void main(String[] args) throws Exception {
    WekaClassifier classifier = new WekaClassifier();
    classifier.setTrainingDataArffFile(new File("trainingData.arff"));
    classifier.setClassifier(new NaiveBayes());

    classifier.setPalette(new Palette()
        .addColor("#6A8B5D")
        .addColor("#8EB27A")
        .addColor("#A2BE97")
        .addColor("#BBC228")
        .addColor("#4F5D40")
        .addColor("#9EB23C")
        .addColor("#778E43"));

    classifier.setInstanceFactory(new CachedInstanceFactory(new File("data/ekonomiska-kartan"),
        classifier, new EkonomiskaKartanInstanceFactory(classifier)));


    Grid grid = new Grid(
        // jönköping huskvarna
        57.747, 14.099, 57.826, 14.324


        // vellinge
//        55.462, 12.99, 55.485, 13.045


        // malmö
//        55.5325167053891, 12.887306213378906, 55.63225992920628, 13.100166320800781

        , 5000, 5000, 250
    );

    for (int x = 0; x < grid.getColumns().length; x++) {
      for (int y = 0; y < grid.getColumns()[x].length; y++) {
        Cell cell = grid.getColumns()[x][y];
        try {
          cell.setInstance(classifier.getInstanceFactory().newInstance(
              grid.getCellSouth(x, y),
              grid.getCellWest(x, y),
              grid.getCellNorth(x, y),
              grid.getCellEast(x, y)
          ));
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }

    grid.writeOsmXML(new File("grid.osm.xml"));

    Map<Instance, String> trainingData = grid.loadTrainingData(classifier.getInstanceFactory());
    for (Map.Entry<Instance, String> instance : trainingData.entrySet()) {
      classifier.train(instance.getKey(), instance.getValue());
    }
    classifier.build();

    for (int x = 0; x < grid.getColumns().length; x++) {
      for (int y = 0; y < grid.getColumns()[x].length; y++) {
        Cell cell = grid.getColumns()[x][y];
        if (cell.getInstance() != null) {
          cell.setClassification(classifier.classify(cell.getInstance()));
        }
      }
    }

    grid.writeOsmXML(new File("classified.osm.xml"));

  }

  public EkonomiskaKartanInstanceFactory(Classifier classifier) {
    super(classifier);
  }

  private int width = 256;
  private int height = 256;


  @Override
  public BufferedImage loadImage(double south, double west, double north, double east) throws Exception {

    DecimalFormat df = new DecimalFormat("#.#######");

    String url = new StringBuilder("http://www.gavert.net/cgi-bin/mapserv")
        .append("?map=ek.map")
        .append("&FORMAT=image/png&VERSION=1.1.1")
        .append("&SERVICE=WMS")
        .append("&REQUEST=GetMap&LAYERS=virtualraster")
        .append("&STYLES=")
        .append("&SRS=EPSG:4326")
        .append("&WIDTH=").append(String.valueOf(width))
        .append("&HEIGHT=").append(String.valueOf(height))
        .append("&BBOX=").append(df.format(west)).append(",").append(df.format(south)).append(",").append(df.format(east)).append(",").append(df.format(north))
        .toString();

    System.out.println(url);

    return ImageIO.read(new URL(url));

  }

}
