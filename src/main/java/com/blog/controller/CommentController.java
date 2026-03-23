package com.blog.controller;


import com.blog.dto.CommentRequest;
import com.blog.dto.CommentResponse;
import com.blog.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {

    private final CommentService commentService;

    @Autowired
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }


@GetMapping
public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long postId) {
    try {
        if (postId == null) {
            return ResponseEntity.badRequest().build();
        }
        List<CommentResponse> comments = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(comments);
    } catch (RuntimeException e) {
        return ResponseEntity.notFound().build();
    }
}

    @GetMapping("/{commentId}")
    public ResponseEntity<CommentResponse> getComment(
            @PathVariable Long postId,
            @PathVariable Long commentId) {
        try {
            CommentResponse comment = commentService.getComment(postId, commentId);
            return ResponseEntity.ok(comment);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long postId,
            @RequestBody CommentRequest commentRequest) {
        try {
            CommentResponse createdComment = commentService.createComment(postId, commentRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

@PutMapping("/{commentId}")
public ResponseEntity<CommentResponse> updateComment(
        @PathVariable Long postId,
        @PathVariable Long commentId,
        @RequestBody CommentRequest commentRequest) {
    try {
        if (commentRequest.getText() == null || commentRequest.getText().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        if (commentRequest.getPostId() == null) {
            commentRequest.setPostId(postId);
        }

        CommentResponse updatedComment = commentService.updateComment(postId, commentId, commentRequest);
        return ResponseEntity.ok(updatedComment);
    } catch (RuntimeException e) {
        return ResponseEntity.notFound().build();
    }
}

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId) {
        try {
            commentService.deleteComment(postId, commentId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
