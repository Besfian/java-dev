package com.blog.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    private Long id;
    private String title;
    private String text;
    private List<String> tags = new ArrayList<>();
    private int likesCount;
    private int commentsCount;
    private byte[] image;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
