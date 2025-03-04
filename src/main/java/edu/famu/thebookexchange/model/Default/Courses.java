package edu.famu.thebookexchange.model.Default;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Courses {

    private int courseId;
    private String courseName;
    private String textbookList;
    private int userId; // Foreign key referencing Users table

    public Courses(int courseId, String courseName, String textbookList, int userId) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.textbookList = textbookList;
        this.userId = userId;
    }

    // Manual Getters (If Lombok is not working)
    public int getCourseId() {
        return courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public String getTextbookList() {
        return textbookList;
    }

    public int getUserId() {
        return userId;
    }

    // Manual Setters (If needed)
    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public void setTextbookList(String textbookList) {
        this.textbookList = textbookList;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}