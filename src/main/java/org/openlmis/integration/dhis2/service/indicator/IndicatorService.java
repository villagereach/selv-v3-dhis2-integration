package org.openlmis.integration.dhis2.service.indicator;

import org.openlmis.integration.dhis2.repository.indicator.CCERepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IndicatorService {

  @Autowired
  private CCERepository cceRepository;

  public String generate(String enumerator) {
    String calculatedIndicator = "";
    if (enumerator.equals("CCE")) {
      Long count = cceRepository.findCCECountByStatus("FUNCTIONAL");
      OpeningBalance openingBalance = new OpeningBalance();
      calculatedIndicator = openingBalance.calculateValue(count);
    }
    return calculatedIndicator;
  }

}
