package alluxio.worker.block;

import alluxio.util.IdUtils;
import alluxio.util.io.PathUtils;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class BlockWorkerDBTest {
  /**
   * Sets up all dependencies before a test runs.
   */
  DefaultBlockWorkerDB mBlockWorkerDB;
  final String mNotExistFile = "mNotExistFile";
  final String mKey1 = "key1";
  final String mValue1 = "value1";

  /**
   * Rule to create a new temporary folder during each test.
   */
  @Rule
  public TemporaryFolder mTestFolder = new TemporaryFolder();

  void createANotExistFile() {
    mBlockWorkerDB =
        new DefaultBlockWorkerDB(
            PathUtils.concatPath(mTestFolder.getRoot().getAbsolutePath(), mNotExistFile));
    reset();
  }

  void createDefault() {
    mBlockWorkerDB = new DefaultBlockWorkerDB();
    reset();
  }

  /**
   * Resets the worker persistence info to original state. not to do if persistence file not exist
   */
  void reset() {
    if (mBlockWorkerDB == null) {
      return;
    }
    try {
      mBlockWorkerDB.resetState();
    } catch (IOException e) {
      e.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void testGetINVALIDClusterIdFromDB() {
    createDefault();
    Assert.assertEquals(IdUtils.INVALID_CLUSTER_ID, mBlockWorkerDB.getClusterId());
  }

  @Test
  public void testSetAndGetClusterIdFromDB() throws IOException {
    createDefault();
    String clusterId = IdUtils.createFileSystemContextId();
    mBlockWorkerDB.setClusterId(clusterId);
    Assert.assertEquals(clusterId, mBlockWorkerDB.getClusterId());
  }

  @Test
  public void testSetAndGetToExistFile() throws IOException {
    createDefault();
    mBlockWorkerDB.set(mKey1, mValue1);
    Assert.assertEquals(mValue1, mBlockWorkerDB.get(mKey1));
  }

  @Test
  public void testSetAndGetToNotExistFile() throws IOException {
    createANotExistFile();
    // When writing info to a file that does not exist, the file will be created
    mBlockWorkerDB.set(mKey1, mValue1);
    Assert.assertEquals(mValue1, mBlockWorkerDB.get(mKey1));
  }

  @Test
  public void testGetToNotExistFile() throws IOException {
    createANotExistFile();
    // The get will return an empty string rather than a null pointer if the key does not exist
    Assert.assertEquals("", mBlockWorkerDB.get(mKey1));
  }

  @Test
  public void testResetStateExistFile() throws IOException {
    createDefault();
    mBlockWorkerDB.set(mKey1, mValue1);
    mBlockWorkerDB.resetState();
    // normal resetState, all info will be clear
    Assert.assertEquals("", mBlockWorkerDB.get(mKey1));
  }

  @Test
  public void testResetStateNotExistFile() throws IOException {
    mBlockWorkerDB =
        new DefaultBlockWorkerDB(
            PathUtils.concatPath(mTestFolder.getRoot().getAbsolutePath(), mNotExistFile));
    // nothing to do if reset a not exist file
    mBlockWorkerDB.resetState();
  }
}