package com.blog.service;


import com.blog.dto.CommentRequest;
import com.blog.dto.CommentResponse;

import java.util.List;

public interface CommentService {
    CommentResponse createComment(Long postId, CommentRequest commentRequest);
    CommentResponse updateComment(Long postId, Long commentId, CommentRequest commentRequest);
    CommentResponse getComment(Long postId, Long commentId);
    List<CommentResponse> getCommentsByPostId(Long postId);
    void deleteComment(Long postId, Long commentId);
}
