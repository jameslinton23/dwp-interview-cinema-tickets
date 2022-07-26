package uk.gov.dwp.uc.pairtest.domain;

import org.junit.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.*;
import static uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type.ADULT;
import static uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type.CHILD;
import static uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type.INFANT;

public class TicketTypeRequestFactoryTest {

    public static final int REQUESTED_NR_TICKETS_ADULT = 1;
    public static final int REQUESTED_NR_TICKETS_CHILD = 2;
    public static final int REQUESTED_NR_TICKETS_INFANT = 3;

    private final TicketTypeRequestFactory underTest = new TicketTypeRequestFactory();

    @Test
    public void shouldReturnAListOfTicketTypeRequestsFromATicketOrderRequest() {
        TicketOrderRequest ticketOrderRequest = new TicketOrderRequest(REQUESTED_NR_TICKETS_ADULT,
                                                                       REQUESTED_NR_TICKETS_CHILD,
                                                                       REQUESTED_NR_TICKETS_INFANT);
        List<TicketTypeRequest> result = underTest.createListOfTicketRequests(ticketOrderRequest);

        List<TicketTypeRequest> expected = List.of(new TicketTypeRequest(ADULT, REQUESTED_NR_TICKETS_ADULT),
                                                   new TicketTypeRequest(CHILD, REQUESTED_NR_TICKETS_CHILD),
                                                   new TicketTypeRequest(INFANT, REQUESTED_NR_TICKETS_INFANT));
        assertThat(result, is(expected));
    }

    @Test
    public void shouldReturnAListOfOneTicketTypeRequestsFromATicketOrderRequest() {
        TicketOrderRequest ticketOrderRequest = new TicketOrderRequest(REQUESTED_NR_TICKETS_ADULT,
                                                                       0,
                                                                       null);
        List<TicketTypeRequest> result = underTest.createListOfTicketRequests(ticketOrderRequest);

        List<TicketTypeRequest> expected = List.of(new TicketTypeRequest(ADULT, REQUESTED_NR_TICKETS_ADULT));
        assertThat(result, is(expected));
    }
}
