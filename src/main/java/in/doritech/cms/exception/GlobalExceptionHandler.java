package in.doritech.cms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import in.doritech.cms.util.ApiResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse> handleResponseStatusException(
            ResponseStatusException ex) {

        HttpStatus status = HttpStatus.valueOf(
                ex.getStatusCode().value()
        );

        ApiResponse response = new ApiResponse(
                ex.getReason(),
                null,
                status.value()
        );

        return ResponseEntity.status(status).body(response);
    }
}