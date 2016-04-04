/*
 * Copyright 2016 Google Inc. All Rights Reserved.
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

package com.google.gcloud.dns;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.api.services.dns.model.Change;
import com.google.common.base.Function;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Objects;

/**
 * An immutable class representing an atomic update to a collection of {@link RecordSet}s within a
 * {@code Zone}.
 *
 * @see <a href="https://cloud.google.com/dns/api/v1/changes">Google Cloud DNS documentation</a>
 */
public class ChangeRequest extends ChangeRequestInfo {

  private static final long serialVersionUID = 5335667200595081449L;
  private final DnsOptions options;
  private final String zone;
  private transient Dns dns;

  /**
   * A builder for {@code ChangeRequest}s.
   */
  public static class Builder extends ChangeRequestInfo.Builder {

    private final Dns dns;
    private final String zone;
    private final ChangeRequestInfo.BuilderImpl infoBuilder;

    private Builder(ChangeRequest cr) {
      this.dns = cr.dns;
      this.zone = cr.zone;
      this.infoBuilder = new ChangeRequestInfo.BuilderImpl(cr);
    }

    @Override
    public Builder additions(List<RecordSet> additions) {
      infoBuilder.additions(additions);
      return this;
    }

    @Override
    public Builder deletions(List<RecordSet> deletions) {
      infoBuilder.deletions(deletions);
      return this;
    }

    @Override
    public Builder add(RecordSet recordSet) {
      infoBuilder.add(recordSet);
      return this;
    }

    @Override
    public Builder delete(RecordSet recordSet) {
      infoBuilder.delete(recordSet);
      return this;
    }

    @Override
    public Builder clearAdditions() {
      infoBuilder.clearAdditions();
      return this;
    }

    @Override
    public Builder clearDeletions() {
      infoBuilder.clearDeletions();
      return this;
    }

    @Override
    public Builder removeAddition(RecordSet recordSet) {
      infoBuilder.removeAddition(recordSet);
      return this;
    }

    @Override
    public Builder removeDeletion(RecordSet recordSet) {
      infoBuilder.removeDeletion(recordSet);
      return this;
    }

    @Override
    Builder generatedId(String generatedId) {
      infoBuilder.generatedId(generatedId);
      return this;
    }

    @Override
    Builder startTimeMillis(long startTimeMillis) {
      infoBuilder.startTimeMillis(startTimeMillis);
      return this;
    }

    @Override
    Builder status(Status status) {
      infoBuilder.status(status);
      return this;
    }

    @Override
    public ChangeRequest build() {
      return new ChangeRequest(dns, zone, infoBuilder);
    }
  }

  ChangeRequest(Dns dns, String zone, ChangeRequest.BuilderImpl infoBuilder) {
    super(infoBuilder);
    this.zone = checkNotNull(zone);
    this.dns = checkNotNull(dns);
    this.options = dns.options();
  }

  /**
   * Returns the name of the {@link Zone} associated with this change request.
   */
  public String zone() {
    return this.zone;
  }

  /**
   * Returns the change request's {@code Dns} object used to issue requests.
   */
  public Dns dns() {
    return dns;
  }

  /**
   * Applies this change request to the zone identified by {@code zoneName}.
   */
  public ChangeRequest applyTo(String zoneName, Dns.ChangeRequestOption... options) {
    return dns.applyChangeRequest(zoneName, this, options);
  }

  /**
   * Retrieves the up-to-date information about the change request from Google Cloud DNS. Parameter
   * {@code options} can be used to restrict the fields to be included in the updated object the
   * same way as in {@link Dns#getChangeRequest(String, String, Dns.ChangeRequestOption...)}. If
   * {@code options} are provided, any field other than generatedId which is not included in the
   * {@code options} will be {@code null} regardless of whether they are initialized or not in
   * {@code this} instance.
   *
   * @return an object with the updated information or {@code null} if it does not exist
   * @throws DnsException upon failure of the API call or if the associated zone was not found
   */
  public ChangeRequest reload(Dns.ChangeRequestOption... options) {
    return dns.getChangeRequest(zone, generatedId(), options);
  }

  /**
   * Returns {@code true} if the change request has been completed. The function makes an API call
   * to Google Cloud DNS in order to request the status update only if the status of the change
   * request is {@link Status#PENDING}. If the status is already {@link Status#DONE}, the method
   * returns {@code true} without an API call.
   *
   * @throws DnsException upon failure of the API call or if the associated zone was not found
   */
  public boolean isDone() {
    if (status() == Status.DONE) {
      return true;
    }
    ChangeRequest updated = reload(Dns.ChangeRequestOption.fields(Dns.ChangeRequestField.STATUS));
    return updated == null || updated.status() == Status.DONE;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !obj.getClass().equals(ChangeRequest.class)) {
      return false;
    } else {
      ChangeRequest other = (ChangeRequest) obj;
      return Objects.equals(options, other.options)
          && Objects.equals(zone, other.zone)
          && Objects.equals(toPb(), other.toPb());
    }
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), options, zone);
  }

  private void readObject(ObjectInputStream input) throws IOException, ClassNotFoundException {
    input.defaultReadObject();
    this.dns = options.service();
  }

  static ChangeRequest fromPb(Dns dns, String zoneName, Change pb) {
    ChangeRequestInfo info = ChangeRequestInfo.fromPb(pb);
    return new ChangeRequest(dns, zoneName, new ChangeRequestInfo.BuilderImpl(info));
  }

  static Function<Change, ChangeRequest> fromPbFunction(final Dns dns, final String zoneName) {
    return new Function<Change, ChangeRequest>() {
      @Override
      public ChangeRequest apply(com.google.api.services.dns.model.Change pb) {
        return ChangeRequest.fromPb(dns, zoneName, pb);
      }
    };
  }
}
