package com.example.demo.entities;

import com.example.demo.event.IndemnisationValidated;
import com.example.demo.entities.BeneficiaryType;
import com.example.demo.entities.PaymentRecipientIndem;
import com.example.demo.entities.StatusEnum;
import com.example.demo.event.IndemnisationCreated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "indemnisations")
public class Indemnisation {

    @Id
    private String ref;
    private BeneficiaryType type;
    private BigDecimal amount;
    private List<PaymentRecipientIndem> paymentRecipients;
    private Integer paymentDay;
    private StatusEnum status ;
    private Long indemnisationVersion;


    public void on(IndemnisationCreated event) {
        setStatus(event.getStatus());
        setRef(event.getRef().toString());
        setAmount(event.getAmount());
        setType(event.getType());
        setPaymentDay(event.getPaymentDay());
        setPaymentRecipients(event.getPaymentRecipients());
        setIndemnisationVersion(event.getVersion());
    }
    public void apply(IndemnisationValidated event) {
        this.status = event.getStatus();
    }
}
