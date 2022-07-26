package uk.gov.dwp.uc.pairtest.domain;

import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type;

import java.util.ArrayList;
import java.util.List;

public class TicketTypeRequestFactory {

    public List<TicketTypeRequest> createListOfTicketRequests(TicketOrderRequest ticketOrderRequest) {
        List<TicketTypeRequest> ticketTypeRequests = new ArrayList<>();

        if (hasRequestedTickets(ticketOrderRequest.getRequestedNrTicketsForAdult())) {
            ticketTypeRequests.add(new TicketTypeRequest(Type.ADULT, ticketOrderRequest.getRequestedNrTicketsForAdult()));
        }

        if (hasRequestedTickets(ticketOrderRequest.getRequestedNrTicketsForChild())) {
            ticketTypeRequests.add(new TicketTypeRequest(Type.CHILD, ticketOrderRequest.getRequestedNrTicketsForChild()));
        }

        if (hasRequestedTickets(ticketOrderRequest.getRequestedNrTicketsForInfant())) {
            ticketTypeRequests.add(new TicketTypeRequest(Type.INFANT, ticketOrderRequest.getRequestedNrTicketsForInfant()));
        }

        return ticketTypeRequests;
    }

    private boolean hasRequestedTickets(Integer requestedNrTicketsForAdult) {
        return (requestedNrTicketsForAdult != null) && (requestedNrTicketsForAdult > 0);
    }
}
