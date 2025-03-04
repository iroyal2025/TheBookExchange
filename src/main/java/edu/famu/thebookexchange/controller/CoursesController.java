package edu.famu.thebookexchange.controller;

import edu.famu.thebookexchange.model.Rest.RestCourses;
import edu.famu.thebookexchange.service.CoursesService;
import edu.famu.thebookexchange.util.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.lang.InterruptedException;

@RestController
@RequestMapping("/Courses")
public class CoursesController {

    @Autowired
    private CoursesService courseService;

    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<RestCourses>>> getAllCourses() {
        try {
            List<RestCourses> restCourses = courseService.getAllCourses();

            if (!restCourses.isEmpty()) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Courses List", restCourses, null));
            } else {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ApiResponse<>(true, "No courses found", null, null));
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error retrieving courses", null, e.getMessage()));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<String>> addCourse(@RequestBody RestCourses course) {
        try {
            String courseId = courseService.addCourse(course);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "Course created", courseId, null));
        } catch (InterruptedException | ExecutionException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error creating course", null, e.getMessage()));
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ApiResponse<String>> deleteCourseByName(@RequestParam String courseName) {
        try {
            boolean deleted = courseService.deleteCourseByName(courseName);

            if (deleted) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Course deleted successfully", null, null));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Failed to delete course", null, null));
            }
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error deleting course", null, e.getMessage()));
        }
    }

    @PutMapping("/{courseId}")
    public ResponseEntity<ApiResponse<String>> updateCourse(@PathVariable String courseId, @RequestBody RestCourses updatedCourse) {
        try {
            String updateTime = courseService.updateCourse(courseId, updatedCourse);
            return ResponseEntity.ok(new ApiResponse<>(true, "Course updated", updateTime, null));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error updating course", null, e.getMessage()));
        }
    }
}