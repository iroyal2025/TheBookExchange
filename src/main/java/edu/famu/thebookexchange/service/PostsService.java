// Service Class (PostsService.java)
package edu.famu.thebookexchange.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import edu.famu.thebookexchange.model.Rest.RestPosts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class PostsService {

    private static final Logger logger = LoggerFactory.getLogger(PostsService.class);
    private Firestore firestore;

    private static final String POSTS_COLLECTION = "Posts";
    private static final long FIRESTORE_TIMEOUT = 5; // Timeout in seconds

    public PostsService() {
        this.firestore = FirestoreClient.getFirestore();
    }

    public List<RestPosts> getAllPosts() throws InterruptedException, ExecutionException, TimeoutException {
        CollectionReference postsCollection = firestore.collection(POSTS_COLLECTION);
        ApiFuture<QuerySnapshot> querySnapshot = postsCollection.get();
        List<QueryDocumentSnapshot> documents = querySnapshot.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getDocuments();

        List<RestPosts> posts = new ArrayList<>();

        for (QueryDocumentSnapshot document : documents) {
            if (document.exists()) {
                RestPosts post = new RestPosts(
                        document.getString("content"),
                        document.getTimestamp("createdAt") != null ? new Timestamp(document.getTimestamp("createdAt").toDate().getTime()) : null,
                        document.get("forumId", DocumentReference.class),
                        document.get("userId", DocumentReference.class)
                );
                posts.add(post);
            }
        }
        return posts;
    }

    public String addPost(RestPosts post) throws InterruptedException, ExecutionException {
        logger.info("Adding post with details: {}", post);

        Map<String, Object> postData = new HashMap<>();
        postData.put("content", post.getContent());
        postData.put("createdAt", post.getCreatedAt());
        postData.put("forumId", post.getForumId());
        postData.put("userId", post.getUserId());

        ApiFuture<DocumentReference> writeResult = firestore.collection(POSTS_COLLECTION).add(postData);
        DocumentReference rs = writeResult.get();
        logger.info("Post added with ID: {}", rs.getId());
        return rs.getId();
    }

    public boolean deletePostByContent(String content) throws ExecutionException, InterruptedException, TimeoutException {
        try {
            Query query = firestore.collection(POSTS_COLLECTION).whereEqualTo("content", content);
            ApiFuture<QuerySnapshot> querySnapshot = query.get();
            List<QueryDocumentSnapshot> documents = querySnapshot.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getDocuments();

            if (!documents.isEmpty()) {
                for (QueryDocumentSnapshot document : documents) {
                    firestore.collection(POSTS_COLLECTION).document(document.getId()).delete().get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS);
                    logger.info("Post deleted successfully with ID: {} and content: {}", document.getId(), content);
                }
                return true;
            } else {
                logger.warn("Post not found for deletion with content: {}", content);
                return false;
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Error deleting post with content: {}", content, e);
            throw e;
        }
    }

    public String updatePost(String postId, RestPosts updatedPost) throws InterruptedException, ExecutionException, TimeoutException {
        DocumentReference postRef = firestore.collection(POSTS_COLLECTION).document(postId);

        Map<String, Object> updatedPostData = new HashMap<>();
        updatedPostData.put("content", updatedPost.getContent());
        updatedPostData.put("createdAt", updatedPost.getCreatedAt());
        updatedPostData.put("forumId", updatedPost.getForumId());
        updatedPostData.put("userId", updatedPost.getUserId());

        ApiFuture<WriteResult> writeResult = postRef.update(updatedPostData);
        logger.info("Post updated at: {}", writeResult.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getUpdateTime().toString());

        return writeResult.get(FIRESTORE_TIMEOUT, TimeUnit.SECONDS).getUpdateTime().toString();
    }
}