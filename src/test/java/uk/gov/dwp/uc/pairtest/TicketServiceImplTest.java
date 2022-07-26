package uk.gov.dwp.uc.pairtest;

import org.junit.Test;

import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidAccountNumberException;
import uk.gov.dwp.uc.pairtest.exception.MaximumNrOfTicketsAllowedExceededException;
import uk.gov.dwp.uc.pairtest.exception.NoAdultTicketsRequestedException;
import uk.gov.dwp.uc.pairtest.exception.NoTicketsOrderedException;

import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationService;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;
import static uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type.*;

public class TicketServiceImplTest {

    public static final long ACCOUNT_ID = 1L;
    public static final int NR_OF_ADULT_TICKETS = 2;
    public static final int NR_OF_CHILD_TICKETS = 3;
    public static final int NR_OF_INFANT_TICKETS = 1;

    private final SeatReservationService seatReservationService = mock(SeatReservationService.class);
    private final TicketPaymentServiceImpl ticketPaymentService = mock(TicketPaymentServiceImpl.class);
    private final TicketServiceImpl underTest = new TicketServiceImpl(seatReservationService, ticketPaymentService);

    @Test
    public void shouldReserveCorrectNumberOfSeatsForAdultTicketTypes() {
        TicketTypeRequest ticketTypeRequest = new TicketTypeRequest(ADULT, NR_OF_ADULT_TICKETS);

        underTest.purchaseTickets(ACCOUNT_ID, ticketTypeRequest);

        verify(seatReservationService).reserveSeat(ACCOUNT_ID, NR_OF_ADULT_TICKETS);
    }

    @Test
    public void shouldReserveCorrectNumberOfSeatsForAdultAndChildrenTicketTypes() {
        TicketTypeRequest adultRequest = new TicketTypeRequest(ADULT, NR_OF_ADULT_TICKETS);
        TicketTypeRequest childRequest = new TicketTypeRequest(CHILD, NR_OF_CHILD_TICKETS);

        underTest.purchaseTickets(ACCOUNT_ID, adultRequest, childRequest);

        verify(seatReservationService).reserveSeat(ACCOUNT_ID, NR_OF_ADULT_TICKETS + NR_OF_CHILD_TICKETS);
    }

    @Test
    public void shouldNotIncludeInfantTicketsInTheSeatReservations() {
        TicketTypeRequest adultRequest = new TicketTypeRequest(ADULT, NR_OF_ADULT_TICKETS);
        TicketTypeRequest childRequest = new TicketTypeRequest(CHILD, NR_OF_CHILD_TICKETS);
        TicketTypeRequest infantRequest = new TicketTypeRequest(INFANT, NR_OF_INFANT_TICKETS);

        underTest.purchaseTickets(ACCOUNT_ID, adultRequest, childRequest, infantRequest);

        verify(seatReservationService).reserveSeat(ACCOUNT_ID, NR_OF_ADULT_TICKETS + NR_OF_CHILD_TICKETS);
    }

    @Test(expected = MaximumNrOfTicketsAllowedExceededException.class)
    public void shouldThrowInvalidPurchaseExceptionWhenNumberOfRequestedReservedTicketsIsGreaterThanTheMaximumAllowed() {
        int nrOfAdultTickets = 8;
        int nrOfChildTickets = 10;
        int nrOfInfantTickets = 3;
        int expectedAmount =
            (nrOfAdultTickets * ADULT.getTicketCost()) + (nrOfChildTickets * CHILD.getTicketCost()) + (nrOfInfantTickets * INFANT.getTicketCost());

        TicketTypeRequest adultRequest = new TicketTypeRequest(ADULT, nrOfAdultTickets);
        TicketTypeRequest childRequest = new TicketTypeRequest(CHILD, nrOfChildTickets);
        TicketTypeRequest infantRequest = new TicketTypeRequest(INFANT, nrOfInfantTickets);

        underTest.purchaseTickets(ACCOUNT_ID, adultRequest, childRequest, infantRequest);

        verifyNoCallsMadeToPaymentAndReservationServices(nrOfAdultTickets + nrOfChildTickets, expectedAmount);
    }

    @Test
    public void shouldNotThrowInvalidPurchaseExceptionWhenNumberOfRequestedReservedTicketsIsAtTheMaximumAllowed() {
        int nrOfAdultTickets = 8;
        int nrOfChildTickets = 9;
        int nrOfInfantTickets = 3;

        TicketTypeRequest adultRequest = new TicketTypeRequest(ADULT, nrOfAdultTickets);
        TicketTypeRequest childRequest = new TicketTypeRequest(CHILD, nrOfChildTickets);
        TicketTypeRequest infantRequest = new TicketTypeRequest(INFANT, nrOfInfantTickets);

        underTest.purchaseTickets(ACCOUNT_ID, adultRequest, childRequest, infantRequest);

        verify(seatReservationService).reserveSeat(ACCOUNT_ID, nrOfAdultTickets + nrOfChildTickets);
    }

    @Test
    public void shouldCalculateCorrectPaymentAmountForTicketPaymentService() {
        int nrOfInfantTickets = 1;
        int expectedAmount =
            (NR_OF_ADULT_TICKETS * ADULT.getTicketCost()) + (NR_OF_CHILD_TICKETS * CHILD.getTicketCost()) + (nrOfInfantTickets * INFANT.getTicketCost());

        TicketTypeRequest adultRequest = new TicketTypeRequest(ADULT, NR_OF_ADULT_TICKETS);
        TicketTypeRequest childRequest = new TicketTypeRequest(CHILD, NR_OF_CHILD_TICKETS);
        TicketTypeRequest infantRequest = new TicketTypeRequest(INFANT, nrOfInfantTickets);

        underTest.purchaseTickets(ACCOUNT_ID, adultRequest, childRequest, infantRequest);

        verify(ticketPaymentService).makePayment(ACCOUNT_ID, expectedAmount);
    }

    @Test(expected = NoAdultTicketsRequestedException.class)
    public void shouldThrowNoAdultTicketsRequestedExceptionWhenZeroAdultTicketsIsRequested() {
        TicketTypeRequest adultRequest = new TicketTypeRequest(ADULT, 0);
        TicketTypeRequest childRequest = new TicketTypeRequest(CHILD, NR_OF_CHILD_TICKETS);
        TicketTypeRequest infantRequest = new TicketTypeRequest(INFANT, NR_OF_INFANT_TICKETS);
        int expectedAmount =
            adultRequest.getTotalCostForTicketType() * childRequest.getTotalCostForTicketType()
            * infantRequest.getTotalCostForTicketType();

        underTest.purchaseTickets(ACCOUNT_ID, adultRequest, childRequest, infantRequest);

        verifyNoCallsMadeToPaymentAndReservationServices(NR_OF_ADULT_TICKETS, expectedAmount);
    }

    @Test(expected = NoAdultTicketsRequestedException.class)
    public void shouldThrowNoAdultTicketsRequestedExceptionWhenNoAdultTicketIsRequested() {
        TicketTypeRequest childRequest = new TicketTypeRequest(CHILD, NR_OF_CHILD_TICKETS);
        TicketTypeRequest infantRequest = new TicketTypeRequest(INFANT, NR_OF_INFANT_TICKETS);
        int expectedAmount = childRequest.getTotalCostForTicketType() * infantRequest.getTotalCostForTicketType();

        underTest.purchaseTickets(ACCOUNT_ID, childRequest, infantRequest);

        verifyNoCallsMadeToPaymentAndReservationServices(NR_OF_ADULT_TICKETS, expectedAmount);
    }

    @Test(expected = InvalidAccountNumberException.class)
    public void shouldThrowInvalidAccountNumberExceptionWhenAccountIdIsZero() {
        TicketTypeRequest adultRequest = new TicketTypeRequest(ADULT, NR_OF_ADULT_TICKETS);
        int expectedAmount = adultRequest.getTotalCostForTicketType();

        underTest.purchaseTickets(0L, adultRequest);

        verifyNoCallsMadeToPaymentAndReservationServices(NR_OF_ADULT_TICKETS, expectedAmount);
    }

    @Test(expected = InvalidAccountNumberException.class)
    public void shouldThrowInvalidAccountNumberExceptionWhenAccountIdIsNull() {
        TicketTypeRequest adultRequest = new TicketTypeRequest(ADULT, NR_OF_ADULT_TICKETS);
        int expectedAmount = adultRequest.getTicketType().getTicketCost() * adultRequest.getNoOfTickets();

        underTest.purchaseTickets(null, adultRequest);

        verifyNoCallsMadeToPaymentAndReservationServices(NR_OF_ADULT_TICKETS, expectedAmount);
    }

    @Test(expected = InvalidAccountNumberException.class)
    public void shouldThrowInvalidAccountNumberExceptionWhenAccountIdIsNegative() {
        TicketTypeRequest adultRequest = new TicketTypeRequest(ADULT, NR_OF_ADULT_TICKETS);
        int expectedAmount = adultRequest.getTicketType().getTicketCost() * adultRequest.getNoOfTickets();

        underTest.purchaseTickets(-1L, adultRequest);

        verifyNoCallsMadeToPaymentAndReservationServices(NR_OF_ADULT_TICKETS, expectedAmount);
    }

    @Test(expected = NoTicketsOrderedException.class)
    public void shouldThrowNoTicketsOrderedExceptionWhenNoTicketRequests() {
        int expectedAmount = 0;

        underTest.purchaseTickets(ACCOUNT_ID);

        verifyNoCallsMadeToPaymentAndReservationServices(NR_OF_ADULT_TICKETS, expectedAmount);
    }

    private void verifyNoCallsMadeToPaymentAndReservationServices(int nrOfAdultTickets, int expectedAmount) {
        verify(seatReservationService, never()).reserveSeat(ACCOUNT_ID, nrOfAdultTickets);
        verify(ticketPaymentService, never()).makePayment(ACCOUNT_ID, expectedAmount);
    }
}
