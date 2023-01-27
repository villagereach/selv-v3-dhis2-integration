package org.openlmis.integration.dhis2.service.indicator;

import org.openlmis.integration.dhis2.domain.enumerator.IndicatorEnum;
import org.openlmis.integration.dhis2.repository.indicator.CCERepository;
import org.springframework.beans.factory.annotation.Autowired;

public class CCEOperational implements IndicatorSupplier {

  public static final String STATUS = "FUNCTIONING";
  public static final String NAME = IndicatorEnum.CCE_OPERATIONAL.toString();

  @Autowired
  private CCERepository cceRepository;

  public String getIndicatorName() { return NAME; }

  public String calculateValue() {
    Long count = cceRepository.findCCECountByStatus(STATUS);

    return "FridgesOperational: " + count;
  }

}
