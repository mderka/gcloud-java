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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.common.collect.ImmutableList;
import com.google.gcloud.bigquery.JobInfo.CreateDisposition;
import com.google.gcloud.bigquery.JobInfo.WriteDisposition;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class LoadConfigurationTest {

  private static final CsvOptions CSV_OPTIONS = CsvOptions.builder()
      .allowJaggedRows(true)
      .allowQuotedNewLines(false)
      .encoding(StandardCharsets.UTF_8)
      .build();
  private static final TableId TABLE_ID = TableId.of("dataset", "table");
  private static final CreateDisposition CREATE_DISPOSITION = CreateDisposition.CREATE_IF_NEEDED;
  private static final WriteDisposition WRITE_DISPOSITION = WriteDisposition.WRITE_APPEND;
  private static final Integer MAX_BAD_RECORDS = 42;
  private static final String FORMAT = "CSV";
  private static final Boolean IGNORE_UNKNOWN_VALUES = true;
  private static final List<String> PROJECTION_FIELDS = ImmutableList.of("field1", "field2");
  private static final Field FIELD_SCHEMA = Field.builder("IntegerField", Field.Type.integer())
      .mode(Field.Mode.REQUIRED)
      .description("FieldDescription")
      .build();
  private static final Schema TABLE_SCHEMA = Schema.of(FIELD_SCHEMA);
  private static final LoadConfiguration LOAD_CONFIGURATION = LoadConfiguration.builder(TABLE_ID)
      .createDisposition(CREATE_DISPOSITION)
      .writeDisposition(WRITE_DISPOSITION)
      .formatOptions(CSV_OPTIONS)
      .ignoreUnknownValues(IGNORE_UNKNOWN_VALUES)
      .maxBadRecords(MAX_BAD_RECORDS)
      .projectionFields(PROJECTION_FIELDS)
      .schema(TABLE_SCHEMA)
      .build();

  @Test
  public void testToBuilder() {
    compareLoadConfiguration(LOAD_CONFIGURATION, LOAD_CONFIGURATION.toBuilder().build());
    LoadConfiguration configuration = LOAD_CONFIGURATION.toBuilder()
        .destinationTable(TableId.of("dataset", "newTable"))
        .build();
    assertEquals("newTable", configuration.destinationTable().table());
    configuration = configuration.toBuilder().destinationTable(TABLE_ID).build();
    compareLoadConfiguration(LOAD_CONFIGURATION, configuration);
  }

  @Test
  public void testOf() {
    LoadConfiguration configuration = LoadConfiguration.of(TABLE_ID);
    assertEquals(TABLE_ID, configuration.destinationTable());
    configuration = LoadConfiguration.of(TABLE_ID, CSV_OPTIONS);
    assertEquals(TABLE_ID, configuration.destinationTable());
    assertEquals(FORMAT, configuration.format());
    assertEquals(CSV_OPTIONS, configuration.csvOptions());
  }

  @Test
  public void testToBuilderIncomplete() {
    LoadConfiguration configuration = LoadConfiguration.of(TABLE_ID);
    compareLoadConfiguration(configuration, configuration.toBuilder().build());
  }

  @Test
  public void testBuilder() {
    assertEquals(TABLE_ID, LOAD_CONFIGURATION.destinationTable());
    assertEquals(CREATE_DISPOSITION, LOAD_CONFIGURATION.createDisposition());
    assertEquals(WRITE_DISPOSITION, LOAD_CONFIGURATION.writeDisposition());
    assertEquals(CSV_OPTIONS, LOAD_CONFIGURATION.csvOptions());
    assertEquals(FORMAT, LOAD_CONFIGURATION.format());
    assertEquals(IGNORE_UNKNOWN_VALUES, LOAD_CONFIGURATION.ignoreUnknownValues());
    assertEquals(MAX_BAD_RECORDS, LOAD_CONFIGURATION.maxBadRecords());
    assertEquals(PROJECTION_FIELDS, LOAD_CONFIGURATION.projectionFields());
    assertEquals(TABLE_SCHEMA, LOAD_CONFIGURATION.schema());
  }

  @Test
  public void testToPbAndFromPb() {
    assertNull(LOAD_CONFIGURATION.toPb().getSourceUris());
    compareLoadConfiguration(LOAD_CONFIGURATION,
        LoadConfiguration.fromPb(LOAD_CONFIGURATION.toPb()));
    LoadConfiguration configuration = LoadConfiguration.of(TABLE_ID);
    compareLoadConfiguration(configuration, LoadConfiguration.fromPb(configuration.toPb()));
  }

  private void compareLoadConfiguration(LoadConfiguration expected, LoadConfiguration value) {
    assertEquals(expected, value);
    assertEquals(expected.hashCode(), value.hashCode());
    assertEquals(expected.toString(), value.toString());
    assertEquals(expected.destinationTable(), value.destinationTable());
    assertEquals(expected.createDisposition(), value.createDisposition());
    assertEquals(expected.writeDisposition(), value.writeDisposition());
    assertEquals(expected.csvOptions(), value.csvOptions());
    assertEquals(expected.format(), value.format());
    assertEquals(expected.ignoreUnknownValues(), value.ignoreUnknownValues());
    assertEquals(expected.maxBadRecords(), value.maxBadRecords());
    assertEquals(expected.projectionFields(), value.projectionFields());
    assertEquals(expected.schema(), value.schema());
  }
}
