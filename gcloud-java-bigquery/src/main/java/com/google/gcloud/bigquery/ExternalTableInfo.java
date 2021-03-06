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

package com.google.gcloud.bigquery;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.api.services.bigquery.model.Table;
import com.google.common.base.MoreObjects.ToStringHelper;

import java.util.Objects;

/**
 * Google BigQuery External Table information. BigQuery's external tables are tables whose data
 * reside outside of BigQuery but can be queried as normal BigQuery tables. External tables are
 * experimental and might be subject to change or removed.
 *
 * @see <a href="https://cloud.google.com/bigquery/federated-data-sources">Federated Data Sources
 *     </a>
 */
public class ExternalTableInfo extends BaseTableInfo {

  private static final long serialVersionUID = -5893406738246214865L;

  private final ExternalDataConfiguration configuration;

  public static final class Builder extends BaseTableInfo.Builder<ExternalTableInfo, Builder> {

    private ExternalDataConfiguration configuration;

    private Builder() {}

    private Builder(ExternalTableInfo tableInfo) {
      super(tableInfo);
      this.configuration = tableInfo.configuration;
    }

    protected Builder(Table tablePb) {
      super(tablePb);
      if (tablePb.getExternalDataConfiguration() != null) {
        this.configuration =
            ExternalDataConfiguration.fromPb(tablePb.getExternalDataConfiguration());
      }
    }

    /**
     * Sets the data format, location and other properties of a table stored outside of BigQuery.
     *
     * @see <a href="https://cloud.google.com/bigquery/federated-data-sources">Federated Data
     *     Sources</a>
     */
    public Builder configuration(ExternalDataConfiguration configuration) {
      this.configuration = checkNotNull(configuration);
      return self();
    }

    /**
     * Creates a {@code ExternalTableInfo} object.
     */
    @Override
    public ExternalTableInfo build() {
      return new ExternalTableInfo(this);
    }
  }

  private ExternalTableInfo(Builder builder) {
    super(builder);
    this.configuration = builder.configuration;
  }

  /**
   * Returns the data format, location and other properties of a table stored outside of BigQuery.
   * This property is experimental and might be subject to change or removed.
   *
   * @see <a href="https://cloud.google.com/bigquery/federated-data-sources">Federated Data Sources
   *     </a>
   */
  public ExternalDataConfiguration configuration() {
    return configuration;
  }

  /**
   * Returns a builder for the {@code ExternalTableInfo} object.
   */
  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  ToStringHelper toStringHelper() {
    return super.toStringHelper().add("configuration", configuration);
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof ExternalTableInfo && baseEquals((ExternalTableInfo) obj);
  }

  @Override
  public int hashCode() {
    return Objects.hash(baseHashCode(), configuration);
  }

  @Override
  Table toPb() {
    Table tablePb = super.toPb();
    tablePb.setExternalDataConfiguration(configuration.toPb());
    return tablePb;
  }

  /**
   * Returns a builder for a BigQuery External Table.
   *
   * @param tableId table id
   * @param configuration data format, location and other properties of an External Table
   */
  public static Builder builder(TableId tableId, ExternalDataConfiguration configuration) {
    return new Builder().tableId(tableId).type(Type.EXTERNAL).configuration(configuration);
  }

  /**
   * Returns a BigQuery External Table.
   *
   * @param table table id
   * @param configuration data format, location and other properties of an External Table
   */
  public static ExternalTableInfo of(TableId table, ExternalDataConfiguration configuration) {
    return builder(table, configuration).build();
  }

  @SuppressWarnings("unchecked")
  static ExternalTableInfo fromPb(Table tablePb) {
    return new Builder(tablePb).build();
  }
}
