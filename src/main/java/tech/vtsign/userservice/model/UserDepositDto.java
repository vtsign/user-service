package tech.vtsign.userservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserDepositDto {
    @NotBlank(message = "method in [CC, ATM, zalopayapp]")
    private String method;
    @NotNull(message = "amount is required")
    @Min(value = 1000, message = "amount must be greater than 1000")
    private Long amount;
}
