package se.kodapan.osm.city;

import se.kodapan.osm.city.*;
import weka.core.Instances;

import java.io.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author kalle
 * @since 2015-01-12 05:31
 */
public class WekaClassifier extends Classifier {

  private weka.classifiers.Classifier classifier;
  private weka.core.Instances wekaTrainingData;

  private File trainingDataArffFile;

  @Override
  public void build() throws Exception {
    File file = trainingDataArffFile != null ? trainingDataArffFile : File.createTempFile("training_data", "arff");
    try {
      Writer arff = new OutputStreamWriter(new FileOutputStream(file), "UTF8");
      arff.append("@Relation color_distributions\n");
      for (Color color : getPalette().getColors()) {
        String name = String.valueOf((char) ('a' + color.getAttributeIndex()));
        arff.append("@Attribute ").append(String.valueOf((char) ('a' + color.getAttributeIndex()))).append(" numeric\n");
      }
      arff.append("@Attribute class {");
      for (Iterator<String> iterator = new HashSet<String>(getTrainingData().values()).iterator(); iterator.hasNext(); ) {
        String classification = iterator.next();
        arff.append("\"").append(classification).append("\"");
        if (iterator.hasNext()) {
          arff.append(", ");
        }
      }
      arff.append("}\n");
      arff.append("@Data\n");

      for (Map.Entry<Instance, String> instance : getTrainingData().entrySet()) {
        for (double value : instance.getKey().getHistogramPercent()) {
          arff.append(String.valueOf(value)).append(",");
        }
        arff.append("\"").append(instance.getValue()).append("\"");
        arff.append("\n");
      }

      arff.close();

      wekaTrainingData = new Instances(new InputStreamReader(new FileInputStream(file), "UTF8"));
      wekaTrainingData.setClass(wekaTrainingData.attribute("class"));
      classifier.buildClassifier(wekaTrainingData);

    } finally {
      //file.delete();
    }
  }

  @Override
  public String classify(Instance instance) throws Exception {
    weka.core.Instance wekaInstance = new weka.core.Instance(wekaTrainingData.numAttributes());
    wekaInstance.setDataset(wekaTrainingData);

    double[] histogramPercent = instance.getHistogramPercent();
    for (int i = 0; i < histogramPercent.length; i++) {
      wekaInstance.setValue(i, histogramPercent[i]);
    }
    wekaInstance.setMissing(wekaTrainingData.attribute("class"));


    double wekaClassification = classifier.classifyInstance(wekaInstance);
    String classification = wekaTrainingData.attribute("class").value((int)wekaClassification);
    return classification;
  }

  public weka.classifiers.Classifier getClassifier() {
    return classifier;
  }

  public void setClassifier(weka.classifiers.Classifier classifier) {
    this.classifier = classifier;
  }

  public File getTrainingDataArffFile() {
    return trainingDataArffFile;
  }

  public void setTrainingDataArffFile(File trainingDataArffFile) {
    this.trainingDataArffFile = trainingDataArffFile;
  }
}
