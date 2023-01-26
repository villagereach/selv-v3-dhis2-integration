package org.openlmis.integration.dhis2.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.integration.dhis2.repository.indicator.CCERepository;
import org.openlmis.integration.dhis2.service.indicator.IndicatorService;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IndicatorServiceTest {

  @InjectMocks
  private IndicatorService indicatorService;
  @Mock
  private CCERepository cceRepository;

  @Test
  public void cceIndicatorTest() {
    when(cceRepository.findCCECountByStatus("FUNCTIONAL")).thenReturn(5L);

    String indicator = indicatorService.generate("CCE");
    assertThat(indicator, is("Vaccine: 5"));
  }

}
