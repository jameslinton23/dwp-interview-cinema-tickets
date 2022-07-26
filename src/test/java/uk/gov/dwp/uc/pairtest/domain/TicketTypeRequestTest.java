package uk.gov.dwp.uc.pairtest.domain;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.*;

public class TicketTypeRequestTest {

    public static final int NR_OF_TICKETS = 2;

    @Test
    public void shouldCalculateCorrectTotalAmountForRequestedNumberOfTicketsForAnAdultTicketType() {
        TicketTypeRequest underTest = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, NR_OF_TICKETS);

        int result = underTest.getTotalCostForTicketType();

        int expected = underTest.getTicketType().getTicketCost() * NR_OF_TICKETS;
        assertThat(result, is(expected));
    }

    @Test
    public void shouldCalculateCorrectTotalAmountForRequestedNumberOfTicketsForAChildTicketType() {
        TicketTypeRequest underTest = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, NR_OF_TICKETS);

        int result = underTest.getTotalCostForTicketType();

        int expected = underTest.getTicketType().getTicketCost() * NR_OF_TICKETS;
        assertThat(result, is(expected));
    }

    @Test
    public void shouldCalculateCorrectTotalAmountForRequestedNumberOfTicketsForAnInfantTicketType() {
        TicketTypeRequest underTest = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, NR_OF_TICKETS);

        int result = underTest.getTotalCostForTicketType();

        int expected = underTest.getTicketType().getTicketCost() * NR_OF_TICKETS;
        assertThat(result, is(expected));
    }

}
