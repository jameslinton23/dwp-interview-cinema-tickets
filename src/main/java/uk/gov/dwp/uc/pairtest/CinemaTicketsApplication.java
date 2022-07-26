package uk.gov.dwp.uc.pairtest;

import uk.gov.dwp.uc.pairtest.adaptors.CinemaTicketsConsoleAdaptor;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequestFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationServiceImpl;

public final class CinemaTicketsApplication {

    private CinemaTicketsApplication() {
    }

    public static void main(String[] args) {
        List<String> arguments = createArgumentsList(args);
        long accountNumber = Long.parseLong(arguments.get(0));
        List<String> requestedTicketNumbers = List.of(arguments.get(1),
                                                      arguments.get(2),
                                                      arguments.get(3));
        CinemaTicketsConsoleAdaptor cinemaTicketsConsole = createCinemaTicketsConsoleWithDependencies();
        cinemaTicketsConsole.processPurchase(accountNumber, requestedTicketNumbers);
    }

    private static List<String> createArgumentsList(String[] args) {
        List<String> arguments = Arrays.stream(args)
            .collect(Collectors.toList());
        if (arguments.size() != 4) {
            throw new IllegalStateException("Must provide 4 arguments - account number, number of adult tickets, "
                                            + "number of child tickets and number of infant tickets");
        }
        return arguments;
    }

    private static CinemaTicketsConsoleAdaptor createCinemaTicketsConsoleWithDependencies() {
        TicketService ticketService = new TicketServiceImpl(new SeatReservationServiceImpl(), new TicketPaymentServiceImpl());
        return new CinemaTicketsConsoleAdaptor(ticketService, new TicketTypeRequestFactory());
    }
}
