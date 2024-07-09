package com.db.awmd.challenge.domain;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class TransferRequestDTO {
    @NotEmpty
    private String accountFromId;

    @NotEmpty
    private String accountToId;

    @NotNull
    @DecimalMin(value = "0.01", message = "The amount must be positive")
    private BigDecimal amount;
}