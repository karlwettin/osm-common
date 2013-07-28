package se.kodapan.osm.data.planet.changesetstore;

import java.util.List;

/**
 *
 *
 * @author kalle
 * @since 2013-05-03 20:22
 */
public class ChangesetStoreState {

  /**
   * The sequence number of the change/state file.
   */
  private Integer sequenceNumber;

  /**
   * The timestamp when the diff was generated.
   */
  private Long timestamp;


  /**
   * The maximum transaction ID which is included in the diff. (NOTE: Doesn't seem to be used for the hourly diffs)
   */
  private Long txnMaxQueried;
  /**
   * The maximum transaction ID at the time the diff was generated, usually the same as txnMaxQueried.
   */
  private Long txnMax;
  /**
   * The list of transaction IDs between this state and the previous state which have not been committed yet. (NOTE: Doesn't seem to be used for the hourly diffs).
   */
  private List<Long> txnActiveList;
  /**
   * Unknown - seems to be unused.
   */
  private List<Long> txnReadyList;


  @Override
  public String toString() {
    return "ChangesetStoreState{" +
        "sequenceNumber=" + sequenceNumber +
        ", timestamp=" + timestamp +
        ", txnMaxQueried=" + txnMaxQueried +
        ", txnMax=" + txnMax +
        ", txnActiveList=" + txnActiveList +
        ", txnReadyList=" + txnReadyList +
        '}';
  }

  public Integer getSequenceNumber() {
    return sequenceNumber;
  }

  public void setSequenceNumber(Integer sequenceNumber) {
    this.sequenceNumber = sequenceNumber;
  }

  public Long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Long timestamp) {
    this.timestamp = timestamp;
  }

  public Long getTxnMaxQueried() {
    return txnMaxQueried;
  }

  public void setTxnMaxQueried(Long txnMaxQueried) {
    this.txnMaxQueried = txnMaxQueried;
  }

  public Long getTxnMax() {
    return txnMax;
  }

  public void setTxnMax(Long txnMax) {
    this.txnMax = txnMax;
  }

  public List<Long> getTxnActiveList() {
    return txnActiveList;
  }

  public void setTxnActiveList(List<Long> txnActiveList) {
    this.txnActiveList = txnActiveList;
  }

  public List<Long> getTxnReadyList() {
    return txnReadyList;
  }

  public void setTxnReadyList(List<Long> txnReadyList) {
    this.txnReadyList = txnReadyList;
  }
}
