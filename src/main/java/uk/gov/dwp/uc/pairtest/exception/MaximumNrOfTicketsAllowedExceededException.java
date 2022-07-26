package uk.gov.dwp.uc.pairtest.exception;

public class MaximumNrOfTicketsAllowedExceededException extends InvalidPurchaseException {

    private final int maximumAllowed;
    private final int nrOfTicketsOrdered;

    public MaximumNrOfTicketsAllowedExceededException(int maximumAllowed, int nrOfTicketsOrdered) {
        this.maximumAllowed = maximumAllowed;
        this.nrOfTicketsOrdered = nrOfTicketsOrdered;
    }

    public int getMaximumAllowed() {
        return maximumAllowed;
    }

    public int getNrOfTicketsOrdered() {
        return nrOfTicketsOrdered;
    }
}
