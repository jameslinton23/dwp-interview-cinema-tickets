package uk.gov.dwp.uc.pairtest.adaptors;

import org.hamcrest.core.Is;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.gov.dwp.uc.pairtest.TicketService;
import uk.gov.dwp.uc.pairtest.domain.TicketOrderRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequestFactory;
import uk.gov.dwp.uc.pairtest.exception.InvalidAccountNumberException;
import uk.gov.dwp.uc.pairtest.exception.MaximumNrOfTicketsAllowedExceededException;
import uk.gov.dwp.uc.pairtest.exception.NoAdultTicketsRequestedException;
import uk.gov.dwp.uc.pairtest.exception.NoTicketsOrderedException;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type.*;

public class CinemaTicketsConsoleAdaptorTest {

    public static final String REQUESTED_NR_TICKETS_ADULT_STRING = "1";
    public static final int REQUESTED_NR_TICKETS_ADULT = 1;
    public static final TicketTypeRequest ADULT_TICKET_TYPE_REQUEST = new TicketTypeRequest(ADULT, REQUESTED_NR_TICKETS_ADULT);
    public static final String REQUESTED_NR_TICKETS_CHILD_STRING = "2";
    public static final int REQUESTED_NR_TICKETS_CHILD = 2;
    public static final TicketTypeRequest CHILD_TICKET_TYPE_REQUEST = new TicketTypeRequest(CHILD, REQUESTED_NR_TICKETS_CHILD);
    public static final String REQUESTED_NR_TICKETS_INFANT_STRING = "3";
    public static final List<String> REQUESTED_TICKET_NUMBERS = List.of(REQUESTED_NR_TICKETS_ADULT_STRING, REQUESTED_NR_TICKETS_CHILD_STRING, REQUESTED_NR_TICKETS_INFANT_STRING);
    public static final int REQUESTED_NR_TICKETS_INFANT = 3;
    public static final TicketOrderRequest TICKET_ORDER_REQUEST = new TicketOrderRequest(REQUESTED_NR_TICKETS_ADULT, REQUESTED_NR_TICKETS_CHILD, REQUESTED_NR_TICKETS_INFANT);
    public static final TicketTypeRequest INFANT_TICKET_TYPE_REQUEST = new TicketTypeRequest(INFANT, REQUESTED_NR_TICKETS_INFANT);
    public static final List<TicketTypeRequest> TICKET_TYPE_REQUESTS_LIST = List.of(ADULT_TICKET_TYPE_REQUEST, CHILD_TICKET_TYPE_REQUEST, INFANT_TICKET_TYPE_REQUEST);
    public static final long ACCOUNT_NUMBER = 001L;

    private final TicketService ticketService = mock(TicketService.class);
    private final TicketTypeRequestFactory ticketTypeRequestFactory = mock(TicketTypeRequestFactory.class);
    private final CinemaTicketsConsoleAdaptor underTest = new CinemaTicketsConsoleAdaptor(ticketService, ticketTypeRequestFactory);

    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();

    @Before
    public void setup() {
        System.setOut(new PrintStream(outputStreamCaptor));
        when(ticketTypeRequestFactory.createListOfTicketRequests(any(TicketOrderRequest.class))).thenReturn(TICKET_TYPE_REQUESTS_LIST);
    }

    @After
    public void tearDown() {
        System.setOut(standardOut);
    }

    @Test
    public void shouldCallTicketTypeRequestFactoryToGetTicketTypeRequests() {
        underTest.processPurchase(ACCOUNT_NUMBER, REQUESTED_TICKET_NUMBERS);

        verify(ticketTypeRequestFactory).createListOfTicketRequests(TICKET_ORDER_REQUEST);
    }

    @Test
    public void shouldCallTicketServiceWithTheRequestedTicketTypeRequests() {
        underTest.processPurchase(ACCOUNT_NUMBER, REQUESTED_TICKET_NUMBERS);

        verify(ticketService).purchaseTickets(ACCOUNT_NUMBER, ADULT_TICKET_TYPE_REQUEST, CHILD_TICKET_TYPE_REQUEST, INFANT_TICKET_TYPE_REQUEST);
    }

    @Test
    public void handlesExceptionWhenAccountNumberIsInvalidAndPrintsOutResponse() {
        doThrow(new InvalidAccountNumberException())
            .when(ticketService)
            .purchaseTickets(ACCOUNT_NUMBER, ADULT_TICKET_TYPE_REQUEST, CHILD_TICKET_TYPE_REQUEST, INFANT_TICKET_TYPE_REQUEST);

        underTest.processPurchase(ACCOUNT_NUMBER, REQUESTED_TICKET_NUMBERS);

        verify(ticketService).purchaseTickets(ACCOUNT_NUMBER, ADULT_TICKET_TYPE_REQUEST, CHILD_TICKET_TYPE_REQUEST, INFANT_TICKET_TYPE_REQUEST);
        assertThat(outputStreamCaptor.toString().trim(), Is.is(String.format("Tickets ordering failed - invalid accountNumber=%s", ACCOUNT_NUMBER)));
    }

    @Test
    public void handlesExceptionWhenMaximumNumberOfTicketsIsExceededAndPrintsOutResponse() {
        int maximumAllowed = 2;
        int nrOrdered = 3;
        doThrow(new MaximumNrOfTicketsAllowedExceededException(maximumAllowed, nrOrdered))
            .when(ticketService)
            .purchaseTickets(ACCOUNT_NUMBER, ADULT_TICKET_TYPE_REQUEST, CHILD_TICKET_TYPE_REQUEST, INFANT_TICKET_TYPE_REQUEST);

        underTest.processPurchase(ACCOUNT_NUMBER, REQUESTED_TICKET_NUMBERS);

        verify(ticketService).purchaseTickets(ACCOUNT_NUMBER, ADULT_TICKET_TYPE_REQUEST, CHILD_TICKET_TYPE_REQUEST, INFANT_TICKET_TYPE_REQUEST);
        assertThat(outputStreamCaptor.toString().trim(), Is.is(
            String.format("Tickets ordering failed - too many tickets requested maximumAllowed=%s numberOrdered=%s", maximumAllowed, nrOrdered))
        );
    }

    @Test
    public void handlesExceptionWhenNoTicketsAreOrderedAndPrintsOutResponse() {
        doThrow(new NoTicketsOrderedException())
            .when(ticketService)
            .purchaseTickets(ACCOUNT_NUMBER, ADULT_TICKET_TYPE_REQUEST, CHILD_TICKET_TYPE_REQUEST, INFANT_TICKET_TYPE_REQUEST);

        underTest.processPurchase(ACCOUNT_NUMBER, REQUESTED_TICKET_NUMBERS);

        verify(ticketService).purchaseTickets(ACCOUNT_NUMBER, ADULT_TICKET_TYPE_REQUEST, CHILD_TICKET_TYPE_REQUEST, INFANT_TICKET_TYPE_REQUEST);
        assertThat(outputStreamCaptor.toString().trim(), Is.is("Tickets ordering failed - no tickets were ordered"));
    }

    @Test
    public void handlesExceptionWhenNoAdultTicketsAreOrderedAndPrintsOutResponse() {
        when(ticketTypeRequestFactory.createListOfTicketRequests(any(TicketOrderRequest.class))).thenReturn(List.of(CHILD_TICKET_TYPE_REQUEST,
                                                                                                                    INFANT_TICKET_TYPE_REQUEST));
        doThrow(new NoAdultTicketsRequestedException())
            .when(ticketService)
            .purchaseTickets(ACCOUNT_NUMBER, CHILD_TICKET_TYPE_REQUEST, INFANT_TICKET_TYPE_REQUEST);

        underTest.processPurchase(ACCOUNT_NUMBER, REQUESTED_TICKET_NUMBERS);

        verify(ticketService).purchaseTickets(ACCOUNT_NUMBER, CHILD_TICKET_TYPE_REQUEST, INFANT_TICKET_TYPE_REQUEST);
        assertThat(outputStreamCaptor.toString().trim(), Is.is("Tickets ordering failed - no adult tickets were ordered"));
    }
}
