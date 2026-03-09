package com.blog.repository;


import com.blog.model.Post;
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
import java.util.Arrays;
import java.util.List;

@Repository
public class PostRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public PostRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Post> postRowMapper = (rs, rowNum) -> {
        Post post = new Post();
        post.setId(rs.getLong("id"));
        post.setTitle(rs.getString("title"));
        post.setText(rs.getString("text"));

        String tagsStr = rs.getString("tags");
        if (tagsStr != null && !tagsStr.isEmpty()) {
            post.setTags(Arrays.asList(tagsStr.split(",")));
        }

        post.setLikesCount(rs.getInt("likes_count"));
        post.setCommentsCount(rs.getInt("comments_count"));
        post.setImage(rs.getBytes("image"));
        post.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            post.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return post;
    };

    public Post save(Post post) {
        if (post.getId() == null) {
            return insert(post);
        } else {
            return update(post);
        }
    }

    private Post insert(Post post) {
        String sql = "INSERT INTO posts (title, text, tags, likes_count, comments_count, image, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        LocalDateTime now = LocalDateTime.now();
        post.setCreatedAt(now);
        post.setUpdatedAt(now);

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, post.getTitle());
            ps.setString(2, post.getText());
            ps.setString(3, post.getTags() != null ? String.join(",", post.getTags()) : null);
            ps.setInt(4, post.getLikesCount());
            ps.setInt(5, post.getCommentsCount());
            ps.setBytes(6, post.getImage());
            ps.setTimestamp(7, Timestamp.valueOf(post.getCreatedAt()));
            ps.setTimestamp(8, Timestamp.valueOf(post.getUpdatedAt()));
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            post.setId(keyHolder.getKey().longValue());
        }

        return post;
    }

    private Post update(Post post) {
        String sql = "UPDATE posts SET title = ?, text = ?, tags = ?, likes_count = ?, " +
                "comments_count = ?, image = ?, updated_at = ? WHERE id = ?";

        post.setUpdatedAt(LocalDateTime.now());

        jdbcTemplate.update(sql,
                post.getTitle(),
                post.getText(),
                post.getTags() != null ? String.join(",", post.getTags()) : null,
                post.getLikesCount(),
                post.getCommentsCount(),
                post.getImage(),
                Timestamp.valueOf(post.getUpdatedAt()),
                post.getId());

        return post;
    }

    public Post findById(Long id) {
        String sql = "SELECT * FROM posts WHERE id = ?";
        List<Post> posts = jdbcTemplate.query(sql, postRowMapper, id);
        return posts.isEmpty() ? null : posts.get(0);
    }

    public List<Post> findAll(int offset, int limit, String searchQuery, List<String> tags) {
        StringBuilder sql = new StringBuilder("SELECT * FROM posts WHERE 1=1");
        List<Object> params = new java.util.ArrayList<>();

        if (searchQuery != null && !searchQuery.isEmpty()) {
            sql.append(" AND LOWER(title) LIKE LOWER(?)");
            params.add("%" + searchQuery + "%");
        }

        if (tags != null && !tags.isEmpty()) {
            for (String tag : tags) {
                sql.append(" AND tags LIKE ?");
                params.add("%" + tag + "%");
            }
        }

        sql.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        return jdbcTemplate.query(sql.toString(), postRowMapper, params.toArray());
    }

    public int count(String searchQuery, List<String> tags) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM posts WHERE 1=1");
        List<Object> params = new java.util.ArrayList<>();

        if (searchQuery != null && !searchQuery.isEmpty()) {
            sql.append(" AND LOWER(title) LIKE LOWER(?)");
            params.add("%" + searchQuery + "%");
        }

        if (tags != null && !tags.isEmpty()) {
            for (String tag : tags) {
                sql.append(" AND tags LIKE ?");
                params.add("%" + tag + "%");
            }
        }

        return jdbcTemplate.queryForObject(sql.toString(), Integer.class, params.toArray());
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM posts WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public void incrementLikes(Long id) {
        String sql = "UPDATE posts SET likes_count = likes_count + 1 WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public void updateImage(Long id, byte[] image) {
        String sql = "UPDATE posts SET image = ?, updated_at = ? WHERE id = ?";
        jdbcTemplate.update(sql, image, Timestamp.valueOf(LocalDateTime.now()), id);
    }

    public byte[] findImageById(Long id) {
        String sql = "SELECT image FROM posts WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, byte[].class, id);
    }
}
