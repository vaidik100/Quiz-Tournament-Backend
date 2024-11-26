package cs.sahil.QuizAPIBackend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name = "questions")
@Data
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String questionText;

    @ElementCollection
    private List<String> choices;

    private String correctAnswer;

    @ManyToOne
    @JoinColumn(name = "quiz_tournament_id")
    private QuizTournament quizTournament;

    public Question(Long id, String questionText, List<String> choices, String correctAnswer, QuizTournament quizTournament) {
        this.id = id;
        this.questionText = questionText;
        this.choices = choices;
        this.correctAnswer = correctAnswer;
        this.quizTournament = quizTournament;
    }

    public Question(){

    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public List<String> getChoices() {
        return choices;
    }

    public void setChoices(List<String> choices) {
        this.choices = choices;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public QuizTournament getQuizTournament() {
        return quizTournament;
    }

    public void setQuizTournament(QuizTournament quizTournament) {
        this.quizTournament = quizTournament;
    }
}
