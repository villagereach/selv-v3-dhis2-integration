#!/bin/sh

# Sync with Transifex
/transifex/sync_transifex.sh \
  --resource selv-v3-dhis2-integration.messages \
  --pattern 'src/main/resources/messages_<lang>.properties' \
  --source-file src/main/resources/messages_en.properties


# Run Gradle build
gradle clean build
