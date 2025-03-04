package edu.famu.thebookexchange.model.Abstracts;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Data
@NoArgsConstructor
public abstract class ACourses {

    @JsonProperty("Course Name")
    private String courseName;

    public ACourses(String courseName) {
        this.courseName = courseName;
    }

    // Manual Getters
    public String getCourseName() {
        return courseName;
    }

    // Manual Setters (if needed, though @Setter already provides them)
    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }
}