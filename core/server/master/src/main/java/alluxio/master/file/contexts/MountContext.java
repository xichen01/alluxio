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

package alluxio.master.file.contexts;

import alluxio.conf.ServerConfiguration;
import alluxio.grpc.MountPOptions;
import alluxio.recorder.Recorder;
import alluxio.util.FileSystemOptions;

import com.google.common.base.MoreObjects;

import java.util.Collections;
import java.util.List;

/**
 * Used to merge and wrap {@link MountPOptions}.
 */
public class MountContext extends OperationContext<MountPOptions.Builder, MountContext> {
  private Recorder mRecorder;

  // Used to record mount execution process

  /**
   * Creates context with given option data.
   *
   * @param optionsBuilder options builder
   */
  private MountContext(MountPOptions.Builder optionsBuilder) {
    super(optionsBuilder);
    mRecorder = Recorder.create();
  }

  /**
   * @param optionsBuilder Builder for proto {@link MountPOptions}
   * @return the instance of {@link MountContext} with the given options
   */
  public static MountContext create(MountPOptions.Builder optionsBuilder) {
    return new MountContext(optionsBuilder);
  }

  /**
   * Merges and embeds the given {@link MountPOptions} with the corresponding master options.
   *
   * @param optionsBuilder Builder for proto {@link MountPOptions} to embed
   * @return the instance of {@link MountContext} with default values for master
   */
  public static MountContext mergeFrom(MountPOptions.Builder optionsBuilder) {
    MountPOptions masterOptions = FileSystemOptions.mountDefaults(ServerConfiguration.global());
    MountPOptions.Builder mergedOptionsBuilder =
        masterOptions.toBuilder().mergeFrom(optionsBuilder.build());
    return create(mergedOptionsBuilder);
  }

  /**
   * @return the instance of {@link MountContext} with default values for master
   */
  public static MountContext defaults() {
    return create(FileSystemOptions.mountDefaults(ServerConfiguration.global()).toBuilder());
  }

  /**
   * Record a message.
   * @param message options builder
   */
  public void record(String message) {
    mRecorder.record(message);
  }

  /**
   * get a Recorder.
   * @return  Recorder
   */
  public Recorder getRecord() {
    return mRecorder;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("ProtoOptions", getOptions().build())
        .toString();
  }
}
