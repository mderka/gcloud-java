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
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.gcloud.bigquery.TableInfo.StreamingBuffer;

import org.junit.Test;

import java.util.List;

public class TableInfoTest {

  private static final Field FIELD_SCHEMA1 =
      Field.builder("StringField", Field.Type.string())
          .mode(Field.Mode.NULLABLE)
          .description("FieldDescription1")
          .build();
  private static final Field FIELD_SCHEMA2 =
      Field.builder("IntegerField", Field.Type.integer())
          .mode(Field.Mode.REPEATED)
          .description("FieldDescription2")
          .build();
  private static final Field FIELD_SCHEMA3 =
      Field.builder("RecordField", Field.Type.record(FIELD_SCHEMA1, FIELD_SCHEMA2))
          .mode(Field.Mode.REQUIRED)
          .description("FieldDescription3")
          .build();
  private static final Schema TABLE_SCHEMA = Schema.of(FIELD_SCHEMA1, FIELD_SCHEMA2, FIELD_SCHEMA3);
  private static final String VIEW_QUERY = "VIEW QUERY";
  private static final List<String> SOURCE_URIS = ImmutableList.of("uri1", "uri2");
  private static final Integer MAX_BAD_RECORDS = 42;
  private static final Boolean IGNORE_UNKNOWN_VALUES = true;
  private static final String COMPRESSION = "GZIP";
  private static final ExternalDataConfiguration CONFIGURATION = ExternalDataConfiguration
      .builder(SOURCE_URIS, TABLE_SCHEMA, FormatOptions.datastoreBackup())
      .compression(COMPRESSION)
      .ignoreUnknownValues(IGNORE_UNKNOWN_VALUES)
      .maxBadRecords(MAX_BAD_RECORDS)
      .build();
  private static final String ETAG = "etag";
  private static final String ID = "project:dataset:table";
  private static final String SELF_LINK = "selfLink";
  private static final TableId TABLE_ID = TableId.of("dataset", "table");
  private static final String FRIENDLY_NAME = "friendlyName";
  private static final String DESCRIPTION = "description";
  private static final Long NUM_BYTES = 42L;
  private static final Long NUM_ROWS = 43L;
  private static final Long CREATION_TIME = 10L;
  private static final Long EXPIRATION_TIME = 100L;
  private static final Long LAST_MODIFIED_TIME = 20L;
  private static final String LOCATION = "US";
  private static final StreamingBuffer STREAMING_BUFFER = new StreamingBuffer(1L, 2L, 3L);
  private static final TableInfo TABLE_INFO =
      TableInfo.builder(TABLE_ID, TABLE_SCHEMA)
          .creationTime(CREATION_TIME)
          .description(DESCRIPTION)
          .etag(ETAG)
          .expirationTime(EXPIRATION_TIME)
          .friendlyName(FRIENDLY_NAME)
          .id(ID)
          .lastModifiedTime(LAST_MODIFIED_TIME)
          .location(LOCATION)
          .numBytes(NUM_BYTES)
          .numRows(NUM_ROWS)
          .selfLink(SELF_LINK)
          .streamingBuffer(STREAMING_BUFFER)
          .build();
  private static final ExternalTableInfo EXTERNAL_TABLE_INFO =
      ExternalTableInfo.builder(TABLE_ID, CONFIGURATION)
          .creationTime(CREATION_TIME)
          .description(DESCRIPTION)
          .etag(ETAG)
          .expirationTime(EXPIRATION_TIME)
          .friendlyName(FRIENDLY_NAME)
          .id(ID)
          .lastModifiedTime(LAST_MODIFIED_TIME)
          .numBytes(NUM_BYTES)
          .numRows(NUM_ROWS)
          .selfLink(SELF_LINK)
          .build();
  private static final List<UserDefinedFunction> USER_DEFINED_FUNCTIONS =
      ImmutableList.of(UserDefinedFunction.inline("Function"), UserDefinedFunction.fromUri("URI"));
  private static final ViewInfo VIEW_INFO =
      ViewInfo.builder(TABLE_ID, VIEW_QUERY, USER_DEFINED_FUNCTIONS)
          .creationTime(CREATION_TIME)
          .description(DESCRIPTION)
          .etag(ETAG)
          .expirationTime(EXPIRATION_TIME)
          .friendlyName(FRIENDLY_NAME)
          .id(ID)
          .lastModifiedTime(LAST_MODIFIED_TIME)
          .numBytes(NUM_BYTES)
          .numRows(NUM_ROWS)
          .selfLink(SELF_LINK)
          .build();

  @Test
  public void testToBuilder() {
    compareTableInfo(TABLE_INFO, TABLE_INFO.toBuilder().build());
    compareViewInfo(VIEW_INFO, VIEW_INFO.toBuilder().build());
    compareExternalTableInfo(EXTERNAL_TABLE_INFO, EXTERNAL_TABLE_INFO.toBuilder().build());
    BaseTableInfo tableInfo = TABLE_INFO.toBuilder()
        .description("newDescription")
        .build();
    assertEquals("newDescription", tableInfo.description());
    tableInfo = tableInfo.toBuilder()
        .description("description")
        .build();
    compareBaseTableInfo(TABLE_INFO, tableInfo);
  }

  @Test
  public void testToBuilderIncomplete() {
    BaseTableInfo tableInfo = TableInfo.of(TABLE_ID, TABLE_SCHEMA);
    assertEquals(tableInfo, tableInfo.toBuilder().build());
    tableInfo = ViewInfo.of(TABLE_ID, VIEW_QUERY);
    assertEquals(tableInfo, tableInfo.toBuilder().build());
    tableInfo = ExternalTableInfo.of(TABLE_ID, CONFIGURATION);
    assertEquals(tableInfo, tableInfo.toBuilder().build());
  }

  @Test
  public void testBuilder() {
    assertEquals(TABLE_ID, TABLE_INFO.tableId());
    assertEquals(TABLE_SCHEMA, TABLE_INFO.schema());
    assertEquals(CREATION_TIME, TABLE_INFO.creationTime());
    assertEquals(DESCRIPTION, TABLE_INFO.description());
    assertEquals(ETAG, TABLE_INFO.etag());
    assertEquals(EXPIRATION_TIME, TABLE_INFO.expirationTime());
    assertEquals(FRIENDLY_NAME, TABLE_INFO.friendlyName());
    assertEquals(ID, TABLE_INFO.id());
    assertEquals(LAST_MODIFIED_TIME, TABLE_INFO.lastModifiedTime());
    assertEquals(LOCATION, TABLE_INFO.location());
    assertEquals(NUM_BYTES, TABLE_INFO.numBytes());
    assertEquals(NUM_ROWS, TABLE_INFO.numRows());
    assertEquals(SELF_LINK, TABLE_INFO.selfLink());
    assertEquals(STREAMING_BUFFER, TABLE_INFO.streamingBuffer());
    assertEquals(BaseTableInfo.Type.TABLE, TABLE_INFO.type());
    assertEquals(TABLE_ID, VIEW_INFO.tableId());
    assertEquals(null, VIEW_INFO.schema());
    assertEquals(VIEW_QUERY, VIEW_INFO.query());
    assertEquals(BaseTableInfo.Type.VIEW, VIEW_INFO.type());
    assertEquals(CREATION_TIME, VIEW_INFO.creationTime());
    assertEquals(DESCRIPTION, VIEW_INFO.description());
    assertEquals(ETAG, VIEW_INFO.etag());
    assertEquals(EXPIRATION_TIME, VIEW_INFO.expirationTime());
    assertEquals(FRIENDLY_NAME, VIEW_INFO.friendlyName());
    assertEquals(ID, VIEW_INFO.id());
    assertEquals(LAST_MODIFIED_TIME, VIEW_INFO.lastModifiedTime());
    assertEquals(NUM_BYTES, VIEW_INFO.numBytes());
    assertEquals(NUM_ROWS, VIEW_INFO.numRows());
    assertEquals(SELF_LINK, VIEW_INFO.selfLink());
    assertEquals(BaseTableInfo.Type.VIEW, VIEW_INFO.type());
    assertEquals(TABLE_ID, EXTERNAL_TABLE_INFO.tableId());
    assertEquals(null, EXTERNAL_TABLE_INFO.schema());
    assertEquals(CONFIGURATION, EXTERNAL_TABLE_INFO.configuration());
    assertEquals(CREATION_TIME, EXTERNAL_TABLE_INFO.creationTime());
    assertEquals(DESCRIPTION, EXTERNAL_TABLE_INFO.description());
    assertEquals(ETAG, EXTERNAL_TABLE_INFO.etag());
    assertEquals(EXPIRATION_TIME, EXTERNAL_TABLE_INFO.expirationTime());
    assertEquals(FRIENDLY_NAME, EXTERNAL_TABLE_INFO.friendlyName());
    assertEquals(ID, EXTERNAL_TABLE_INFO.id());
    assertEquals(LAST_MODIFIED_TIME, EXTERNAL_TABLE_INFO.lastModifiedTime());
    assertEquals(NUM_BYTES, EXTERNAL_TABLE_INFO.numBytes());
    assertEquals(NUM_ROWS, EXTERNAL_TABLE_INFO.numRows());
    assertEquals(SELF_LINK, EXTERNAL_TABLE_INFO.selfLink());
    assertEquals(BaseTableInfo.Type.EXTERNAL, EXTERNAL_TABLE_INFO.type());
  }

  @Test
  public void testToAndFromPb() {
    assertTrue(BaseTableInfo.fromPb(TABLE_INFO.toPb()) instanceof TableInfo);
    compareTableInfo(TABLE_INFO, BaseTableInfo.<TableInfo>fromPb(TABLE_INFO.toPb()));
    assertTrue(BaseTableInfo.fromPb(VIEW_INFO.toPb()) instanceof ViewInfo);
    compareViewInfo(VIEW_INFO, BaseTableInfo.<ViewInfo>fromPb(VIEW_INFO.toPb()));
    assertTrue(BaseTableInfo.fromPb(EXTERNAL_TABLE_INFO.toPb()) instanceof ExternalTableInfo);
    compareExternalTableInfo(EXTERNAL_TABLE_INFO,
        BaseTableInfo.<ExternalTableInfo>fromPb(EXTERNAL_TABLE_INFO.toPb()));
  }

  private void compareBaseTableInfo(BaseTableInfo expected, BaseTableInfo value) {
    assertEquals(expected, value);
    assertEquals(expected.tableId(), value.tableId());
    assertEquals(expected.schema(), value.schema());
    assertEquals(expected.type(), value.type());
    assertEquals(expected.creationTime(), value.creationTime());
    assertEquals(expected.description(), value.description());
    assertEquals(expected.etag(), value.etag());
    assertEquals(expected.expirationTime(), value.expirationTime());
    assertEquals(expected.friendlyName(), value.friendlyName());
    assertEquals(expected.id(), value.id());
    assertEquals(expected.lastModifiedTime(), value.lastModifiedTime());
    assertEquals(expected.numBytes(), value.numBytes());
    assertEquals(expected.numRows(), value.numRows());
    assertEquals(expected.selfLink(), value.selfLink());
    assertEquals(expected.type(), value.type());
  }

  private void compareTableInfo(TableInfo expected, TableInfo value) {
    compareBaseTableInfo(expected, value);
    assertEquals(expected, value);
    assertEquals(expected.location(), value.location());
    assertEquals(expected.streamingBuffer(), value.streamingBuffer());
  }

  private void compareViewInfo(ViewInfo expected, ViewInfo value) {
    compareBaseTableInfo(expected, value);
    assertEquals(expected, value);
    assertEquals(expected.query(), value.query());
    assertEquals(expected.userDefinedFunctions(), value.userDefinedFunctions());
  }

  private void compareExternalTableInfo(ExternalTableInfo expected, ExternalTableInfo value) {
    compareBaseTableInfo(expected, value);
    assertEquals(expected, value);
    assertEquals(expected.configuration(), value.configuration());
  }
}
