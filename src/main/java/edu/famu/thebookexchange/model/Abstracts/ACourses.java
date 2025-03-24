package edu.famu.thebookexchange.model.Abstracts;

public abstract class ACourses {

    private String courseName;

    public ACourses() {
        this.courseName = "";
    }

    public ACourses(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }
}