package uk.gov.dwp.uc.pairtest;

import org.hamcrest.core.Is;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.gov.dwp.uc.pairtest.adaptors.CinemaTicketsConsoleAdaptor;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequestFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationServiceImpl;

import static org.hamcrest.MatcherAssert.assertThat;

public class CinemaTicketsConsoleAdaptorApplicationTest {

    public static final long ACCOUNT_NUMBER = 1L;

    private final PrintStream standardOut = System.out;
    private final ByteArrayOutputStream outputStreamCaptor = new ByteArrayOutputStream();
    private final TicketService ticketService = new TicketServiceImpl(new SeatReservationServiceImpl(), new TicketPaymentServiceImpl());
    private final TicketTypeRequestFactory ticketTypeRequestFactory = new TicketTypeRequestFactory();
    private final CinemaTicketsConsoleAdaptor underTest = new CinemaTicketsConsoleAdaptor(ticketService, ticketTypeRequestFactory);

    @Before
    public void setup() {
        System.setOut(new PrintStream(outputStreamCaptor));
    }

    @After
    public void tearDown() {
        System.setOut(standardOut);
    }

    @Test
    public void shouldSuccessfullyProcessPaymentWhenRequestIsValid() {
        List<String> requestedTicketNumbers = List.of("1", "2", "0");
        underTest.processPurchase(ACCOUNT_NUMBER, requestedTicketNumbers);
        assertThat(outputStreamCaptor.toString().trim(),
                   Is.is(
                       String.format("Purchasing 3 tickets with 3 seats at a cost of £40 for accountId=%s", ACCOUNT_NUMBER)
                   ));
    }

    @Test
    public void shouldFailToProcessPaymentAndReserveTicketsWhenMoreThanTheMaximumNumberOfTicketsAreRequested() {
        List<String> requestedTicketNumbers = List.of("10", "10", "10");
        int nrOrdered = 30;
        underTest.processPurchase(ACCOUNT_NUMBER, requestedTicketNumbers);
        assertThat(outputStreamCaptor.toString().trim(), Is.is(
            String.format("Tickets ordering failed - too many tickets requested maximumAllowed=%s numberOrdered=%s",
                          TicketServiceImpl.MAXIMUM_NR_OF_TICKERS_THAT_CAN_BE_PURCHASED, nrOrdered))
        );
    }

    @Test
    public void shouldFailToProcessPaymentAndReserveTicketsWhenAccountNumberIsInvalid() {
        List<String> requestedTicketNumbers = List.of("3", "2", "3");
        underTest.processPurchase(-1L, requestedTicketNumbers);
        assertThat(outputStreamCaptor.toString().trim(), Is.is("Tickets ordering failed - invalid accountNumber=-1"));
    }

    @Test
    public void shouldFailToProcessPaymentAndReservationsWhenNoTicketsAreOrdered() {
        List<String> requestedTicketNumbers = List.of("0", "0", "0");
        underTest.processPurchase(ACCOUNT_NUMBER, requestedTicketNumbers);
        assertThat(outputStreamCaptor.toString().trim(), Is.is("Tickets ordering failed - no tickets were ordered"));
    }

    @Test
    public void shouldFailToProcessPaymentAndReservationsWhenNoAdultTicketsAreOrdered() {
        List<String> requestedTicketNumbers = List.of("0", "2", "3");
        underTest.processPurchase(ACCOUNT_NUMBER, requestedTicketNumbers);
        assertThat(outputStreamCaptor.toString().trim(), Is.is("Tickets ordering failed - no adult tickets were ordered"));
    }
}
