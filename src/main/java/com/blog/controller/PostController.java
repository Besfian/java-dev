package com.blog.controller;


import com.blog.dto.PostRequest;
import com.blog.dto.PostResponse;
import com.blog.dto.PostsPageResponse;
import com.blog.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public ResponseEntity<PostsPageResponse> getPosts(
            @RequestParam(value = "search", defaultValue = "") String search,
            @RequestParam(value = "pageNumber", defaultValue = "1") int pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "5") int pageSize) {

        PostsPageResponse response = postService.getPosts(search, pageNumber, pageSize);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPost(@PathVariable Long id) {
        try {
            PostResponse post = postService.getPost(id);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(post);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<PostResponse> createPost(@RequestBody PostRequest postRequest) {
        PostResponse createdPost = postService.createPost(postRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long id,
            @RequestBody PostRequest postRequest) {
        try {
            PostResponse updatedPost = postService.updatePost(id, postRequest);
            return ResponseEntity.ok(updatedPost);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        try {
            postService.deletePost(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/likes")
    public ResponseEntity<Integer> likePost(@PathVariable Long id) {
        try {
            int likesCount = postService.likePost(id);
            return ResponseEntity.ok(likesCount);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> updateImage(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile image) {
        try {
            postService.updateImage(id, image.getBytes());
            return ResponseEntity.ok().build();
        } catch (IOException | RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }



    @GetMapping(value = "/{id}/image", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_GIF_VALUE, MediaType.IMAGE_PNG_VALUE})
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) {
        try {
            byte[] image = postService.getImage(id);
            if (image == null || image.length == 0) {
                byte[] transparentPixel = Base64.getDecoder().decode("R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7");
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_GIF)
                        .body(transparentPixel);
            }

            MediaType mediaType = MediaType.IMAGE_JPEG;
            if (image.length > 4) {
                if (image[0] == (byte) 0x89 && image[1] == (byte) 0x50 && image[2] == (byte) 0x4E && image[3] == (byte) 0x47) {
                    mediaType = MediaType.IMAGE_PNG;
                } else if (image[0] == (byte) 0x47 && image[1] == (byte) 0x49 && image[2] == (byte) 0x46) {
                    mediaType = MediaType.IMAGE_GIF;
                }
            }

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(image);
        } catch (RuntimeException e) {
            byte[] transparentPixel = Base64.getDecoder().decode("R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7");
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_GIF)
                    .body(transparentPixel);
        }
    }
}