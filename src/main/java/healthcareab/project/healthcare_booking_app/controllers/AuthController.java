package healthcareab.project.healthcare_booking_app.controllers;

import healthcareab.project.healthcare_booking_app.dto.AuthRequest;
import healthcareab.project.healthcare_booking_app.dto.AuthResponse;
import healthcareab.project.healthcare_booking_app.dto.RegisterRequest;
import healthcareab.project.healthcare_booking_app.dto.RegisterResponse;
import healthcareab.project.healthcare_booking_app.factories.UserFactory;
import healthcareab.project.healthcare_booking_app.models.Employee;
import healthcareab.project.healthcare_booking_app.models.Patient;
import healthcareab.project.healthcare_booking_app.models.Role;
import healthcareab.project.healthcare_booking_app.models.User;
import healthcareab.project.healthcare_booking_app.services.AuthService;
import healthcareab.project.healthcare_booking_app.utils.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final AuthService authService;
    private final UserFactory userFactory;


    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, AuthService authService, UserFactory userFactory) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.authService = authService;
        this.userFactory = userFactory;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        if(authService.existsByUsername(registerRequest.getUsername())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Username already exists.");
        }

        Role role = (registerRequest.getRoles() != null && !registerRequest.getRoles().isEmpty())
                ? registerRequest.getRoles().iterator().next()
                : Role.PATIENT;

                User user = userFactory.createUser(
                role,
                registerRequest.getUsername(),
                registerRequest.getPassword(),
                registerRequest.getEmail()
        );

        user.setFirstName(registerRequest.getFirstName());
        user.setLastName(registerRequest.getLastName());

        if (user instanceof Patient patient) {
            patient.setPhoneNumber(registerRequest.getPhoneNumber());
            patient.setDateOfBirth(registerRequest.getDateOfBirth());
        } else if (user instanceof Employee employee) {
            employee.setEmployeeNumber(registerRequest.getEmployeeNumber());
            employee.setSpecialization(registerRequest.getSpecialization());
            employee.setDepartment(registerRequest.getDepartment());
        }

        authService.registerUser(user);

        RegisterResponse response = new RegisterResponse(
                "User registered successfully",
                user.getUsername(),
                user.getRoles(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login (@Valid @RequestBody AuthRequest authRequest, HttpServletResponse response) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getUsername(),
                            authRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            String jwt = jwtUtil.generateToken(userDetails);
            ResponseCookie jwtCookie = ResponseCookie.from("jwt", jwt)
                    .httpOnly(true) // prevents javascript to get cookie
                    .secure(false) //IMPORTANT TO CHANGE IN PRODUCTION TO TRUE
                    .path("/")  // cookies is available in all application
                    .maxAge(10 * 60 * 60) // valid for 10h
                    .sameSite("Strict") // Lax & None
                    .build();

            AuthResponse authResponse = new AuthResponse(
                    "Login successful",
                    userDetails.getUsername(),
                    authService.findByUsername(userDetails.getUsername()).getRoles(),
                    authService.findByUsername(userDetails.getUsername()).getEmail(),
                    authService.findByUsername(userDetails.getUsername()).getFirstName(),
                    authService.findByUsername(userDetails.getUsername()).getLastName(),
                    authService.findByUsername(userDetails.getUsername()).getAddress()

            );

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                    .body(authResponse);

        } catch (AuthenticationException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Incorrect username or password");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        ResponseCookie jwtCookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(false) // VIKTIGT! ändra i production
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        SecurityContextHolder.clearContext();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body("Logout successful!");
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated!");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = authService.findByUsername(userDetails.getUsername());

        return ResponseEntity.ok(new AuthResponse(
                "Authenticated",
                user.getUsername(),
                user.getRoles(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getAddress()
        ));
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = authService.findByUsername(userDetails.getUsername());

        AuthResponse profileResponse = new AuthResponse(
                "Profile fetched successfully",
                user.getUsername(),
                user.getRoles(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getAddress()
        );
        return ResponseEntity.ok(profileResponse);
    }
}
