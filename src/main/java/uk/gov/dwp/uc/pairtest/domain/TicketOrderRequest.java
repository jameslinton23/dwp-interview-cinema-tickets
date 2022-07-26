package uk.gov.dwp.uc.pairtest.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class TicketOrderRequest {

    private final Integer requestedNrTicketsForAdult;
    private final Integer requestedNrTicketsForChild;
    private final Integer requestedNrTicketsForInfant;

    public TicketOrderRequest(Integer requestedNrTicketsAdult,
                              Integer requestedNrTicketsChild,
                              Integer requestedNrTicketsInfant) {
        this.requestedNrTicketsForAdult = requestedNrTicketsAdult;
        this.requestedNrTicketsForChild = requestedNrTicketsChild;
        this.requestedNrTicketsForInfant = requestedNrTicketsInfant;
    }

    public Integer getRequestedNrTicketsForAdult() {
        return requestedNrTicketsForAdult;
    }

    public Integer getRequestedNrTicketsForChild() {
        return requestedNrTicketsForChild;
    }

    public Integer getRequestedNrTicketsForInfant() {
        return requestedNrTicketsForInfant;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
