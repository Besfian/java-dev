package com.blog.service;


import com.blog.dto.CommentRequest;
import com.blog.dto.CommentResponse;
import com.blog.model.Comment;
import com.blog.repository.CommentRepository;
import com.blog.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository, PostRepository postRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
    }

    @Override
    public CommentResponse createComment(Long postId, CommentRequest commentRequest) {
        if (postRepository.findById(postId) == null) {
            throw new RuntimeException("Post not found with id: " + postId);
        }

        Comment comment = new Comment();
        comment.setText(commentRequest.getText());
        comment.setPostId(postId);

        Comment savedComment = commentRepository.save(comment);
        return convertToResponse(savedComment);
    }


    @Override
    public CommentResponse updateComment(Long postId, Long commentId, CommentRequest commentRequest) {
        Comment existingComment = commentRepository.findById(commentId);
        if (existingComment == null) {
            throw new RuntimeException("Comment not found");
        }

        if (!existingComment.getPostId().equals(postId)) {
            throw new RuntimeException("Comment does not belong to post");
        }

        existingComment.setText(commentRequest.getText());
        existingComment.setUpdatedAt(LocalDateTime.now());

        Comment updatedComment = commentRepository.save(existingComment);
        return convertToResponse(updatedComment);
    }

    @Override
    public CommentResponse getComment(Long postId, Long commentId) {
        Comment comment = commentRepository.findById(commentId);
        if (comment == null) {
            throw new RuntimeException("Comment not found with id: " + commentId);
        }

        if (!comment.getPostId().equals(postId)) {
            throw new RuntimeException("Comment does not belong to post with id: " + postId);
        }

        return convertToResponse(comment);
    }

    @Override
    public List<CommentResponse> getCommentsByPostId(Long postId) {
        if (postRepository.findById(postId) == null) {
            throw new RuntimeException("Post not found with id: " + postId);
        }

        return commentRepository.findByPostId(postId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteComment(Long postId, Long commentId) {
        Comment comment = commentRepository.findById(commentId);
        if (comment == null) {
            throw new RuntimeException("Comment not found with id: " + commentId);
        }

        if (!comment.getPostId().equals(postId)) {
            throw new RuntimeException("Comment does not belong to post with id: " + postId);
        }

        commentRepository.deleteById(commentId);
    }

    private CommentResponse convertToResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getText(),
                comment.getPostId()
        );
    }
}
