/*
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.gcloud.storage;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.gcloud.storage.Blob.BlobSourceOption.toGetOptions;
import static com.google.gcloud.storage.Blob.BlobSourceOption.toSourceOptions;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gcloud.ReadChannel;
import com.google.gcloud.WriteChannel;
import com.google.gcloud.spi.StorageRpc;
import com.google.gcloud.storage.Storage.BlobTargetOption;
import com.google.gcloud.storage.Storage.BlobWriteOption;
import com.google.gcloud.storage.Storage.CopyRequest;
import com.google.gcloud.storage.Storage.SignUrlOption;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * A Google cloud storage object.
 *
 * <p>Objects of this class are immutable. Operations that modify the blob like {@link #update} and
 * {@link #copyTo} return a new object. To get a {@code Blob} object with the most recent
 * information use {@link #reload}.
 * </p>
 */
public final class Blob {

  private final Storage storage;
  private final BlobInfo info;

  /**
   * Class for specifying blob source options when {@code Blob} methods are used.
   */
  public static class BlobSourceOption extends Option {

    private static final long serialVersionUID = 214616862061934846L;

    private BlobSourceOption(StorageRpc.Option rpcOption) {
      super(rpcOption, null);
    }

    private Storage.BlobSourceOption toSourceOptions(BlobInfo blobInfo) {
      switch (rpcOption()) {
        case IF_GENERATION_MATCH:
          return Storage.BlobSourceOption.generationMatch(blobInfo.generation());
        case IF_GENERATION_NOT_MATCH:
          return Storage.BlobSourceOption.generationNotMatch(blobInfo.generation());
        case IF_METAGENERATION_MATCH:
          return Storage.BlobSourceOption.metagenerationMatch(blobInfo.metageneration());
        case IF_METAGENERATION_NOT_MATCH:
          return Storage.BlobSourceOption.metagenerationNotMatch(blobInfo.metageneration());
        default:
          throw new AssertionError("Unexpected enum value");
      }
    }

    private Storage.BlobGetOption toGetOption(BlobInfo blobInfo) {
      switch (rpcOption()) {
        case IF_GENERATION_MATCH:
          return Storage.BlobGetOption.generationMatch(blobInfo.generation());
        case IF_GENERATION_NOT_MATCH:
          return Storage.BlobGetOption.generationNotMatch(blobInfo.generation());
        case IF_METAGENERATION_MATCH:
          return Storage.BlobGetOption.metagenerationMatch(blobInfo.metageneration());
        case IF_METAGENERATION_NOT_MATCH:
          return Storage.BlobGetOption.metagenerationNotMatch(blobInfo.metageneration());
        default:
          throw new AssertionError("Unexpected enum value");
      }
    }

    /**
     * Returns an option for blob's generation match. If this option is used the request will fail
     * if generation does not match.
     */
    public static BlobSourceOption generationMatch() {
      return new BlobSourceOption(StorageRpc.Option.IF_GENERATION_MATCH);
    }

    /**
     * Returns an option for blob's generation mismatch. If this option is used the request will
     * fail if generation matches.
     */
    public static BlobSourceOption generationNotMatch() {
      return new BlobSourceOption(StorageRpc.Option.IF_GENERATION_NOT_MATCH);
    }

    /**
     * Returns an option for blob's metageneration match. If this option is used the request will
     * fail if metageneration does not match.
     */
    public static BlobSourceOption metagenerationMatch() {
      return new BlobSourceOption(StorageRpc.Option.IF_METAGENERATION_MATCH);
    }

    /**
     * Returns an option for blob's metageneration mismatch. If this option is used the request will
     * fail if metageneration matches.
     */
    public static BlobSourceOption metagenerationNotMatch() {
      return new BlobSourceOption(StorageRpc.Option.IF_METAGENERATION_NOT_MATCH);
    }

    static Storage.BlobSourceOption[] toSourceOptions(BlobInfo blobInfo,
        BlobSourceOption... options) {
      Storage.BlobSourceOption[] convertedOptions = new Storage.BlobSourceOption[options.length];
      int index = 0;
      for (BlobSourceOption option : options) {
        convertedOptions[index++] = option.toSourceOptions(blobInfo);
      }
      return convertedOptions;
    }

    static Storage.BlobGetOption[] toGetOptions(BlobInfo blobInfo, BlobSourceOption... options) {
      Storage.BlobGetOption[] convertedOptions = new Storage.BlobGetOption[options.length];
      int index = 0;
      for (BlobSourceOption option : options) {
        convertedOptions[index++] = option.toGetOption(blobInfo);
      }
      return convertedOptions;
    }
  }

  /**
   * Constructs a {@code Blob} object for the provided {@code BlobInfo}. The storage service is used
   * to issue requests.
   *
   * @param storage the storage service used for issuing requests
   * @param info blob's info
   */
  public Blob(Storage storage, BlobInfo info) {
    this.storage = checkNotNull(storage);
    this.info = checkNotNull(info);
  }

  /**
   * Creates a {@code Blob} object for the provided bucket and blob names. Performs an RPC call to
   * get the latest blob information. Returns {@code null} if the blob does not exist.
   *
   * @param storage the storage service used for issuing requests
   * @param bucket bucket's name
   * @param options blob get options
   * @param blob blob's name
   * @return the {@code Blob} object or {@code null} if not found
   * @throws StorageException upon failure
   */
  public static Blob get(Storage storage, String bucket, String blob,
      Storage.BlobGetOption... options) {
    return get(storage, BlobId.of(bucket, blob), options);
  }

  /**
   * Creates a {@code Blob} object for the provided {@code blobId}. Performs an RPC call to get the
   * latest blob information. Returns {@code null} if the blob does not exist.
   *
   * @param storage the storage service used for issuing requests
   * @param blobId blob's identifier
   * @param options blob get options
   * @return the {@code Blob} object or {@code null} if not found
   * @throws StorageException upon failure
   */
  public static Blob get(Storage storage, BlobId blobId, Storage.BlobGetOption... options) {
    BlobInfo info = storage.get(blobId, options);
    return info != null ? new Blob(storage, info) : null;
  }

  /**
   * Returns the blob's information.
   */
  public BlobInfo info() {
    return info;
  }

  /**
   * Returns the blob's id.
   */
  public BlobId id() {
    return info.blobId();
  }

  /**
   * Checks if this blob exists.
   *
   * @param options blob read options
   * @return true if this blob exists, false otherwise
   * @throws StorageException upon failure
   */
  public boolean exists(BlobSourceOption... options) {
    int length = options.length;
    Storage.BlobGetOption[] getOptions = Arrays.copyOf(toGetOptions(info, options), length + 1);
    getOptions[length] = Storage.BlobGetOption.fields();
    return storage.get(info.blobId(), getOptions) != null;
  }

  /**
   * Returns this blob's content.
   *
   * @param options blob read options
   * @throws StorageException upon failure
   */
  public byte[] content(Storage.BlobSourceOption... options) {
    return storage.readAllBytes(info.blobId(), options);
  }

  /**
   * Fetches current blob's latest information. Returns {@code null} if the blob does not exist.
   *
   * @param options blob read options
   * @return a {@code Blob} object with latest information or {@code null} if not found
   * @throws StorageException upon failure
   */
  public Blob reload(BlobSourceOption... options) {
    return Blob.get(storage, info.blobId(), toGetOptions(info, options));
  }

  /**
   * Updates the blob's information. Bucket or blob's name cannot be changed by this method. If you
   * want to rename the blob or move it to a different bucket use the {@link #copyTo} and
   * {@link #delete} operations. A new {@code Blob} object is returned. By default no checks are
   * made on the metadata generation of the current blob. If you want to update the information only
   * if the current blob metadata are at their latest version use the {@code metagenerationMatch}
   * option: {@code blob.update(newInfo, BlobTargetOption.metagenerationMatch())}.
   *
   * <p>Original metadata are merged with metadata in the provided {@code blobInfo}. To replace
   * metadata instead you first have to unset them. Unsetting metadata can be done by setting the
   * provided {@code blobInfo}'s metadata to {@code null}.
   * </p>
   *
   * <p>Example usage of replacing blob's metadata:
   * <pre>    {@code blob.update(blob.info().toBuilder().metadata(null).build());}
   *    {@code blob.update(blob.info().toBuilder().metadata(newMetadata).build());}
   * </pre>
   *
   * @param blobInfo new blob's information. Bucket and blob names must match the current ones
   * @param options update options
   * @return a {@code Blob} object with updated information
   * @throws StorageException upon failure
   */
  public Blob update(BlobInfo blobInfo, BlobTargetOption... options) {
    checkArgument(Objects.equals(blobInfo.bucket(), info.bucket()), "Bucket name must match");
    checkArgument(Objects.equals(blobInfo.name(), info.name()), "Blob name must match");
    return new Blob(storage, storage.update(blobInfo, options));
  }

  /**
   * Deletes this blob.
   *
   * @param options blob delete options
   * @return {@code true} if blob was deleted, {@code false} if it was not found
   * @throws StorageException upon failure
   */
  public boolean delete(BlobSourceOption... options) {
    return storage.delete(info.blobId(), toSourceOptions(info, options));
  }

  /**
   * Sends a copy request for the current blob to the target blob. Possibly also some of the
   * metadata are copied (e.g. content-type).
   *
   * @param targetBlob target blob's id
   * @param options source blob options
   * @return a {@link CopyWriter} object that can be used to get information on the newly created
   *     blob or to complete the copy if more than one RPC request is needed
   * @throws StorageException upon failure
   */
  public CopyWriter copyTo(BlobId targetBlob, BlobSourceOption... options) {
    CopyRequest copyRequest = CopyRequest.builder().source(info.bucket(), info.name())
        .sourceOptions(toSourceOptions(info, options)).target(targetBlob).build();
    return storage.copy(copyRequest);
  }

  /**
   * Sends a copy request for the current blob to the target bucket, preserving its name. Possibly
   * copying also some of the metadata (e.g. content-type).
   *
   * @param targetBucket target bucket's name
   * @param options source blob options
   * @return a {@link CopyWriter} object that can be used to get information on the newly created
   *     blob or to complete the copy if more than one RPC request is needed
   * @throws StorageException upon failure
   */
  public CopyWriter copyTo(String targetBucket, BlobSourceOption... options) {
    return copyTo(targetBucket, info.name(), options);
  }

  /**
   * Sends a copy request for the current blob to the target blob. Possibly also some of the
   * metadata are copied (e.g. content-type).
   *
   * @param targetBucket target bucket's name
   * @param targetBlob target blob's name
   * @param options source blob options
   * @return a {@link CopyWriter} object that can be used to get information on the newly created
   *     blob or to complete the copy if more than one RPC request is needed
   * @throws StorageException upon failure
   */
  public CopyWriter copyTo(String targetBucket, String targetBlob, BlobSourceOption... options) {
    return copyTo(BlobId.of(targetBucket, targetBlob), options);
  }

  /**
   * Returns a {@code ReadChannel} object for reading this blob's content.
   *
   * @param options blob read options
   * @throws StorageException upon failure
   */
  public ReadChannel reader(BlobSourceOption... options) {
    return storage.reader(info.blobId(), toSourceOptions(info, options));
  }

  /**
   * Returns a {@code WriteChannel} object for writing to this blob. By default any md5 and
   * crc32c values in the current blob are ignored unless requested via the
   * {@code BlobWriteOption.md5Match} and {@code BlobWriteOption.crc32cMatch} options.
   *
   * @param options target blob options
   * @throws StorageException upon failure
   */
  public WriteChannel writer(BlobWriteOption... options) {
    return storage.writer(info, options);
  }

  /**
   * Generates a signed URL for this blob. If you want to allow access to for a fixed amount of time
   * for this blob, you can use this method to generate a URL that is only valid within a certain
   * time period. This is particularly useful if you don't want publicly accessible blobs, but don't
   * want to require users to explicitly log in.
   *
   * @param duration time until the signed URL expires, expressed in {@code unit}. The finer
   *     granularity supported is 1 second, finer granularities will be truncated
   * @param unit time unit of the {@code duration} parameter
   * @param options optional URL signing options
   * @return a signed URL for this bucket and the specified options
   * @see <a href="https://cloud.google.com/storage/docs/access-control#Signed-URLs">Signed-URLs</a>
   */
  public URL signUrl(long duration, TimeUnit unit, SignUrlOption... options) {
    return storage.signUrl(info, duration, unit, options);
  }

  /**
   * Returns the blob's {@code Storage} object used to issue requests.
   */
  public Storage storage() {
    return storage;
  }

  /**
   * Gets the requested blobs. A batch request is used to fetch blobs.
   *
   * @param storage the storage service used to issue the request
   * @param first the first blob to get
   * @param second the second blob to get
   * @param other other blobs to get
   * @return an immutable list of {@code Blob} objects. If a blob does not exist or access to it has
   *     been denied the corresponding item in the list is {@code null}
   * @throws StorageException upon failure
   */
  public static List<Blob> get(Storage storage, BlobId first, BlobId second, BlobId... other) {
    checkNotNull(storage);
    checkNotNull(first);
    checkNotNull(second);
    checkNotNull(other);
    ImmutableList<BlobId> blobs = ImmutableList.<BlobId>builder()
        .add(first)
        .add(second)
        .addAll(Arrays.asList(other))
        .build();
    return get(storage, blobs);
  }

  /**
   * Gets the requested blobs. A batch request is used to fetch blobs.
   *
   * @param storage the storage service used to issue the request
   * @param blobs list of blobs to get
   * @return an immutable list of {@code Blob} objects. If a blob does not exist or access to it has
   *     been denied the corresponding item in the list is {@code null}
   * @throws StorageException upon failure
   */
  public static List<Blob> get(final Storage storage, List<BlobId> blobs) {
    checkNotNull(storage);
    checkNotNull(blobs);
    BlobId[] blobArray = blobs.toArray(new BlobId[blobs.size()]);
    return Collections.unmodifiableList(Lists.transform(storage.get(blobArray),
        new Function<BlobInfo, Blob>() {
          @Override
          public Blob apply(BlobInfo blobInfo) {
            return blobInfo != null ? new Blob(storage, blobInfo) : null;
          }
        }));
  }

  /**
   * Updates the requested blobs. A batch request is used to update blobs. Original metadata are
   * merged with metadata in the provided {@code BlobInfo} objects. To replace metadata instead
   * you first have to unset them. Unsetting metadata can be done by setting the provided
   * {@code BlobInfo} objects metadata to {@code null}. See
   * {@link #update(com.google.gcloud.storage.BlobInfo,
   * com.google.gcloud.storage.Storage.BlobTargetOption...) } for a code example.
   *
   * @param storage the storage service used to issue the request
   * @param infos the blobs to update
   * @return an immutable list of {@code Blob} objects. If a blob does not exist or access to it has
   *     been denied the corresponding item in the list is {@code null}
   * @throws StorageException upon failure
   */
  public static List<Blob> update(final Storage storage, BlobInfo... infos) {
    checkNotNull(storage);
    checkNotNull(infos);
    if (infos.length == 0) {
      return Collections.emptyList();
    }
    return Collections.unmodifiableList(Lists.transform(storage.update(infos),
        new Function<BlobInfo, Blob>() {
          @Override
          public Blob apply(BlobInfo blobInfo) {
            return blobInfo != null ? new Blob(storage, blobInfo) : null;
          }
        }));
  }

  /**
   * Deletes the requested blobs. A batch request is used to delete blobs.
   *
   * @param storage the storage service used to issue the request
   * @param blobs the blobs to delete
   * @return an immutable list of booleans. If a blob has been deleted the corresponding item in the
   *     list is {@code true}. If a blob was not found, deletion failed or access to the resource
   *     was denied the corresponding item is {@code false}
   * @throws StorageException upon failure
   */
  public static List<Boolean> delete(Storage storage, BlobId... blobs) {
    checkNotNull(storage);
    checkNotNull(blobs);
    if (blobs.length == 0) {
      return Collections.emptyList();
    }
    return storage.delete(blobs);
  }
}
