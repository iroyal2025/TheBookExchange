package edu.famu.thebookexchange.model.Rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class RestCourses extends edu.famu.thebookexchange.model.Abstracts.ACourses {
    private String teacher;
    private List<String> textbooks; // Store book titles as strings for JSON

    public RestCourses() {
        super();
    }

    public RestCourses(String courseName, String teacher, List<String> textbooks) {
        super(courseName);
        this.teacher = teacher;
        this.textbooks = textbooks;
    }

    @JsonProperty("teacher")
    public String getTeacher() {
        return teacher;
    }

    @JsonProperty("teacher")
    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    @JsonProperty("textbooks")
    public List<String> getTextbooks() {
        return textbooks;
    }

    @JsonProperty("textbooks")
    public void setTextbooks(List<String> textbooks) {
        this.textbooks = textbooks;
    }

    @Override
    public String toString() {
        return "RestCourses{" +
                ", teacher='" + teacher + '\'' +
                ", textbooks=" + textbooks +
                ", Course Name='" + getCourseName() + '\'' +
                '}';
    }
}