package com.taskmanager.backend.controller;

import com.taskmanager.backend.dto.AuthResponse;
import com.taskmanager.backend.dto.LoginRequest;
import com.taskmanager.backend.dto.MessageResponse;
import com.taskmanager.backend.dto.RegisterRequest;
import com.taskmanager.backend.model.Role;
import com.taskmanager.backend.model.User;
import com.taskmanager.backend.repository.RoleRepository;
import com.taskmanager.backend.repository.UserRepository;
import com.taskmanager.backend.service.EmailService;
import com.taskmanager.backend.util.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication and registration endpoints")
public class AuthController {

        private final AuthenticationManager authenticationManager;
        private final UserRepository userRepository;
        private final RoleRepository roleRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtUtils jwtUtils;
        private final EmailService emailService;

        @Operation(summary = "Register a new user", description = "Creates a new user account and sends verification email")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "User registered - verification email sent"),
                        @ApiResponse(responseCode = "400", description = "Username or email already exists")
        })
        @PostMapping("/register")
        public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
                if (userRepository.existsByUsername(registerRequest.getUsername())) {
                        return ResponseEntity.badRequest()
                                        .body(new MessageResponse("Error: Username is already taken!"));
                }

                if (userRepository.existsByEmail(registerRequest.getEmail())) {
                        return ResponseEntity.badRequest()
                                        .body(new MessageResponse("Error: Email is already in use!"));
                }

                // Generate verification token
                String verificationToken = UUID.randomUUID().toString();

                User user = User.builder()
                                .username(registerRequest.getUsername())
                                .email(registerRequest.getEmail())
                                .password(passwordEncoder.encode(registerRequest.getPassword()))
                                .emailVerified(false)
                                .verificationToken(verificationToken)
                                .verificationTokenExpiry(LocalDateTime.now().plusHours(24))
                                .build();

                Set<Role> roles = new HashSet<>();
                Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                                .orElseGet(() -> {
                                        Role newRole = Role.builder().name(Role.RoleName.ROLE_USER).build();
                                        return roleRepository.save(newRole);
                                });
                roles.add(userRole);
                user.setRoles(roles);

                userRepository.save(user);

                // Send verification email
                emailService.sendVerificationEmail(user.getEmail(), user.getUsername(), verificationToken);

                return ResponseEntity.ok(new MessageResponse(
                                "User registered! Please check your email to verify your account."));
        }

        @Operation(summary = "Verify email", description = "Verify user email using token from email link")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Email verified successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid or expired token")
        })
        @GetMapping("/verify-email")
        public ResponseEntity<?> verifyEmail(@RequestParam String token) {
                User user = userRepository.findByVerificationToken(token)
                                .orElse(null);

                if (user == null) {
                        return ResponseEntity.badRequest()
                                        .body(new MessageResponse("Error: Invalid verification token!"));
                }

                if (user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
                        return ResponseEntity.badRequest()
                                        .body(new MessageResponse(
                                                        "Error: Verification token has expired! Please register again."));
                }

                user.setEmailVerified(true);
                user.setVerificationToken(null);
                user.setVerificationTokenExpiry(null);
                userRepository.save(user);

                return ResponseEntity.ok(new MessageResponse("Email verified successfully! You can now login."));
        }

        @Operation(summary = "Resend verification email", description = "Resend verification email to user")
        @PostMapping("/resend-verification")
        public ResponseEntity<?> resendVerification(@RequestParam String email) {
                User user = userRepository.findByEmail(email)
                                .orElse(null);

                if (user == null) {
                        return ResponseEntity.badRequest()
                                        .body(new MessageResponse("Error: User not found!"));
                }

                if (user.getEmailVerified()) {
                        return ResponseEntity.badRequest()
                                        .body(new MessageResponse("Email is already verified!"));
                }

                // Generate new token
                String newToken = UUID.randomUUID().toString();
                user.setVerificationToken(newToken);
                user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(24));
                userRepository.save(user);

                emailService.sendVerificationEmail(user.getEmail(), user.getUsername(), newToken);

                return ResponseEntity.ok(new MessageResponse("Verification email sent!"));
        }

        @Operation(summary = "Authenticate user", description = "Login with username and password to get JWT token")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Authentication successful"),
                        @ApiResponse(responseCode = "401", description = "Invalid credentials or email not verified")
        })
        @PostMapping("/login")
        public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
                // Check if user exists and email is verified
                User user = userRepository.findByUsername(loginRequest.getUsername())
                                .orElse(null);

                if (user == null) {
                        return ResponseEntity.badRequest()
                                        .body(new MessageResponse("Error: Invalid credentials!"));
                }

                if (!user.getEmailVerified()) {
                        return ResponseEntity.badRequest()
                                        .body(new MessageResponse(
                                                        "Error: Please verify your email before logging in!"));
                }

                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(
                                                loginRequest.getUsername(),
                                                loginRequest.getPassword()));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                String jwt = jwtUtils.generateJwtToken(authentication);

                UserDetails userDetails = (UserDetails) authentication.getPrincipal();

                List<String> roles = userDetails.getAuthorities().stream()
                                .map(item -> item.getAuthority())
                                .collect(Collectors.toList());

                return ResponseEntity.ok(AuthResponse.builder()
                                .token(jwt)
                                .type("Bearer")
                                .id(user.getId())
                                .username(user.getUsername())
                                .email(user.getEmail())
                                .roles(roles)
                                .build());
        }
}
