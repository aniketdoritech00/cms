package in.doritech.cms.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import in.doritech.cms.util.ApiResponse;

public class Snippet {
	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<ApiResponse> handleResponseStatusException(ResponseStatusException ex) {
		ApiResponse response = new ApiResponse(ex.getReason(), null, ex.getStatusCode().value());
		return ResponseEntity.status(ex.getStatusCode()).body(response);
	}
}
