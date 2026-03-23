package com.blog.repository;


import com.blog.model.Post;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public class PostRepository {

    private final JdbcClient jdbcClient;

    @Autowired
    public PostRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
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
                "VALUES (:title, :text, :tags, :likesCount, :commentsCount, :image, :createdAt, :updatedAt)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        LocalDateTime now = LocalDateTime.now();
        post.setCreatedAt(now);
        post.setUpdatedAt(now);

        jdbcClient.sql(sql)
                .param("title", post.getTitle())
                .param("text", post.getText())
                .param("tags", post.getTags() != null ? String.join(",", post.getTags()) : null)
                .param("likesCount", post.getLikesCount())
                .param("commentsCount", post.getCommentsCount())
                .param("image", post.getImage())
                .param("createdAt", Timestamp.valueOf(post.getCreatedAt()))
                .param("updatedAt", Timestamp.valueOf(post.getUpdatedAt()))
                .update(keyHolder, "id");

        if (keyHolder.getKey() != null) {
            post.setId(keyHolder.getKey().longValue());
        }

        return post;
    }

    private Post update(Post post) {
        String sql = "UPDATE posts SET title = :title, text = :text, tags = :tags, likes_count = :likesCount, " +
                "comments_count = :commentsCount, image = :image, updated_at = :updatedAt WHERE id = :id";

        post.setUpdatedAt(LocalDateTime.now());

        jdbcClient.sql(sql)
                .param("title", post.getTitle())
                .param("text", post.getText())
                .param("tags", post.getTags() != null ? String.join(",", post.getTags()) : null)
                .param("likesCount", post.getLikesCount())
                .param("commentsCount", post.getCommentsCount())
                .param("image", post.getImage())
                .param("updatedAt", Timestamp.valueOf(post.getUpdatedAt()))
                .param("id", post.getId())
                .update();

        return post;
    }

    public Optional<Post> findById(Long id) {
        String sql = "SELECT * FROM posts WHERE id = :id";
        return jdbcClient.sql(sql)
                .param("id", id)
                .query(postRowMapper)
                .optional();
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

        return jdbcClient.sql(sql.toString())
                .params(params.toArray())
                .query(postRowMapper)
                .list();
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

        return jdbcClient.sql(sql.toString())
                .params(params.toArray())
                .query(Integer.class)
                .single();
    }

    public void deleteById(Long id) {
        String sql = "DELETE FROM posts WHERE id = :id";
        jdbcClient.sql(sql).param("id", id).update();
    }

    public void incrementLikes(Long id) {
        String sql = "UPDATE posts SET likes_count = likes_count + 1 WHERE id = :id";
        jdbcClient.sql(sql).param("id", id).update();
    }

    public void updateImage(Long id, byte[] image) {
        String sql = "UPDATE posts SET image = :image, updated_at = :updatedAt WHERE id = :id";
        jdbcClient.sql(sql)
                .param("image", image)
                .param("updatedAt", Timestamp.valueOf(LocalDateTime.now()))
                .param("id", id)
                .update();
    }

    public Optional<byte[]> findImageById(Long id) {
        String sql = "SELECT image FROM posts WHERE id = :id";
        return jdbcClient.sql(sql)
                .param("id", id)
                .query(byte[].class)
                .optional();
    }

    public void updateCommentsCount(Long id, int delta) {
        String sql = "UPDATE posts SET comments_count = comments_count + :delta, updated_at = :updatedAt WHERE id = :id";
        jdbcClient.sql(sql)
                .param("delta", delta)
                .param("updatedAt", Timestamp.valueOf(LocalDateTime.now()))
                .param("id", id)
                .update();
    }

}