package se.kodapan.osm.city;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author kalle
 * @since 2015-01-12 05:19
 */
public abstract class InstanceFactory {

  private Classifier classifier;

  protected InstanceFactory(Classifier classifier) {
    this.classifier = classifier;
  }

  public abstract BufferedImage loadImage(double south, double west, double north, double east) throws Exception;

  public Instance newInstance(double south, double west, double north, double east) throws Exception {
    BufferedImage image = loadImage(south, west, north, east);

    Instance instance = new Instance();
    instance.setNorth(north);
    instance.setEast(east);
    instance.setWest(west);
    instance.setSouth(south);
    byte[][] data = new byte[image.getWidth()][];
    for (int x = 0; x < image.getWidth(); x++) {
      data[x] = new byte[image.getHeight()];
      for (int y = 0; y < image.getHeight(); y++) {
        data[x][y] = getClassifier().getPalette().getColor(image.getRGB(x, y)).getAttributeIndex();
      }
    }
    instance.setData(data);

    Map<Byte, AtomicInteger> occurances = new HashMap<Byte, AtomicInteger>(256);

    for (int x = 0; x < data.length; x++) {
      for (int y = 0; y < data[x].length; y++) {
        AtomicInteger value = occurances.get(data[x][y]);
        if (value == null) {
          value = new AtomicInteger();
          occurances.put(data[x][y], value);
        }
        value.incrementAndGet();
      }
    }

    double totalDataSize = data.length * data[0].length;

    double[] histogramPercent = new double[getClassifier().getPalette().getColors().size()];
    for (Map.Entry<Byte, AtomicInteger> entry : occurances.entrySet()) {
      histogramPercent[entry.getKey()] = entry.getValue().get() / totalDataSize;
    }
    instance.setHistogramPercent(histogramPercent);

    return instance;

  }


  public Classifier getClassifier() {
    return classifier;
  }
}
