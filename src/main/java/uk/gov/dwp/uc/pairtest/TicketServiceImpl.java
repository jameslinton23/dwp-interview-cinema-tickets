package uk.gov.dwp.uc.pairtest;

import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidAccountNumberException;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import uk.gov.dwp.uc.pairtest.exception.MaximumNrOfTicketsAllowedExceededException;
import uk.gov.dwp.uc.pairtest.exception.NoAdultTicketsRequestedException;
import uk.gov.dwp.uc.pairtest.exception.NoTicketsOrderedException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationService;

import static uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type.*;


public class TicketServiceImpl implements TicketService {

    public static final int MAXIMUM_NR_OF_TICKERS_THAT_CAN_BE_PURCHASED = 20;

    private final SeatReservationService seatReservationService;
    private final TicketPaymentServiceImpl ticketPaymentService;

    public TicketServiceImpl(SeatReservationService seatReservationService, TicketPaymentServiceImpl ticketPaymentService) {
        this.seatReservationService = seatReservationService;
        this.ticketPaymentService = ticketPaymentService;
    }

    /**
     * Should only have private methods other than the one below.
     */
    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        List<TicketTypeRequest> allTicketRequests = Arrays.stream(ticketTypeRequests).collect(Collectors.toList());
        int totalNrOfTickets = getTotalNrOfTicketsRequested(allTicketRequests);
        throwInvalidPurchaseExceptionIfNotAValidTicketRequest(accountId, totalNrOfTickets, allTicketRequests);
        int totalNrOfReservations = getTotalNrOfReservedSeatsRequired(allTicketRequests);
        int totalCostOfTickets = calculateTotalCostOfTickets(allTicketRequests);
        reserveSeatsAndPurchaseTickets(accountId, totalNrOfReservations, totalNrOfTickets, totalCostOfTickets);
    }

    private int getTotalNrOfTicketsRequested(List<TicketTypeRequest> requests) {
        return requests.stream()
            .map(TicketTypeRequest::getNoOfTickets)
            .mapToInt(Integer::intValue)
            .sum();
    }

    private int getTotalNrOfReservedSeatsRequired(List<TicketTypeRequest> requests) {
        return requests.stream()
            .filter(ticketTypeRequest -> ticketTypeRequest.getTicketType() != INFANT)
            .map(TicketTypeRequest::getNoOfTickets)
            .mapToInt(Integer::intValue)
            .sum();
    }

    private int calculateTotalCostOfTickets(List<TicketTypeRequest> requests) {
        return requests.stream()
            .map(TicketTypeRequest::getTotalCostForTicketType)
            .mapToInt(Integer::intValue)
            .sum();
    }

    private boolean checkContainsAtLeastOneAdultTicket(List<TicketTypeRequest> allTicketRequests) {
        List<TicketTypeRequest> adultTicketTypeRequest = allTicketRequests.stream()
            .filter(ticketTypeRequest -> ticketTypeRequest.getTicketType() == ADULT)
            .filter(ticketTypeRequest -> ticketTypeRequest.getNoOfTickets() > 0)
            .collect(Collectors.toList());

        if (adultTicketTypeRequest.isEmpty()) {
            return true;
        }

        return false;
    }

    private void throwInvalidPurchaseExceptionIfNotAValidTicketRequest(Long accountId, int totalNrOfTickets, List<TicketTypeRequest> allTicketRequests) {
        if (totalNrOfTickets == 0) {
            throw new NoTicketsOrderedException();
        }

        if (checkContainsAtLeastOneAdultTicket(allTicketRequests)) {
            throw new NoAdultTicketsRequestedException();
        }

        if ((accountId == null) || (accountId < 1)) {
            throw new InvalidAccountNumberException();
        }

        if (totalNrOfTickets > MAXIMUM_NR_OF_TICKERS_THAT_CAN_BE_PURCHASED) {
            throw new MaximumNrOfTicketsAllowedExceededException(MAXIMUM_NR_OF_TICKERS_THAT_CAN_BE_PURCHASED, totalNrOfTickets);
        }
    }

    private void reserveSeatsAndPurchaseTickets(Long accountId, int totalNrOfReservations, int totalNrOfTickets, int totalCostOfTickets) {
        System.out.println(String.format("Purchasing %s tickets with %s seats at a cost of £%s for accountId=%s",
                                         totalNrOfTickets, totalNrOfReservations, totalCostOfTickets, accountId));
        seatReservationService.reserveSeat(accountId, totalNrOfReservations);
        ticketPaymentService.makePayment(accountId, totalCostOfTickets);
    }
}
