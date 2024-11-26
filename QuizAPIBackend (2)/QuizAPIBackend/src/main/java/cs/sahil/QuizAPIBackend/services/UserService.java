package cs.sahil.QuizAPIBackend.services;

import cs.sahil.QuizAPIBackend.model.User;
import cs.sahil.QuizAPIBackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

@Service
public class UserService {

    private static final Logger LOGGER = Logger.getLogger(UserService.class.getName());

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    // Register a new user with default role assignment
    public void registerUser(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username is already taken");
        }

        // Encrypt the password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Assign default role if no roles are provided
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.setRoles(Set.of("PLAYER")); // Default role
        }

        // Save the user
        userRepository.save(user);
    }


    // Find user by username
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User updateUserProfile(User updatedUser) {
        Optional<User> existingUserOpt = userRepository.findById(updatedUser.getId());
        if (existingUserOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        User existingUser = existingUserOpt.get();

        // Update only the fields provided
        if (updatedUser.getFirstName() != null) {
            existingUser.setFirstName(updatedUser.getFirstName());
        }
        if (updatedUser.getLastName() != null) {
            existingUser.setLastName(updatedUser.getLastName());
        }
        if (updatedUser.getUsername() != null) {
            existingUser.setUsername(updatedUser.getUsername());
        }
        if (updatedUser.getEmail() != null) {
            existingUser.setEmail(updatedUser.getEmail());
        }
        if (updatedUser.getPictureUrl() != null) {
            existingUser.setPictureUrl(updatedUser.getPictureUrl());
        }
        if (updatedUser.getAddress() != null) {
            existingUser.setAddress(updatedUser.getAddress());
        }
        if (updatedUser.getPhoneNumber() != null) {
            existingUser.setPhoneNumber(updatedUser.getPhoneNumber());
        }
        if (updatedUser.getBio() != null) {
            existingUser.setBio(updatedUser.getBio());
        }

        // Save the updated user
        User updatedProfile = userRepository.save(existingUser);
        LOGGER.info("User profile updated for: " + updatedProfile.getUsername());
        return updatedProfile;
    }


    // Reset password by email
    public void resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email).orElseThrow(() ->
                new IllegalArgumentException("No user found with email: " + email));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        LOGGER.info("Password reset for user with email: " + email);
    }

    // Find user by email
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    // Find user by reset token
    public User findByResetToken(String resetToken) {
        return userRepository.findByResetToken(resetToken).orElse(null);
    }

    // Update user password
    public void updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null); // Clear the reset token
        userRepository.save(user);
        LOGGER.info("Password updated for user: " + user.getUsername());
    }
}
