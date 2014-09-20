package se.kodapan.osm.services.changesetstore;

import junit.framework.TestCase;
import se.kodapan.osm.OsmCommonTest;

import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

/**
 * @author kalle
 * @since 2013-05-03 20:27
 */
public class TestChangesetStore extends OsmCommonTest {

  public void testGetFirstChangesetStateSince() throws Exception {
    ChangesetStore store = new ChangesetStore();
    setUserAgent(store);
    store.open();
    store.setBaseURL("http://download.geofabrik.de/europe/sweden-updates");

    ChangesetStoreState mostRecentChangesetState = store.getMostRecentChangesetState();
    if (mostRecentChangesetState.getSequenceNumber() < 10) {
      fail("Not enough changesets on server to execute test!");
    }

    ChangesetStoreState changesetStateApriori = store.getChangesetState(mostRecentChangesetState.getSequenceNumber() - 10);

    if (changesetStateApriori == null) {
      fail("Apriori changeset does not exist on server.");
    }

    ChangesetStoreState changesetState = store.findFirstChangesetStateSince(changesetStateApriori.getTimestamp());

    assertEquals(changesetStateApriori.getSequenceNumber().intValue() + 1, changesetState.getSequenceNumber().intValue());

    System.out.println();


  }

  public void testGetChangesetStatesSince() throws Exception {
    ChangesetStore store = new ChangesetStore();
    setUserAgent(store);
    store.open();
    store.setBaseURL("http://download.geofabrik.de/europe/sweden-updates");

    ChangesetStoreState mostRecentChangesetState = store.getMostRecentChangesetState();
    if (mostRecentChangesetState.getSequenceNumber() < 10) {
      fail("Not enough changesets on server to execute test!");
    }

    ChangesetStoreState changesetStateApriori = store.getChangesetState(mostRecentChangesetState.getSequenceNumber() - 10);

    if (changesetStateApriori == null) {
      fail("Apriori changeset does not exist on server.");
    }

    List<ChangesetStoreState> changesetStates = store.findChangesetStatesSince(changesetStateApriori.getTimestamp());

    assertEquals(mostRecentChangesetState.getSequenceNumber(), changesetStates.get(changesetStates.size() - 1).getSequenceNumber());
    assertEquals("Expecting number of changesets to be sequence number of last state - aprioi state, there might however be files missing at the server!", mostRecentChangesetState.getSequenceNumber() - changesetStateApriori.getSequenceNumber(), changesetStates.size());
    assertEquals(changesetStateApriori.getSequenceNumber().intValue() + 1, changesetStates.get(0).getSequenceNumber().intValue());

    System.out.println();
  }

  public void testGetMostRecentChangesetState() throws Exception {
    ChangesetStore store = new ChangesetStore();
    store.open();
    setUserAgent(store);
    store.setBaseURL("http://download.geofabrik.de/europe/sweden-updates");
    ChangesetStoreState state = store.getMostRecentChangesetState();
    Reader changesetReader = store.getChangeset(state.getSequenceNumber());
    try {
      System.currentTimeMillis();
    } finally {
      changesetReader.close();
    }
  }

  public void testParseState() throws Exception {
    ChangesetStore store = new ChangesetStore();
    store.open();
    setUserAgent(store);
    StringBuilder stateBuilder = new StringBuilder();
    stateBuilder.append("#this is a comment\n");
    stateBuilder.append("sequenceNumber=1\n");
    stateBuilder.append("timestamp=2006-12-31T20\\:57\\:28Z\n");
    stateBuilder.append("txnMaxQueried=2\n");
    stateBuilder.append("txnMax=3\n");
    stateBuilder.append("txnActiveList=4,5\n");
    stateBuilder.append("txnReadyList=6,7\n");
    stateBuilder.append("unknown property=generates a warning. ignore me.\n");
    stateBuilder.append("bad property line that generates a warning. ignore me.\n");
    stateBuilder.append("#this is also a comment\n");
    ChangesetStoreState state = store.parseChangesetState(new StringReader(stateBuilder.toString()));
    assertEquals(new Integer(1), state.getSequenceNumber());
    assertEquals(new Long(1167595048000l), state.getTimestamp());
    assertEquals(new Long(2), state.getTxnMaxQueried());
    assertEquals(new Long(3), state.getTxnMax());
    assertEquals(Arrays.asList(new Long[]{4l, 5l}), state.getTxnActiveList());
    assertEquals(Arrays.asList(new Long[]{6l, 7l}), state.getTxnReadyList());
  }

  public void testGetChangesetURL() throws Exception {

    ChangesetStore store = new ChangesetStore();
    store.open();
    setUserAgent(store);
    store.setBaseURL("http://foo/");
    assertEquals(new URL("http://foo/000/000/000.osc.gz"), store.getChangesetURL(0));
    assertEquals(new URL("http://foo/000/000/100.osc.gz"), store.getChangesetURL(100));
    assertEquals(new URL("http://foo/000/001/000.osc.gz"), store.getChangesetURL(1000));
    assertEquals(new URL("http://foo/001/000/000.osc.gz"), store.getChangesetURL(1000000));

  }

  public void testGetStateURL() throws Exception {

    ChangesetStore store = new ChangesetStore();
    store.open();
    setUserAgent(store);
    store.setBaseURL("http://foo/");
    assertEquals(new URL("http://foo/000/000/000.state.txt"), store.getChangesetStateURL(0));
    assertEquals(new URL("http://foo/000/000/100.state.txt"), store.getChangesetStateURL(100));
    assertEquals(new URL("http://foo/000/001/000.state.txt"), store.getChangesetStateURL(1000));
    assertEquals(new URL("http://foo/001/000/000.state.txt"), store.getChangesetStateURL(1000000));

  }

  public void testSetBaseURL() throws Exception {

    ChangesetStore store = new ChangesetStore();
    store.open();
    setUserAgent(store);
    store.setBaseURL("http://foo/");
    assertEquals(new URL("http://foo/"), store.getBaseURL());

    store.setBaseURL("http://foo");
    assertEquals(new URL("http://foo/"), store.getBaseURL());

    try {
      store.setBaseURL("http://foo/bar#");
      fail();
    } catch (IllegalArgumentException iae) {
      // all good
    }

    try {
      store.setBaseURL("http://foo/bar#anchor");
      fail();
    } catch (IllegalArgumentException iae) {
      // all good
    }

    try {
      store.setBaseURL("http://foo/bar?");
      fail();
    } catch (IllegalArgumentException iae) {
      // all good
    }

    try {
      store.setBaseURL("http://foo/bar?query");
      fail();
    } catch (IllegalArgumentException iae) {
      // all good
    }

  }

}
