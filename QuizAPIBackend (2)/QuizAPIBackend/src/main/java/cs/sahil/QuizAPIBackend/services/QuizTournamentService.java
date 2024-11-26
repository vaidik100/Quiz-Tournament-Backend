package cs.sahil.QuizAPIBackend.services;

import cs.sahil.QuizAPIBackend.OpenTDBQuestion;
import cs.sahil.QuizAPIBackend.OpenTDBResponse;
import cs.sahil.QuizAPIBackend.model.Question;
import cs.sahil.QuizAPIBackend.model.QuizTournament;
import cs.sahil.QuizAPIBackend.model.Score;
import cs.sahil.QuizAPIBackend.model.User;
import cs.sahil.QuizAPIBackend.repository.QuestionRepository;
import cs.sahil.QuizAPIBackend.repository.QuizTournamentRepository;
import cs.sahil.QuizAPIBackend.repository.ScoreRepository;
import cs.sahil.QuizAPIBackend.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@Service
public class QuizTournamentService {

    @Autowired
    private QuizTournamentRepository quizTournamentRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private ScoreRepository scoreRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;

    // Create a new tournament
    public QuizTournament createTournament(QuizTournament tournament) {
        // Step 1: Save the tournament to generate an ID
        tournament = quizTournamentRepository.save(tournament);
        System.out.println("Tournament created with ID: " + tournament.getId());

        // Step 2: Fetch questions from OpenTDB API
        List<Question> questions = fetchQuestionsFromOpenTDB(tournament.getCategory(), tournament.getDifficulty());
        if (questions.isEmpty()) {
            System.out.println("No questions were fetched from OpenTDB.");
            throw new RuntimeException("Unable to fetch questions from OpenTDB. Please try again.");
        }
        System.out.println("Fetched " + questions.size() + " questions from OpenTDB.");

        // Step 3: Assign fetched questions to the tournament and save each question
        for (Question question : questions) {
            question.setQuizTournament(tournament);
            questionRepository.save(question);
        }
        System.out.println("All questions have been saved.");

        // Step 4: Update the tournament with the questions
        tournament.setQuestions(new HashSet<>(questions));
        tournament = quizTournamentRepository.save(tournament);
        System.out.println("Tournament updated with questions.");

        // Step 5: Notify users about the new tournament
        List<User> users = userRepository.findAll();
        emailService.sendTournamentNotification(users, tournament.getName());

        return tournament;
    }

    // Helper method to fetch questions from OpenTDB API
    private List<Question> fetchQuestionsFromOpenTDB(String category, String difficulty) {
        RestTemplate restTemplate = new RestTemplate();
        StringBuilder url = new StringBuilder("https://opentdb.com/api.php?amount=10");

        if (category != null && !category.isEmpty()) {
            url.append("&category=").append(category);
        }
        if (difficulty != null && !difficulty.isEmpty()) {
            url.append("&difficulty=").append(difficulty);
        }

        OpenTDBResponse response = restTemplate.getForObject(url.toString(), OpenTDBResponse.class);

        if (response == null || response.getResults() == null || response.getResults().isEmpty()) {
            System.out.println("OpenTDB API returned no questions.");
            return new ArrayList<>();
        }

        List<Question> questions = new ArrayList<>();
        for (OpenTDBQuestion apiQuestion : response.getResults()) {
            Question question = new Question();
            question.setQuestionText(apiQuestion.getQuestionText());
            question.setCorrectAnswer(apiQuestion.getCorrectAnswer());

            List<String> choices = new ArrayList<>(apiQuestion.getIncorrectAnswers());
            choices.add(apiQuestion.getCorrectAnswer());
            Collections.shuffle(choices);
            question.setChoices(choices);

            questions.add(question);
        }
        return questions;
    }

    public List<Question> getQuestionsByTournamentId(Long quizId) {
        return questionRepository.findByQuizTournamentId(quizId);
    }

    public QuizTournament getTournamentById(Long id) {
        return quizTournamentRepository.findById(id).orElse(null);
    }

    public List<QuizTournament> getAllTournaments() {
        return quizTournamentRepository.findAll();
    }

    public QuizTournament updateTournament(QuizTournament tournament) {
        return quizTournamentRepository.save(tournament);
    }

    @Transactional
    public void deleteTournament(Long id) {
        QuizTournament tournament = quizTournamentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tournament not found"));

        // Delete associated questions
        List<Question> questions = questionRepository.findByQuizTournamentId(id);
        questionRepository.deleteAll(questions);

        // Delete associated scores
        List<Score> scores = scoreRepository.findByQuizTournament_Id(id);
        scoreRepository.deleteAll(scores);

        // Clear associations to avoid persistence issues
        tournament.getQuestions().clear();
        tournament.getScores().clear();

        // Finally, delete the tournament
        quizTournamentRepository.deleteById(id);
        System.out.println("Tournament and associated data deleted successfully.");
    }

    public List<QuizTournament> getOngoingTournaments() {
        LocalDateTime now = LocalDateTime.now();
        return quizTournamentRepository.findByStartDateBeforeAndEndDateAfter(now, now);
    }

    public List<QuizTournament> getUpcomingTournaments() {
        LocalDateTime now = LocalDateTime.now();
        return quizTournamentRepository.findByStartDateAfter(now);
    }

    public List<QuizTournament> getPastTournaments() {
        LocalDateTime now = LocalDateTime.now();
        return quizTournamentRepository.findByEndDateBefore(now);
    }

    public void likeTournament(Long id) {
        QuizTournament tournament = quizTournamentRepository.findById(id).orElse(null);
        if (tournament != null) {
            tournament.setLikes(tournament.getLikes() + 1);
            quizTournamentRepository.save(tournament);
        }
    }

    public void unlikeTournament(Long id) {
        QuizTournament tournament = quizTournamentRepository.findById(id).orElse(null);
        if (tournament != null && tournament.getLikes() > 0) {
            tournament.setLikes(tournament.getLikes() - 1);
            quizTournamentRepository.save(tournament);
        }
    }
}
