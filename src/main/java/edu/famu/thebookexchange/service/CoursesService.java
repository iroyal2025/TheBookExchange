package edu.famu.thebookexchange.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import edu.famu.thebookexchange.model.Rest.RestBooks;
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
    private static final String BOOKS_COLLECTION = "Books";
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
                RestCourses course = document.toObject(RestCourses.class);
                courses.add(course);
            }
        }

        return courses;
    }

    public RestCourses getCourseById(String courseId) throws InterruptedException, ExecutionException, TimeoutException {
        DocumentReference courseRef = firestore.collection(COURSES_COLLECTION).document(courseId);
        ApiFuture<DocumentSnapshot> future = courseRef.get();
        DocumentSnapshot document = future.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);

        if (document.exists()) {
            return new RestCourses(
                    document.getString("Course Name"),
                    document.getString("teacher"),
                    (List<String>) document.get("textbooks") // Now a list of book titles
            );
        } else {
            return null;
        }
    }

    public List<RestCourses> getCoursesByTeacher(String teacherEmail) throws ExecutionException, InterruptedException {
        List<RestCourses> coursesList = new ArrayList<>();
        Logger logger = LoggerFactory.getLogger(CoursesService.class);

        ApiFuture<QuerySnapshot> future = firestore.collection(COURSES_COLLECTION).whereEqualTo("teacher", teacherEmail).get();

        try {
            List<QueryDocumentSnapshot> documents = future.get(10, TimeUnit.SECONDS).getDocuments();

            for (QueryDocumentSnapshot document : documents) {
                RestCourses restCourse = new RestCourses();
                restCourse.setCourseName(document.getString("Course Name"));
                restCourse.setTeacher(document.getString("teacher"));

                List<String> textbookTitles = (List<String>) document.get("textbooks");
                restCourse.setTextbooks(textbookTitles);

                logger.info("Textbooks from Firestore: {}", textbookTitles); // Log the textbooks list
                logger.info("RestCourse object: {}", restCourse); // log the rest course object.

                coursesList.add(restCourse);
            }

            logger.info("CoursesList being returned: {}", coursesList); // log the final list.

            return coursesList;
        } catch (TimeoutException e) {
            logger.error("TimeoutException in getCoursesByTeacher for teacherEmail: {}", teacherEmail, e);
            return coursesList;
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Error in getCoursesByTeacher for teacherEmail: {}", teacherEmail, e);
            return coursesList;
        }
    }

    public String addCourse(RestCourses course) throws InterruptedException, ExecutionException {
        logger.info("Adding course with details: {}", course);

        Map<String, Object> courseData = new HashMap<>();
        courseData.put("Course Name", course.getCourseName());
        courseData.put("teacher", course.getTeacher());
        courseData.put("textbooks", course.getTextbooks());

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
                    logger.info("Course deleted successfully with ID: {} and Course Name: {}", document.getId(), courseName);
                }
                return true;
            } else {
                logger.warn("Course not found for deletion with Course Name: {}", courseName);
                return false;
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error deleting course with Course Name: {}", courseName, e);
            throw e;
        }
    }

    public String updateCourse(String courseId, RestCourses updatedCourse) throws InterruptedException, ExecutionException, TimeoutException {
        DocumentReference courseRef = firestore.collection(COURSES_COLLECTION).document(courseId);

        Map<String, Object> updatedCourseData = new HashMap<>();
        updatedCourseData.put("Course Name", updatedCourse.getCourseName());
        updatedCourseData.put("teacher", updatedCourse.getTeacher());
        updatedCourseData.put("textbooks", updatedCourse.getTextbooks());

        ApiFuture<WriteResult> writeResult = courseRef.update(updatedCourseData);
        logger.info("Course updated at: {}", writeResult.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getUpdateTime().toString());

        return writeResult.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getUpdateTime().toString();
    }

    public String updateCourseTextbooks(String courseId, List<String> textbookTitles) throws ExecutionException, InterruptedException, TimeoutException {
        DocumentReference courseRef = firestore.collection(COURSES_COLLECTION).document(courseId);

        ApiFuture<WriteResult> future = courseRef.update("textbooks", textbookTitles);
        WriteResult result = future.get(10, java.util.concurrent.TimeUnit.SECONDS);
        return result.getUpdateTime().toString();
    }

    public List<RestBooks> getCourseBooksByCourseName(String courseName) throws ExecutionException, InterruptedException, TimeoutException {
        // Find the course document by course name
        ApiFuture<QuerySnapshot> courseQuery = firestore.collection(COURSES_COLLECTION)
                .whereEqualTo("Course Name", courseName)
                .get();
        List<QueryDocumentSnapshot> courseDocuments = courseQuery.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getDocuments();

        if (courseDocuments.isEmpty()) {
            logger.warn("Course document does not exist for name: {}", courseName);
            return new ArrayList<>();
        }

        DocumentSnapshot courseDocument = courseDocuments.get(0); // Assuming course name is unique
        List<String> bookTitles = (List<String>) courseDocument.get("textbooks");

        if (bookTitles == null || bookTitles.isEmpty()) {
            logger.warn("No book titles found for course name: {}", courseName);
            return new ArrayList<>();
        }

        // Fetch books based on titles
        List<RestBooks> books = new ArrayList<>();
        for (String title : bookTitles) {
            ApiFuture<QuerySnapshot> bookQuery = firestore.collection(BOOKS_COLLECTION)
                    .whereEqualTo("title", title)
                    .get();
            List<QueryDocumentSnapshot> bookDocuments = bookQuery.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getDocuments();

            if (!bookDocuments.isEmpty()) {
                DocumentSnapshot bookDocument = bookDocuments.get(0); // Assuming title is unique
                String bookId = bookDocument.getId();
                List<String> ownedBy = (List<String>) bookDocument.get("ownedBy");
                if (ownedBy == null) {
                    ownedBy = new ArrayList<>();
                }

                RestBooks book = new RestBooks(
                        bookDocument.getString("title"),
                        bookDocument.getString("author"),
                        bookDocument.getString("edition"),
                        bookDocument.getString("ISBN"),
                        bookDocument.getString("condition"),
                        bookDocument.getString("description"),
                        bookDocument.getDouble("price") != null ? bookDocument.getDouble("price") : 0.0,
                        bookDocument.getBoolean("isDigital") != null ? bookDocument.getBoolean("isDigital") : false,
                        bookDocument.getString("digitalCopyPath"),
                        bookId,
                        ownedBy,
                        bookDocument.get("userId", DocumentReference.class),
                        bookDocument.get("courseId", DocumentReference.class),
                        bookDocument.getDouble("rating") != null ? bookDocument.getDouble("rating") : 0.0,
                        bookDocument.getLong("ratingCount") != null ? bookDocument.getLong("ratingCount") : 0
                );
                books.add(book);
            }
        }

        return books;
    }
    public void addTextbookToCourse(String courseName, String textbook) throws ExecutionException, InterruptedException, TimeoutException {
        Query query = firestore.collection(COURSES_COLLECTION).whereEqualTo("Course Name", courseName);
        ApiFuture<QuerySnapshot> future = query.get();
        QuerySnapshot querySnapshot = future.get();

        if (querySnapshot.isEmpty()) {
            throw new IllegalArgumentException("Course not found: " + courseName);
        }

        DocumentSnapshot document = querySnapshot.getDocuments().get(0);
        DocumentReference docRef = document.getReference();

        List<String> textbooks = getCourseTextbooks(docRef.getId());
        textbooks.add(textbook);

        docRef.update("textbooks", textbooks);
    }
    public List<String> getCourseTextbooks(String courseId) throws ExecutionException, InterruptedException, TimeoutException {
        DocumentReference docRef = firestore.collection(COURSES_COLLECTION).document(courseId);
        DocumentSnapshot document = docRef.get().get();
        if (document.exists()) {
            RestCourses course = document.toObject(RestCourses.class);
            if (course != null && course.getTextbooks() != null) {
                return course.getTextbooks();
            }
        }
        return new ArrayList<>();
    }
}