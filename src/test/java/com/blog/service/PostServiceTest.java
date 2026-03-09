package com.blog.service;


import com.blog.config.AppConfig;
import com.blog.config.DatabaseConfig;
import com.blog.dto.PostRequest;
import com.blog.dto.PostResponse;
import com.blog.dto.PostsPageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AppConfig.class, DatabaseConfig.class})
@WebAppConfiguration
@Transactional
@ActiveProfiles("default")
public class PostServiceTest {

    @Autowired
    private PostService postService;

    private PostRequest testPostRequest;

    @BeforeEach
    void setUp() {
        testPostRequest = new PostRequest();
        testPostRequest.setTitle("Test Post");
        testPostRequest.setText("This is a test post content");
        testPostRequest.setTags(Arrays.asList("test", "java"));
    }

    @Test
    void testCreatePost() {
        PostResponse created = postService.createPost(testPostRequest);

        assertNotNull(created.getId());
        assertEquals(testPostRequest.getTitle(), created.getTitle());
        assertEquals(testPostRequest.getText(), created.getText());
        assertEquals(testPostRequest.getTags(), created.getTags());
        assertEquals(0, created.getLikesCount());
        assertEquals(0, created.getCommentsCount());
    }

    @Test
    void testGetPost() {
        PostResponse created = postService.createPost(testPostRequest);
        PostResponse found = postService.getPost(created.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals(created.getTitle(), found.getTitle());
        assertEquals(created.getText(), found.getText());
    }

    @Test
    void testUpdatePost() {
        PostResponse created = postService.createPost(testPostRequest);

        PostRequest updateRequest = new PostRequest();
        updateRequest.setTitle("Updated Title");
        updateRequest.setText("Updated content");
        updateRequest.setTags(Arrays.asList("updated", "test"));

        PostResponse updated = postService.updatePost(created.getId(), updateRequest);

        assertEquals(created.getId(), updated.getId());
        assertEquals("Updated Title", updated.getTitle());
        assertEquals("Updated content", updated.getText());
        assertEquals(Arrays.asList("updated", "test"), updated.getTags());
    }

    @Test
    void testGetPostsWithPagination() {
        for (int i = 1; i <= 10; i++) {
            PostRequest request = new PostRequest();
            request.setTitle("Post " + i);
            request.setText("Content " + i);
            request.setTags(Arrays.asList("tag" + i));
            postService.createPost(request);
        }

        PostsPageResponse page1 = postService.getPosts("", 1, 5);
        assertEquals(5, page1.getPosts().size());
        assertFalse(page1.isHasPrev());
        assertTrue(page1.isHasNext());
        assertEquals(2, page1.getLastPage());

        PostsPageResponse page2 = postService.getPosts("", 2, 5);
        assertEquals(5, page2.getPosts().size());
        assertTrue(page2.isHasPrev());
        assertFalse(page2.isHasNext());
        assertEquals(2, page2.getLastPage());
    }

    @Test
    void testSearchPosts() {
        postService.createPost(testPostRequest);

        PostRequest anotherPost = new PostRequest();
        anotherPost.setTitle("Different Title");
        anotherPost.setText("Different content");
        anotherPost.setTags(Arrays.asList("other"));
        postService.createPost(anotherPost);

        PostsPageResponse searchResult = postService.getPosts("Test", 1, 10);
        assertEquals(1, searchResult.getPosts().size());
        assertEquals("Test Post", searchResult.getPosts().get(0).getTitle());
    }


    @Test
    void testLikePost() {
        PostResponse created = postService.createPost(testPostRequest);

        int likesCount = postService.likePost(created.getId());
        assertEquals(1, likesCount); // Сейчас здесь ожидание 1, но получает 2


        PostResponse afterLike = postService.getPost(created.getId());
        assertEquals(1, afterLike.getLikesCount());
    }

    @Test
    void testDeletePost() {
        PostResponse created = postService.createPost(testPostRequest);

        postService.deletePost(created.getId());

        assertThrows(RuntimeException.class, () -> {
            postService.getPost(created.getId());
        });
    }
}
