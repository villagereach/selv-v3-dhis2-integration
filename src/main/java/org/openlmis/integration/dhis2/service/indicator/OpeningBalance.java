package org.openlmis.integration.dhis2.service.indicator;

public class OpeningBalance implements IndicatorSupplier {

  private static final String INDICATOR_NAME = "OpeningBalance";

  public String getIndicatorName() { return INDICATOR_NAME; }

  public String calculateValue(Long databaseCount) {
    return "Vaccine: " + databaseCount;
  }

}
