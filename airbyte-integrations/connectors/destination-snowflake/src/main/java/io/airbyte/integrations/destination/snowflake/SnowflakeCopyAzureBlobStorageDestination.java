/*
 * Copyright (c) 2021 Airbyte, Inc., all rights reserved.
 */

package io.airbyte.integrations.destination.snowflake;

import static io.airbyte.integrations.destination.jdbc.copy.azure.AzureBlobStorageConfig.getAzureBlobConfig;

import com.fasterxml.jackson.databind.JsonNode;
import io.airbyte.db.jdbc.JdbcDatabase;
import io.airbyte.integrations.base.AirbyteMessageConsumer;
import io.airbyte.integrations.destination.ExtendedNameTransformer;
import io.airbyte.integrations.destination.azure_blob_storage.AzureBlobStorageConnectionChecker;
import io.airbyte.integrations.destination.azure_blob_storage.AzureBlobStorageDestinationConfig;
import io.airbyte.integrations.destination.jdbc.SqlOperations;
import io.airbyte.integrations.destination.jdbc.copy.CopyConsumerFactory;
import io.airbyte.integrations.destination.jdbc.copy.CopyDestination;
import io.airbyte.protocol.models.AirbyteMessage;
import io.airbyte.protocol.models.ConfiguredAirbyteCatalog;
import java.util.function.Consumer;

public class SnowflakeCopyAzureBlobStorageDestination extends CopyDestination {

  @Override
  public AirbyteMessageConsumer getConsumer(JsonNode config, ConfiguredAirbyteCatalog catalog, Consumer<AirbyteMessage> outputRecordCollector)
      throws Exception {
    return CopyConsumerFactory.create(
        outputRecordCollector,
        getDatabase(config),
        getSqlOperations(),
        getNameTransformer(),
        getAzureBlobConfig(config.get("loading_method")),
        catalog,
        new SnowflakeAzureBlobStorageStreamCopierFactory(),
        getConfiguredSchema(config));
  }

  @Override
  public void checkPersistence(JsonNode config) throws Exception {
    final AzureBlobStorageConnectionChecker client = new AzureBlobStorageConnectionChecker(
        AzureBlobStorageDestinationConfig.getAzureBlobStorageConfig(config.get("loading_method")));
    client.attemptWriteAndDelete();
  }

  @Override
  public ExtendedNameTransformer getNameTransformer() {
    return new SnowflakeSQLNameTransformer();
  }

  @Override
  public JdbcDatabase getDatabase(JsonNode config) throws Exception {
    return SnowflakeDatabase.getDatabase(config);
  }

  @Override
  public SqlOperations getSqlOperations() {
    return new SnowflakeSqlOperations();
  }

  private String getConfiguredSchema(final JsonNode config) {
    return config.get("schema").asText();
  }

}