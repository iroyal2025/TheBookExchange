package edu.famu.thebookexchange.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import edu.famu.thebookexchange.model.Rest.RestCourses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class CoursesService {

    private static final Logger logger = LoggerFactory.getLogger(CoursesService.class);
    private Firestore firestore;

    private static final String COURSES_COLLECTION = "Courses";
    private static final long FIRESTORE_TIMEOUT = 5; // Timeout in seconds

    public CoursesService() {
        this.firestore = FirestoreClient.getFirestore();
    }

    public List<RestCourses> getAllCourses() throws InterruptedException, ExecutionException, TimeoutException {
        CollectionReference coursesCollection = firestore.collection(COURSES_COLLECTION);
        ApiFuture<QuerySnapshot> querySnapshot = coursesCollection.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getDocuments();

        List<RestCourses> courses = new ArrayList<>();

        for (QueryDocumentSnapshot document : documents) {
            if (document.exists()) {
                RestCourses course = new RestCourses(
                        document.getString("courseName"),
                        document.getString("textbookList"),
                        document.getReference() // Correctly get the DocumentReference
                );
                courses.add(course);
            }
        }

        return courses;
    }

    public String addCourse(RestCourses course) throws InterruptedException, ExecutionException {
        logger.info("Adding course with details: {}", course);

        Map<String, Object> courseData = new HashMap<>();
        courseData.put("courseName", course.getCourseName());
        courseData.put("textbookList", course.getTextbookList());

        ApiFuture<DocumentReference> writeResult = firestore.collection(COURSES_COLLECTION).add(courseData);
        DocumentReference rs = writeResult.get();
        logger.info("Course added with ID: {}", rs.getId());
        return rs.getId();
    }

    public boolean deleteCourseByName(String courseName) throws ExecutionException, InterruptedException, TimeoutException {
        try {
            Query query = firestore.collection(COURSES_COLLECTION).whereEqualTo("courseName", courseName);
            ApiFuture<QuerySnapshot> querySnapshot = query.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getDocuments();

            if (!documents.isEmpty()) {
                for (QueryDocumentSnapshot document : documents) {
                    firestore.collection(COURSES_COLLECTION).document(document.getId()).delete().get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
                    logger.info("Course deleted successfully with ID: {} and courseName: {}", document.getId(), courseName);
                }
                return true;
            } else {
                logger.warn("Course not found for deletion with courseName: {}", courseName);
                return false;
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error deleting course with courseName: {}", courseName, e);
            throw e;
        }
    }

    public String updateCourse(String courseId, RestCourses updatedCourse) throws InterruptedException, ExecutionException, TimeoutException {
        DocumentReference courseRef = firestore.collection(COURSES_COLLECTION).document(courseId);

        Map<String, Object> updatedCourseData = new HashMap<>();
        updatedCourseData.put("courseName", updatedCourse.getCourseName());
        updatedCourseData.put("textbookList", updatedCourse.getTextbookList());

        ApiFuture<WriteResult> writeResult = courseRef.update(updatedCourseData);
        logger.info("Course updated at: {}", writeResult.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getUpdateTime().toString());

        return writeResult.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getUpdateTime().toString();
    }
}
