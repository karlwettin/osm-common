package se.kodapan.osm.city;

import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * @author kalle
 * @since 2015-01-12 07:16
 */
public class CachedInstanceFactory extends InstanceFactory {

  private InstanceFactory cached;

  private File path;

  public CachedInstanceFactory(File path, Classifier classifier, InstanceFactory cached) {
    super(classifier);
    this.cached = cached;
    this.path = path;
    if (!path.exists() && !path.mkdirs()) {
      throw new RuntimeException("Could not mkdirs " + path.getAbsolutePath());
    }
  }

  public File getPath() {
    return path;
  }

  public void setPath(File path) {
    this.path = path;
  }

  @Override
  public BufferedImage loadImage(double south, double west, double north, double east) throws Exception {

    File file = new File(path, new StringBuilder()
        .append("south=").append(String.valueOf(south))
        .append("&west=").append(String.valueOf(west))
        .append("&north=").append(String.valueOf(north))
        .append("&east=").append(String.valueOf(east))
        .append(".png")
        .toString());

    if (file.exists()) {
      return ImageIO.read(file);
    }

    BufferedImage image = cached.loadImage(south, west, north, east);
    ImageIO.write(image, "png", file);
    return image;

  }
}
