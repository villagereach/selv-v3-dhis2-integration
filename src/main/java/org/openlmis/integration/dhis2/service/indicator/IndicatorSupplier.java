package org.openlmis.integration.dhis2.service.indicator;

public interface IndicatorSupplier {

  String getIndicatorName();

  String calculateValue(Long databaseCount);

}
