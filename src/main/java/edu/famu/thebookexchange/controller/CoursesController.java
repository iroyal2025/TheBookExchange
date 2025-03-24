package edu.famu.thebookexchange.controller;

import com.google.cloud.firestore.Firestore;
import edu.famu.thebookexchange.model.Rest.RestBooks;
import edu.famu.thebookexchange.model.Rest.RestCourses;
import edu.famu.thebookexchange.service.BooksService;
import edu.famu.thebookexchange.service.CoursesService;
import edu.famu.thebookexchange.service.BooksService; // Import BookService
import edu.famu.thebookexchange.util.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping("/Courses")
public class CoursesController {

    private static final Logger logger = LoggerFactory.getLogger(CoursesController.class);

    @Autowired
    private CoursesService courseService;

    @Autowired
    private BooksService bookService; // Inject BookService

    @Autowired
    private Firestore firestore; // Inject Firestore

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
            logger.error("Error retrieving all courses", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error retrieving courses", null, e.getMessage()));
        }
    }

    @GetMapping("/teacher/{teacherEmail}")
    public ResponseEntity<ApiResponse<List<RestCourses>>> getCoursesByTeacher(@PathVariable String teacherEmail) {
        try {
            List<RestCourses> courses = courseService.getCoursesByTeacher(teacherEmail);
            return ResponseEntity.ok(new ApiResponse<>(true, "Courses for teacher", courses, null));
        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error retrieving courses for teacher: {}", teacherEmail, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error retrieving courses", null, e.getMessage()));
        }
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<String>> addCourse(@RequestBody RestCourses course) {
        try {
            String courseId = courseService.addCourse(course);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(true, "Course created", courseId, null));
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error creating course: {}", course, e);
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
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, "Course not found for deletion", null, null));
            }
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            logger.error("Error deleting course: {}", courseName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error deleting course", null, e.getMessage()));
        }
    }

    @PutMapping("/{courseId}")
    public ResponseEntity<ApiResponse<String>> updateCourse(@PathVariable String courseId, @RequestBody RestCourses updatedCourse) {
        try {
            String updateTime = courseService.updateCourse(courseId, updatedCourse);
            return ResponseEntity.ok(new ApiResponse<>(true, "Course updated", updateTime, null));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error updating course: {}", courseId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error updating course", null, e.getMessage()));
        }
    }

    @PutMapping("/textbooks/{courseId}")
    public ResponseEntity<ApiResponse<String>> updateCourseTextbooks(@PathVariable String courseId, @RequestBody RestCourses updatedCourse) {
        try {
            String updateTime = courseService.updateCourseTextbooks(courseId, updatedCourse.getTextbooks());
            return ResponseEntity.ok(new ApiResponse<>(true, "Course textbooks updated", updateTime, null));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error updating course textbooks: {}", courseId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error updating course textbooks", null, e.getMessage()));
        }
    }

    @GetMapping("/books/name/{courseName}")
    public ResponseEntity<ApiResponse<List<RestBooks>>> getCourseBooksByCourseName(@PathVariable String courseName) {
        try {
            List<RestBooks> books = bookService.getCourseBooksByCourseName(courseName);
            return ResponseEntity.ok(new ApiResponse<>(true, "Books for course name", books, null));
        } catch (InterruptedException e) {
            logger.error("Interrupted while retrieving course books by name: {}", courseName, e);
            Thread.currentThread().interrupt(); // Restore interrupt status
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Interrupted while retrieving course books by name", null, e.getMessage()));
        } catch (ExecutionException | TimeoutException e) {
            logger.error("Error retrieving course books by name: {}", courseName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error retrieving course books by name", null, e.getMessage()));
        }
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<ApiResponse<RestCourses>> getCourseById(@PathVariable String courseId) {
        try {
            RestCourses course = courseService.getCourseById(courseId);
            if (course != null) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Course found", course, null));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse<>(false, "Course not found", null, null));
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error retrieving course by ID: {}", courseId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error retrieving course", null, e.getMessage()));
        }
    }
    @PostMapping("/addTextbook")
    public ResponseEntity<ApiResponse<String>> addTextbookToCourse(
            @RequestParam String courseName,
            @RequestParam String textbook
    ) {
        try {
            courseService.addTextbookToCourse(courseName, textbook);
            return ResponseEntity.ok(new ApiResponse<>(true, "Textbook added to course successfully", null, null));
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            logger.error("Error adding textbook to course: {}", courseName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse<>(false, "Error adding textbook to course", null, e.getMessage()));
        }
    }
}