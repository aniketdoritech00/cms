
package in.doritech.cms.serviceImpl;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import in.doritech.cms.entity.Employee;
import in.doritech.cms.repository.EmployeeRepository;
import in.doritech.cms.request.LoginRequest;
import in.doritech.cms.response.LoginResponse;
import in.doritech.cms.security.JwtService;
import in.doritech.cms.service.AuthService;
import in.doritech.cms.util.ApiResponse;

@Service
public class AuthServiceImpl implements AuthService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(
            EmployeeRepository employeeRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public ApiResponse login(LoginRequest request) {

        if (request == null
                || request.getEmail() == null
                || request.getPassword() == null
                || request.getEmail().trim().isEmpty()
                || request.getPassword().isEmpty()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email and password are required");
        }

        Employee employee = employeeRepository
                .findByEmail(request.getEmail().trim())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid email or password"));

        if (!passwordEncoder.matches(
                request.getPassword(),
                employee.getPasswordHash())) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password");
        }

        if (!"ACTIVE".equalsIgnoreCase(employee.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Employee account is not active");
        }

        // Generate JWT token
        String token = jwtService.generateToken(employee.getEmployeeId());

        // Create login response
        LoginResponse response = new LoginResponse();

        response.setToken(token);
        response.setTokenType("Bearer");
        response.setEmployeeId(employee.getEmployeeId());
        response.setEmployeeName(employee.getEmployeeName());
        response.setEmail(employee.getEmail());
        response.setMessage("Login successful");

        return new ApiResponse(
                "Login successful",
                response,
                HttpStatus.OK.value());
    }
}
