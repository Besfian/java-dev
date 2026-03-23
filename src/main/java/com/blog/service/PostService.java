package com.blog.service;


import com.blog.dto.PostRequest;
import com.blog.dto.PostResponse;
import com.blog.dto.PostsPageResponse;

public interface PostService {
    PostResponse createPost(PostRequest postRequest);
    PostResponse updatePost(Long id, PostRequest postRequest);
    PostResponse getPost(Long id);
    PostsPageResponse getPosts(String search, int pageNumber, int pageSize);
    void deletePost(Long id);
    int likePost(Long id);
    void updateImage(Long id, byte[] image);
    byte[] getImage(Long id);
}
