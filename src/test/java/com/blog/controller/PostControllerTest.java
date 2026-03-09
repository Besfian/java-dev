package com.blog.controller;


import com.blog.config.AppConfig;
import com.blog.config.DatabaseConfig;
import com.blog.config.WebConfig;
import com.blog.dto.PostRequest;
import com.blog.dto.PostResponse;
import com.blog.service.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AppConfig.class, DatabaseConfig.class, WebConfig.class})
@WebAppConfiguration
@Transactional
public class PostControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PostService postService;

    private MockMvc mockMvc;
    private PostRequest testPostRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        testPostRequest = new PostRequest();
        testPostRequest.setTitle("Test Post");
        testPostRequest.setText("This is a test post content");
        testPostRequest.setTags(Arrays.asList("test", "java"));
    }

    @Test
    void testGetPosts() throws Exception {
        postService.createPost(testPostRequest);

        mockMvc.perform(get("/api/posts")
                        .param("search", "")
                        .param("pageNumber", "1")
                        .param("pageSize", "5"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.posts").isArray())
                .andExpect(jsonPath("$.hasPrev").isBoolean())
                .andExpect(jsonPath("$.hasNext").isBoolean())
                .andExpect(jsonPath("$.lastPage").isNumber());
    }

    @Test
    void testCreatePost() throws Exception {
        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testPostRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value(testPostRequest.getTitle()))
                .andExpect(jsonPath("$.text").value(testPostRequest.getText()))
                .andExpect(jsonPath("$.tags[0]").value("test"))
                .andExpect(jsonPath("$.likesCount").value(0))
                .andExpect(jsonPath("$.commentsCount").value(0));
    }

    @Test
    void testGetPost() throws Exception {
        PostResponse created = postService.createPost(testPostRequest);

        mockMvc.perform(get("/api/posts/{id}", created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()))
                .andExpect(jsonPath("$.title").value(testPostRequest.getTitle()));
    }

    @Test
    void testUpdatePost() throws Exception {
        PostResponse created = postService.createPost(testPostRequest);

        PostRequest updateRequest = new PostRequest();
        updateRequest.setTitle("Updated Title");
        updateRequest.setText("Updated content");
        updateRequest.setTags(Arrays.asList("updated"));

        mockMvc.perform(put("/api/posts/{id}", created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(created.getId()))
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.text").value("Updated content"));
    }

    @Test
    void testDeletePost() throws Exception {
        PostResponse created = postService.createPost(testPostRequest);

        mockMvc.perform(delete("/api/posts/{id}", created.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/posts/{id}", created.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testLikePost() throws Exception {
        PostResponse created = postService.createPost(testPostRequest);

        mockMvc.perform(post("/api/posts/{id}/likes", created.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    @Test
    void testUpdateImage() throws Exception {
        PostResponse created = postService.createPost(testPostRequest);

        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        mockMvc.perform(multipart("/api/posts/{id}/image", created.getId())
                        .file(imageFile)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk());
    }

    @Test
    void testGetImage() throws Exception {
        PostResponse created = postService.createPost(testPostRequest);

        MockMultipartFile imageFile = new MockMultipartFile(
                "image",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        mockMvc.perform(multipart("/api/posts/{id}/image", created.getId())
                .file(imageFile)
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                }));

        mockMvc.perform(get("/api/posts/{id}/image", created.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_JPEG));
    }
}
