/*
 * The Alluxio Open Foundation licenses this work under the Apache License, version 2.0
 * (the "License"). You may not use this work except in compliance with the License, which is
 * available at www.apache.org/licenses/LICENSE-2.0
 *
 * This software is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied, as more fully set forth in the License.
 *
 * See the NOTICE file distributed with this work for information regarding copyright ownership.
 */

package alluxio.worker.block;

import alluxio.conf.PropertyKey;
import alluxio.conf.ServerConfiguration;
import alluxio.metrics.MetricsConfig;
import alluxio.util.IdUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import javax.annotation.concurrent.NotThreadSafe;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * persistence worker info to a file.
 */
@NotThreadSafe
public class DefaultBlockWorkerDB implements BlockWorkerDB {
  private static final Logger LOG = LoggerFactory.getLogger(MetricsConfig.class);
  /*Use Properties to persistence info */
  final Properties mProperties;
  /*Properties file path */
  final String mPath;
  boolean mIsInit;
  final String mClusterIdKey = "clusterId";

  DefaultBlockWorkerDB(String path) {
    mProperties = new Properties();
    mPath = path;
    mIsInit = false;
  }

  DefaultBlockWorkerDB() {
    this(ServerConfiguration.get(PropertyKey.WORKER_PERSISTENCE_CLUSTER_ID_FILE));
  }

  @Override
  public String getClusterId() {
    String mClusterId = get(mClusterIdKey);
    return (mClusterId.isEmpty()) ? IdUtils.INVALID_CLUSTER_ID : mClusterId;
  }

  @Override
  public void setClusterId(String clusterId) throws IOException {
    set(mClusterIdKey, clusterId);
  }

  /**
   * Searches for the property with the specified key in file.
   * If the property file does not exist or key is not found in this property list
   * will return empty string
   * @param key the property key
   * @return the value in this property list with the specified key value
   */
  String get(String key) {
    Preconditions.checkNotNull(key, "get");

    String value = "";
    if (Files.exists(Paths.get(mPath))) {
      try (InputStream is = new FileInputStream(mPath)) {
        mProperties.load(is);
        value = mProperties.getProperty(key);
      } catch (Exception e) {
        LOG.error("Error loading metrics configuration file.", e);
      }
    }
    return (value == null) ? "" : value;
  }

  /**
   * persist a key-value to property file. If the file does not exist, it will be created.
   * @param key  the key to be persist into this file
   * @param value  the value corresponding to key
   * @throws IOException  I/O error if create or write file failed
   */
  void set(String key, String value) throws IOException {
    Preconditions.checkNotNull(key, "set");
    Preconditions.checkNotNull(value, "set");
    init();

    try (OutputStream os = new FileOutputStream(mPath)) {
      mProperties.setProperty(key, value);
      mProperties.store(os, "generated by worker process, don't edit it");
    } catch (Exception e) {
      LOG.error("Error loading metrics configuration file.", e);
    }
  }

  private void init() {
    if (mIsInit) {
      return;
    }

    if (!Files.exists(Paths.get(mPath))) {
      try {
        new File(mPath).getParentFile().mkdirs();
        Files.createFile(Paths.get(mPath));
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    mIsInit = true;
  }

  @Override
  public void resetState() throws IOException {
    if (!Files.exists(Paths.get(mPath))) {
      return;
    }
    // clear mProperties in memory
    mProperties.clear();
    // clear the persistence file
    try (OutputStream os = new FileOutputStream(mPath)) {
      os.write("".getBytes(StandardCharsets.UTF_8));
    } catch (Exception e) {
      LOG.error("Error loading metrics configuration file.", e);
    }
  }
}
