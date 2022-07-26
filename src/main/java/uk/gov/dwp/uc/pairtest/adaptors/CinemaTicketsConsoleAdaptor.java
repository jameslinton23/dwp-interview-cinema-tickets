package uk.gov.dwp.uc.pairtest.adaptors;

import uk.gov.dwp.uc.pairtest.TicketService;
import uk.gov.dwp.uc.pairtest.domain.TicketOrderRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequestFactory;
import uk.gov.dwp.uc.pairtest.exception.InvalidAccountNumberException;
import uk.gov.dwp.uc.pairtest.exception.MaximumNrOfTicketsAllowedExceededException;
import uk.gov.dwp.uc.pairtest.exception.NoAdultTicketsRequestedException;
import uk.gov.dwp.uc.pairtest.exception.NoTicketsOrderedException;

import java.util.List;
import java.util.stream.Collectors;

public class CinemaTicketsConsoleAdaptor {

    private final TicketService ticketService;
    private final TicketTypeRequestFactory ticketTypeRequestFactory;

    public CinemaTicketsConsoleAdaptor(TicketService ticketService, TicketTypeRequestFactory ticketTypeRequestFactory) {
        this.ticketService = ticketService;
        this.ticketTypeRequestFactory = ticketTypeRequestFactory;
    }

    public void processPurchase(long accountNumber, List<String> requestedTicketNumbers) {
        TicketOrderRequest ticketOrderRequest = createTicketOrderRequest(requestedTicketNumbers);
        List<TicketTypeRequest> ticketTypeRequests = ticketTypeRequestFactory.createListOfTicketRequests(ticketOrderRequest);
        tryToPurchaseTickets(accountNumber, ticketTypeRequests);
    }

    private TicketOrderRequest createTicketOrderRequest(List<String> requestedTicketNumbersStrings) {
        List<Integer> requestedTicketNumbers = requestedTicketNumbersStrings.stream()
            .map(Integer::parseInt)
            .collect(Collectors.toList());
        return new TicketOrderRequest(requestedTicketNumbers.get(0), requestedTicketNumbers.get(1), requestedTicketNumbers.get(2));
    }

    private void tryToPurchaseTickets(long accountNumber, List<TicketTypeRequest> ticketTypeRequests) {
        try {
            TicketTypeRequest[] ticketArray = ticketTypeRequests.toArray(TicketTypeRequest[]::new);
            ticketService.purchaseTickets(accountNumber, ticketArray);
        } catch (InvalidAccountNumberException e) {
            System.out.println(String.format("Tickets ordering failed - invalid accountNumber=%s", accountNumber));
        } catch (MaximumNrOfTicketsAllowedExceededException e) {
            System.out.println(
                String.format("Tickets ordering failed - too many tickets requested maximumAllowed=%s numberOrdered=%s",
                              e.getMaximumAllowed(), e.getNrOfTicketsOrdered()));
        } catch (NoTicketsOrderedException e) {
            System.out.println("Tickets ordering failed - no tickets were ordered");
        } catch (NoAdultTicketsRequestedException e) {
            System.out.println("Tickets ordering failed - no adult tickets were ordered");
        }
    }
}
