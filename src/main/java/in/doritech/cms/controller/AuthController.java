
package in.doritech.cms.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.doritech.cms.request.LoginRequest;
import in.doritech.cms.service.AuthService;
import in.doritech.cms.util.ApiResponse;

@RestController
@RequestMapping("/api/cms/auth")
@CrossOrigin(allowedHeaders = "*")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/login")
	public ApiResponse login(@RequestBody LoginRequest request) {
		return authService.login(request);
	}
}
