package cs.sahil.QuizAPIBackend;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class OpenTDBResponse {

    @JsonProperty("response_code")
    private int responseCode;

    private List<OpenTDBQuestion> results;
    public List<OpenTDBQuestion> getResults() {
        return results;
    }

}
