package in.doritech.cms.service;

import in.doritech.cms.request.LoginRequest;
import in.doritech.cms.util.ApiResponse;

public interface AuthService {
	ApiResponse login(LoginRequest request);
}