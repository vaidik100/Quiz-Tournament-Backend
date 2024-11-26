package cs.sahil.QuizAPIBackend.repository;

import cs.sahil.QuizAPIBackend.model.QuizTournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QuizTournamentRepository extends JpaRepository<QuizTournament, Long> {
    List<QuizTournament> findByStartDateBeforeAndEndDateAfter(LocalDateTime beforeDate, LocalDateTime afterDate);

    List<QuizTournament> findByStartDateAfter(LocalDateTime now);

    List<QuizTournament> findByEndDateBefore(LocalDateTime now);
}

