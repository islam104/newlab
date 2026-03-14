package com.recruting.demo.license.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TicketResponse {
    Ticket ticket;
    String signature;
}
