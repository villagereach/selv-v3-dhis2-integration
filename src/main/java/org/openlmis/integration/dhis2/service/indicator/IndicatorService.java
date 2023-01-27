package org.openlmis.integration.dhis2.service.indicator;

import org.openlmis.integration.dhis2.domain.enumerator.IndicatorEnum;
import org.openlmis.integration.dhis2.repository.indicator.CCERepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IndicatorService {

  public String generate(IndicatorEnum indicatorEnum) {
    String calculatedIndicator = "";
    switch (indicatorEnum) {
      case OPENING_BALANCE:
        break;
      case RECEIVED:
        break;
      case CLOSING_BALANCE:
        break;
      case CCE_ALLOCATED:
        break;
      case CCE_OPERATIONAL:
        calculatedIndicator = new CCEOperational().calculateValue();
        break;
      case NEGATIVE_ADJUSTMENTS:
        break;
      case POSITIVE_ADJUSTMENTS:
        break;
      case ADJUSTMENTS_BY_REASON:
    }

    return calculatedIndicator;
  }

}
