package edu.famu.thebookexchange.model.Abstracts;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Data
@NoArgsConstructor
public abstract class ACourses {

    @JsonProperty("courseName")
    private String courseName;

    @JsonProperty("textbookList")
    private String textbookList;

    public ACourses(String courseName, String textbookList) {
        this.courseName = courseName;
        this.textbookList = textbookList;
    }
}
