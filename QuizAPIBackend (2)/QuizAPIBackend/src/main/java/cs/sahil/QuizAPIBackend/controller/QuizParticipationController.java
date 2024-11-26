package cs.sahil.QuizAPIBackend.controller;

import cs.sahil.QuizAPIBackend.model.Question;
import cs.sahil.QuizAPIBackend.model.QuizTournament;
import cs.sahil.QuizAPIBackend.model.Score;
import cs.sahil.QuizAPIBackend.model.User;
import cs.sahil.QuizAPIBackend.services.QuizTournamentService;
import cs.sahil.QuizAPIBackend.services.ScoreService;
import cs.sahil.QuizAPIBackend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/participation")
public class QuizParticipationController {

    @Autowired
    private QuizTournamentService quizTournamentService;

    @Autowired
    private ScoreService scoreService;

    @Autowired
    private UserService userService;

    // Player: Participate in an ongoing tournament
    @GetMapping("/tournament/{id}/start")
    public ResponseEntity<?> startQuiz(@PathVariable Long id) {
        // Fetch the tournament
        QuizTournament tournament = quizTournamentService.getTournamentById(id);
        if (tournament == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tournament not found");
        }

        // Fetch associated questions
        List<Question> questions = quizTournamentService.getQuestionsByTournamentId(id);
        if (questions == null || questions.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No questions found for the tournament");
        }

        // Debugging logs
        System.out.println("Fetched Tournament: " + tournament.getName());
        questions.forEach(q -> System.out.println("Question: " + q.getQuestionText()));

        return ResponseEntity.ok(questions);
    }

    @PostMapping("/tournament/{id}/submit")
    public ResponseEntity<?> submitQuiz(@PathVariable Long id, @RequestBody Map<String, Object> payload, Principal principal) {
        // Authenticate user
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated.");
        }

        User user = userService.findByUsername(principal.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found.");
        }

        // Fetch the tournament
        QuizTournament tournament = quizTournamentService.getTournamentById(id);
        if (tournament == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tournament not found.");
        }

        // Fetch questions for the tournament
        List<Question> questions = quizTournamentService.getQuestionsByTournamentId(id);
        if (questions == null || questions.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No questions found for this tournament.");
        }

        // Validate answers
        List<String> answers = (List<String>) payload.get("answers");
        if (answers == null || answers.size() != questions.size()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid number of answers provided.");
        }

        // Calculate the score
        int scoreValue = calculateScore(questions, answers);

        // Save the score
        Score score = new Score();
        score.setUser(user);
        score.setQuizTournament(tournament);
        score.setScore(scoreValue);
        score.setCompletedDate(LocalDateTime.now());

        scoreService.saveScore(score);

        return ResponseEntity.ok(Map.of("score", scoreValue, "totalQuestions", questions.size()));
    }

    // Helper method to calculate score
    private int calculateScore(List<Question> questions, List<String> answers) {
        int score = 0;
        for (int i = 0; i < questions.size(); i++) {
            System.out.println("Comparing Question: " + questions.get(i).getQuestionText()
                    + " | Correct Answer: " + questions.get(i).getCorrectAnswer()
                    + " | Provided Answer: " + answers.get(i));
            if (questions.get(i).getCorrectAnswer().equalsIgnoreCase(answers.get(i))) {
                score++;
            }
        }
        System.out.println("Total Score: " + score);
        return score;
    }
}
