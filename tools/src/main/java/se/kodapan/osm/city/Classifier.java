package se.kodapan.osm.city;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author kalle
 * @since 2015-01-12 04:47
 */
public abstract class Classifier {

  private Palette palette;
  private InstanceFactory instanceFactory;

  private Map<Instance, String> trainingData = new HashMap<Instance, String>(1024);

  public void train(double south, double west, double north, double east, String classification) throws Exception {
    Instance instance = getInstanceFactory().newInstance(south, west, north, east);
    train(instance, classification);
  }

  public void train(Instance instance, String classification) throws Exception {
    trainingData.put(instance, classification);
  }

  public abstract void build() throws Exception;

  public String classify(double south, double west, double north, double east) throws Exception {
    return classify(getInstanceFactory().newInstance(south, west, north, east));
  }

  public abstract String classify(Instance instance) throws Exception;


  public InstanceFactory getInstanceFactory() {
    return instanceFactory;
  }

  /** expert */
  public void setInstanceFactory(InstanceFactory instanceFactory) {
    this.instanceFactory = instanceFactory;
  }

  public Palette getPalette() {
    return palette;
  }

  /** expert */
  public void setPalette(Palette palette) {
    this.palette = palette;
  }

  public Map<Instance, String> getTrainingData() {
    return trainingData;
  }

  public void setTrainingData(Map<Instance, String> trainingData) {
    this.trainingData = trainingData;
  }


}
