package cs.sahil.QuizAPIBackend.services;

import cs.sahil.QuizAPIBackend.model.User;
import cs.sahil.QuizAPIBackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserRepository userRepository;

    // Method to send tournament notifications to all users (excluding admins)
    public void sendTournamentNotification(List<User> users, String tournamentName) {
        for (User user : users) {
            if (!user.getRoles().contains("ADMIN")) {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(user.getEmail());
                message.setSubject("New Quiz Tournament Created");
                message.setText("A new quiz tournament named \"" + tournamentName + "\" has been created. Join now to participate!");
                mailSender.send(message);
            }
        }
    }

    // Method to initiate the password reset process
    public void initiatePasswordReset(String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            String resetToken = UUID.randomUUID().toString();
            user.setResetToken(resetToken);
            userRepository.save(user);

            sendPasswordResetEmail(user);
        }
    }

    // Method to send password reset email to the user
    private void sendPasswordResetEmail(User user) {
        String resetUrl = "http://localhost:3000/reset-password?token=" + user.getResetToken();


        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Password Reset Request");
        message.setText("Hello " + user.getFirstName() + ",\n\n"
                + "We received a request to reset your password. Click the link below to choose a new password:\n"
                + resetUrl + "\n\n"
                + "If you did not request a password reset, please ignore this email.\n\n"
                + "Best regards,\n"
                + "Quiz App Team");

        mailSender.send(message);
    }
}
