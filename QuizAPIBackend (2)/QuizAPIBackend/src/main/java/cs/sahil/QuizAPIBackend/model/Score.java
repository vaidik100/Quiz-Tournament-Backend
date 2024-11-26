package cs.sahil.QuizAPIBackend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "scores")
@Data
public class Score {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int score;

    private LocalDateTime completedDate;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "quiz_tournament_id" ,nullable = false)
    private QuizTournament quizTournament;

    public Score(Long id, int score, LocalDateTime completedDate, User user, QuizTournament quizTournament) {
        this.id = id;
        this.score = score;
        this.completedDate = completedDate;
        this.user = user;
        this.quizTournament = quizTournament;
    }

    public Score(){

    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public LocalDateTime getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(LocalDateTime completedDate) {
        this.completedDate = completedDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public QuizTournament getQuizTournament() {
        return quizTournament;
    }

    public void setQuizTournament(QuizTournament quizTournament) {
        this.quizTournament = quizTournament;
    }
}

