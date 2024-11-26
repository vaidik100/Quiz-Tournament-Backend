package cs.sahil.QuizAPIBackend.controller;

import cs.sahil.QuizAPIBackend.model.Score;
import cs.sahil.QuizAPIBackend.model.User;
import cs.sahil.QuizAPIBackend.services.UserService;
import cs.sahil.QuizAPIBackend.services.EmailService;
import cs.sahil.QuizAPIBackend.services.ScoreService;
import cs.sahil.QuizAPIBackend.utils.JwtUtils; // Utility to generate JWT tokens
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private ScoreService scoreService;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private PasswordEncoder passwordEncoder;

    // Register new user
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User user) {
        userService.registerUser(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        try {
            String username = loginRequest.get("username");
            String password = loginRequest.get("password");

            System.out.println("Login attempt for username: " + username);

            User user = userService.findByUsername(username).orElse(null);
            if (user == null) {
                System.out.println("User not found");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
            }

            boolean passwordMatches = passwordEncoder.matches(password, user.getPassword());
            System.out.println("Password match result: " + passwordMatches);

            if (!passwordMatches) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
            }

            String token = jwtUtils.generateJwtToken(user.getUsername(),
                    user.getRoles().stream().toList());

            System.out.println("Generated JWT Token: " + token);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("role", user.getRoles().stream().findFirst().orElse("PLAYER"));
            response.put("username", user.getUsername());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace(); // Log exception details
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred during login");
        }
    }


    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Principal principal) {
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
        }

        User user = userService.findByUsername(principal.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }

        // Return a simplified response to avoid exposing sensitive data
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("username", user.getUsername());
        profile.put("email", user.getEmail());
        profile.put("firstName", user.getFirstName());
        profile.put("lastName", user.getLastName());
        profile.put("pictureUrl", user.getPictureUrl());
        profile.put("address", user.getAddress());
        profile.put("phoneNumber", user.getPhoneNumber());
        profile.put("bio", user.getBio());

        return ResponseEntity.ok(profile);
    }



    // Update user profile
    @PutMapping("/profile/update")
    public ResponseEntity<?> updateProfile(Principal principal, @Valid @RequestBody Map<String, String> updates) {
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
        }

        User user = userService.findByUsername(principal.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }

        // Apply updates (ensure fields are updated only if present in request)
        updates.forEach((key, value) -> {
            switch (key) {
                case "firstName" -> user.setFirstName(value);
                case "lastName" -> user.setLastName(value);
                case "email" -> user.setEmail(value);
                case "username" -> user.setUsername(value);
                case "pictureUrl" -> user.setPictureUrl(value);
                case "address" -> user.setAddress(value);
                case "phoneNumber" -> user.setPhoneNumber(value);
                case "bio" -> user.setBio(value);
            }
        });

        userService.updateUserProfile(user);
        return ResponseEntity.ok("Profile updated successfully.");
    }

    // Player: View participated tournaments
    @GetMapping("/participated-tournaments")
    public ResponseEntity<?> getParticipatedTournaments(Principal principal) {
        // Check if the user is authenticated
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
        }

        // Fetch user details
        User user = userService.findByUsername(principal.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }

        // Fetch scores for the user
        List<Score> scores = scoreService.getScoresByUser(user.getId());
        if (scores.isEmpty()) {
            return ResponseEntity.ok("You have not participated in any tournaments yet.");
        }

        // Prepare a detailed response with explicit type casting
        List<Map<String, Object>> response = scores.stream()
                .map(score -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("tournamentId", score.getQuizTournament().getId());
                    map.put("tournamentName", score.getQuizTournament().getName());
                    map.put("score", score.getScore());
                    map.put("totalQuestions", score.getQuizTournament().getQuestions().size());
                    map.put("completedDate", score.getCompletedDate());
                    return map;
                })
                .toList();

        return ResponseEntity.ok(response);
    }



    // Request password reset
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        User user = userService.findByEmail(email);
        if (user != null) {
            emailService.initiatePasswordReset(email);
            return ResponseEntity.ok("Password reset instructions have been sent to your email.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with the provided email does not exist.");
        }
    }

    // Endpoint to reset the password
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        User user = userService.findByResetToken(token);
        if (user != null) {
            userService.updatePassword(user, newPassword);
            return ResponseEntity.ok("Your password has been reset successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired password reset token.");
        }
    }
}
