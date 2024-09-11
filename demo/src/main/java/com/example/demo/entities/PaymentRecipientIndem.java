package com.example.demo.entities;

import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@Data
@Builder
@NoArgsConstructor
public class PaymentRecipientIndem {
    private String recipientName;
    private String accountNumber;
    @Setter
    private BigDecimal amount;

}
