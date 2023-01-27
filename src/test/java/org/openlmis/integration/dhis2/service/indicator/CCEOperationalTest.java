package org.openlmis.integration.dhis2.service.indicator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openlmis.integration.dhis2.domain.enumerator.IndicatorEnum;
import org.openlmis.integration.dhis2.repository.indicator.CCERepository;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CCEOperationalTest {

  @InjectMocks
  private CCEOperational cceOperational;

  @Mock
  private CCERepository cceRepository;

  @Test
  public void cceIndicatorTest() {
    when(cceRepository.findCCECountByStatus(CCEOperational.STATUS)).thenReturn(5L);

    String indicator = cceOperational.calculateValue();
    assertThat(indicator, is("FridgesOperational: 5"));
  }

}
