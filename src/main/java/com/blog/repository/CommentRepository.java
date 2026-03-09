package com.blog.repository;


import com.blog.model.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class CommentRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public CommentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
        String sql = "INSERT INTO comments (text, post_id, created_at, updated_at) VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        LocalDateTime now = LocalDateTime.now();
        comment.setCreatedAt(now);
        comment.setUpdatedAt(now);

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, comment.getText());
            ps.setLong(2, comment.getPostId());
            ps.setTimestamp(3, Timestamp.valueOf(comment.getCreatedAt()));
            ps.setTimestamp(4, Timestamp.valueOf(comment.getUpdatedAt()));
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            comment.setId(keyHolder.getKey().longValue());
        }

        jdbcTemplate.update("UPDATE posts SET comments_count = comments_count + 1, updated_at = ? WHERE id = ?",
                Timestamp.valueOf(LocalDateTime.now()), comment.getPostId());

        return comment;
    }

    private Comment update(Comment comment) {
        String sql = "UPDATE comments SET text = ?, updated_at = ? WHERE id = ?";

        comment.setUpdatedAt(LocalDateTime.now());

        jdbcTemplate.update(sql,
                comment.getText(),
                Timestamp.valueOf(comment.getUpdatedAt()),
                comment.getId());

        return comment;
    }

    public Comment findById(Long id) {
        String sql = "SELECT * FROM comments WHERE id = ?";
        List<Comment> comments = jdbcTemplate.query(sql, commentRowMapper, id);
        return comments.isEmpty() ? null : comments.get(0);
    }

    public List<Comment> findByPostId(Long postId) {
        String sql = "SELECT * FROM comments WHERE post_id = ? ORDER BY created_at ASC";
        return jdbcTemplate.query(sql, commentRowMapper, postId);
    }

    public void deleteById(Long id) {
        Comment comment = findById(id);
        if (comment != null) {
            String sql = "DELETE FROM comments WHERE id = ?";
            jdbcTemplate.update(sql, id);

            jdbcTemplate.update("UPDATE posts SET comments_count = comments_count - 1, updated_at = ? WHERE id = ?",
                    Timestamp.valueOf(LocalDateTime.now()), comment.getPostId());
        }
    }

    public void deleteByPostId(Long postId) {
        String sql = "DELETE FROM comments WHERE post_id = ?";
        jdbcTemplate.update(sql, postId);
    }
}
