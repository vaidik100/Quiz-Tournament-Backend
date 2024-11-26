package cs.sahil.QuizAPIBackend.services;

import cs.sahil.QuizAPIBackend.model.Score;
import cs.sahil.QuizAPIBackend.repository.ScoreRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ScoreService {

    @Autowired
    private ScoreRepository scoreRepository;

    @Transactional
    public Score saveScore(Score score) {
        System.out.println("Saving score: " + score);
        return scoreRepository.save(score);
    }

    public List<Score> getScoresByTournament(Long tournamentId) {
        return scoreRepository.findByQuizTournament_Id(tournamentId);
    }

    public List<Score> getScoresByUser(Long userId) {
        return scoreRepository.findByUser_Id(userId);
    }
}
