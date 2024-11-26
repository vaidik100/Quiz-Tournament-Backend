package cs.sahil.QuizAPIBackend.controller;

import cs.sahil.QuizAPIBackend.model.QuizTournament;
import cs.sahil.QuizAPIBackend.model.User;
import cs.sahil.QuizAPIBackend.services.EmailService;
import cs.sahil.QuizAPIBackend.services.QuizTournamentService;
import cs.sahil.QuizAPIBackend.services.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/tournaments")
public class QuizTournamentController {

    @Autowired
    private QuizTournamentService quizTournamentService;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    private static final Logger logger = LoggerFactory.getLogger(QuizTournamentController.class);

    @PostMapping("/admin/create")
    public ResponseEntity<?> createTournament(@RequestBody QuizTournament tournament) {
        try {
            // Validate input data
            if (tournament.getName() == null || tournament.getName().isEmpty()) {
                return ResponseEntity.badRequest().body("Tournament name is required.");
            }
            if (tournament.getStartDate() == null || tournament.getEndDate() == null) {
                return ResponseEntity.badRequest().body("Start and end dates are required.");
            }
            if (tournament.getStartDate().isAfter(tournament.getEndDate())) {
                return ResponseEntity.badRequest().body("Start date must be before end date.");
            }

            // Save the tournament
            QuizTournament savedTournament = quizTournamentService.createTournament(tournament);

            // Fetch all users
            List<User> users = userService.getAllUsers();

            // Send email notifications
            emailService.sendTournamentNotification(users, savedTournament.getName());

            return ResponseEntity.ok("Quiz tournament created successfully and notifications sent.");
        } catch (MailAuthenticationException e) {
            logger.error("Email authentication failed: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Email authentication failed. Check your email configuration.");
        } catch (DataIntegrityViolationException e) {
            logger.error("Database error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Database error: Check if all required fields are provided.");
        } catch (Exception e) {
            logger.error("Error creating quiz tournament: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while creating the tournament.");
        }
    }



    @PutMapping("/{id}")
    public ResponseEntity<?> updateTournament(@PathVariable Long id, @RequestBody QuizTournament tournament) {
        QuizTournament existingTournament = quizTournamentService.getTournamentById(id);
        if (existingTournament == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tournament not found.");
        }
        existingTournament.setName(tournament.getName());
        existingTournament.setCategory(tournament.getCategory());
        existingTournament.setDifficulty(tournament.getDifficulty());
        existingTournament.setStartDate(tournament.getStartDate());
        existingTournament.setEndDate(tournament.getEndDate());
        quizTournamentService.updateTournament(existingTournament);

        return ResponseEntity.ok("Quiz tournament updated successfully");
    }

    @DeleteMapping("/admin/delete/{id}")
    public ResponseEntity<?> deleteTournament(@PathVariable Long id) {
        try {
            QuizTournament tournament = quizTournamentService.getTournamentById(id);
            if (tournament == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tournament not found.");
            }
            quizTournamentService.deleteTournament(id);
            return ResponseEntity.ok("Quiz tournament deleted successfully");
        } catch (Exception e) {
            logger.error("Error deleting tournament: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while deleting the tournament.");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTournamentById(@PathVariable Long id) {
        try {
            QuizTournament tournament = quizTournamentService.getTournamentById(id);
            if (tournament == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tournament not found.");
            }
            return ResponseEntity.ok(tournament);
        } catch (Exception e) {
            logger.error("Error fetching tournament with ID: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while fetching the tournament.");
        }
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('PLAYER') or hasRole('ADMIN')")
    public ResponseEntity<List<QuizTournament>> getAllTournaments() {
        List<QuizTournament> tournaments = quizTournamentService.getAllTournaments();
        return ResponseEntity.ok(tournaments);
    }

    @GetMapping("/{id}/likes")
    public ResponseEntity<Integer> getLikes(@PathVariable Long id) {
        QuizTournament tournament = quizTournamentService.getTournamentById(id);
        return ResponseEntity.ok(tournament.getLikes());
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<?> likeTournament(@PathVariable Long id) {
        quizTournamentService.likeTournament(id);
        return ResponseEntity.ok("Tournament liked");
    }

    @PostMapping("/{id}/unlike")
    public ResponseEntity<?> unlikeTournament(@PathVariable Long id) {
        quizTournamentService.unlikeTournament(id);
        return ResponseEntity.ok("Tournament unliked");
    }

    @GetMapping("/ongoing")
    public ResponseEntity<List<QuizTournament>> getOngoingTournaments() {
        try {
            List<QuizTournament> tournaments = quizTournamentService.getOngoingTournaments();
            return ResponseEntity.ok(tournaments);
        } catch (Exception e) {
            logger.error("Error fetching ongoing tournaments: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<QuizTournament>> getUpcomingTournaments() {
        List<QuizTournament> tournaments = quizTournamentService.getUpcomingTournaments();
        return ResponseEntity.ok(tournaments);
    }

    @GetMapping("/past")
    public ResponseEntity<List<QuizTournament>> getPastTournaments() {
        List<QuizTournament> tournaments = quizTournamentService.getPastTournaments();
        return ResponseEntity.ok(tournaments);
    }
}
