package se.kodapan.osm.city;

/**
* @author kalle
* @since 2015-01-12 03:52
*/
public class Cell {

  private int x;
  private int y;

  private String classification;

  public Cell(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public Cell(int x, int y, String classification) {
    this.x = x;
    this.y = y;
    this.classification = classification;
  }

  private Instance instance = new Instance();

  public String getClassification() {
    return classification;
  }

  public void setClassification(String classification) {
    this.classification = classification;
  }

  public Instance getInstance() {
    return instance;
  }

  public void setInstance(Instance instance) {
    this.instance = instance;
  }

  public int getX() {
    return x;
  }

  public void setX(int x) {
    this.x = x;
  }

  public int getY() {
    return y;
  }

  public void setY(int y) {
    this.y = y;
  }

}
