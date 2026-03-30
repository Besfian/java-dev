package com.blog.service;

import com.blog.dto.PostRequest;
import com.blog.dto.PostResponse;
import com.blog.dto.PostsPageResponse;
import com.blog.exception.PostNotFoundException;
import com.blog.model.Post;
import com.blog.repository.CommentRepository;
import com.blog.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Autowired
    public PostServiceImpl(PostRepository postRepository, CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    @Override
    public PostResponse createPost(PostRequest postRequest) {
        Post post = new Post();
        post.setTitle(postRequest.getTitle());
        post.setText(postRequest.getText());
        post.setTags(postRequest.getTags());
        post.setLikesCount(0);
        post.setCommentsCount(0);

        Post savedPost = postRepository.save(post);
        return convertToResponse(savedPost, false);
    }

    @Override
    public PostResponse updatePost(Long id, PostRequest postRequest) {
        Post existingPost = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));

        existingPost.setTitle(postRequest.getTitle());
        existingPost.setText(postRequest.getText());
        existingPost.setTags(postRequest.getTags());

        Post updatedPost = postRepository.save(existingPost);
        return convertToResponse(updatedPost, true);
    }

    @Override
    public PostResponse getPost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
        return convertToResponse(post, true);
    }

    @Override
    public PostsPageResponse getPosts(String search, int pageNumber, int pageSize) {
        String searchQuery = null;
        List<String> tags = new ArrayList<>();

        if (search != null && !search.isEmpty()) {
            String[] words = search.split("\\s+");
            List<String> searchWords = new ArrayList<>();

            for (String word : words) {
                if (word.trim().isEmpty()) continue;

                if (word.startsWith("#")) {
                    String tag = word.substring(1).trim();
                    if (!tag.isEmpty()) {
                        tags.add(tag);
                    }
                } else {
                    searchWords.add(word);
                }
            }

            if (!searchWords.isEmpty()) {
                searchQuery = String.join(" ", searchWords);
            }
        }

        int offset = (pageNumber - 1) * pageSize;
        List<Post> posts = postRepository.findAll(offset, pageSize, searchQuery, tags.isEmpty() ? null : tags);
        int totalPosts = postRepository.count(searchQuery, tags.isEmpty() ? null : tags);

        int lastPage = (int) Math.ceil((double) totalPosts / pageSize);
        if (lastPage == 0) lastPage = 1;

        boolean hasPrev = pageNumber > 1;
        boolean hasNext = pageNumber < lastPage;

        List<PostResponse> postResponses = posts.stream()
                .map(post -> convertToResponse(post, false))
                .collect(Collectors.toList());

        return new PostsPageResponse(postResponses, hasPrev, hasNext, lastPage);
    }

    @Override
    public void deletePost(Long id) {
        postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
        commentRepository.deleteByPostId(id);
        postRepository.deleteById(id);
    }

    @Override
    public int likePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
        postRepository.incrementLikes(id);
        return post.getLikesCount() + 1;
    }

    @Override
    public void updateImage(Long id, byte[] image) {
        postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));

        if (image == null || image.length == 0) {
            throw new IllegalArgumentException("Image is empty");
        }

        postRepository.updateImage(id, image);
    }

    @Override
    public byte[] getImage(Long id) {
        return postRepository.findImageById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
    }

    private PostResponse convertToResponse(Post post, boolean fullText) {
        PostResponse response = new PostResponse();
        response.setId(post.getId());
        response.setTitle(post.getTitle());

        if (fullText || post.getText().length() <= 128) {
            response.setText(post.getText());
        } else {
            response.setText(post.getText().substring(0, 125) + "...");
        }

        response.setTags(post.getTags());
        response.setLikesCount(post.getLikesCount());
        response.setCommentsCount(post.getCommentsCount());

        return response;
    }
}