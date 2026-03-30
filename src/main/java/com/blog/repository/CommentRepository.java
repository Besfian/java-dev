package com.blog.repository;

import com.blog.model.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class CommentRepository {

    private final JdbcClient jdbcClient;

    @Autowired
    public CommentRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    private final RowMapper<Comment> commentRowMapper = (rs, rowNum) -> {
        Comment comment = new Comment();
        comment.setId(rs.getLong("id"));
        comment.setText(rs.getString("text"));
        comment.setPostId(rs.getLong("post_id"));
        comment.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            comment.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return comment;
    };

    public Comment save(Comment comment) {
        if (comment.getId() == null) {
            return insert(comment);
        } else {
            return update(comment);
        }
    }

    private Comment insert(Comment comment) {
        String sql = "INSERT INTO comments (text, post_id, created_at, updated_at) VALUES (:text, :postId, :createdAt, :updatedAt)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        LocalDateTime now = LocalDateTime.now();
        comment.setCreatedAt(now);
        comment.setUpdatedAt(now);

        jdbcClient.sql(sql)
                .param("text", comment.getText())
                .param("postId", comment.getPostId())
                .param("createdAt", Timestamp.valueOf(comment.getCreatedAt()))
                .param("updatedAt", Timestamp.valueOf(comment.getUpdatedAt()))
                .update(keyHolder, "id");

        if (keyHolder.getKey() != null) {
            comment.setId(keyHolder.getKey().longValue());
        }

        return comment;
    }

    private Comment update(Comment comment) {
        String sql = "UPDATE comments SET text = :text, updated_at = :updatedAt WHERE id = :id";

        comment.setUpdatedAt(LocalDateTime.now());

        jdbcClient.sql(sql)
                .param("text", comment.getText())
                .param("updatedAt", Timestamp.valueOf(comment.getUpdatedAt()))
                .param("id", comment.getId())
                .update();

        return comment;
    }

    public Optional<Comment> findById(Long id) {
        String sql = "SELECT * FROM comments WHERE id = :id";
        return jdbcClient.sql(sql)
                .param("id", id)
                .query(commentRowMapper)
                .optional();
    }

    public List<Comment> findByPostId(Long postId) {
        String sql = "SELECT * FROM comments WHERE post_id = :postId ORDER BY created_at ASC";
        return jdbcClient.sql(sql)
                .param("postId", postId)
                .query(commentRowMapper)
                .list();
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM comments WHERE id = :id";
        jdbcClient.sql(sql).param("id", id).update();
    }

    public void deleteByPostId(Long postId) {
        String sql = "DELETE FROM comments WHERE post_id = :postId";
        jdbcClient.sql(sql).param("postId", postId).update();
    }
}