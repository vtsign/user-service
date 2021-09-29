package tech.vtsign.userservice.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ExceptionResponse {
    private Date timestamp;
    private String message;
    private String details;
    private int status;
}
