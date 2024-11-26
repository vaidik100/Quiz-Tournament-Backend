package cs.sahil.QuizAPIBackend.repository;

import cs.sahil.QuizAPIBackend.model.QuizTournament;
import cs.sahil.QuizAPIBackend.model.Score;
import cs.sahil.QuizAPIBackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScoreRepository extends JpaRepository<Score, Long> {
    List<Score> findByQuizTournament(QuizTournament quizTournament);

    List<Score> findByQuizTournament_Id(Long quizTournamentId);

    List<Score> findByUser(User user);

    List<Score> findByUser_Id(Long userId);
}

