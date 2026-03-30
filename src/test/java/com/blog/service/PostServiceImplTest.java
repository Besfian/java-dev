package com.blog.service;

import com.blog.dto.PostRequest;
import com.blog.dto.PostResponse;
import com.blog.dto.PostsPageResponse;
import com.blog.exception.PostNotFoundException;
import com.blog.model.Post;
import com.blog.repository.CommentRepository;
import com.blog.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private PostServiceImpl postService;

    private Post testPost;
    private PostRequest testPostRequest;

    @BeforeEach
    void setUp() {
        testPost = new Post();
        testPost.setId(1L);
        testPost.setTitle("Test Post");
        testPost.setText("This is a test post content");
        testPost.setTags(Arrays.asList("test", "java"));
        testPost.setLikesCount(0);
        testPost.setCommentsCount(0);

        testPostRequest = new PostRequest();
        testPostRequest.setTitle("Test Post");
        testPostRequest.setText("This is a test post content");
        testPostRequest.setTags(Arrays.asList("test", "java"));
    }


    @Test
    void createPost_shouldSaveAndReturnPostResponse() {
        when(postRepository.save(any(Post.class))).thenReturn(testPost);

        PostResponse response = postService.createPost(testPostRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Test Post");
        assertThat(response.getText()).isEqualTo("This is a test post content");
        assertThat(response.getTags()).containsExactly("test", "java");
        assertThat(response.getLikesCount()).isZero();
        assertThat(response.getCommentsCount()).isZero();

        verify(postRepository, times(1)).save(any(Post.class));
    }


    @Test
    void getPost_shouldReturnPostResponse_whenPostExists() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));

        PostResponse response = postService.getPost(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Test Post");
        assertThat(response.getText()).isEqualTo("This is a test post content");

        verify(postRepository, times(1)).findById(1L);
    }

    @Test
    void getPost_shouldThrowPostNotFoundException_whenPostDoesNotExist() {
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getPost(99L))
                .isInstanceOf(PostNotFoundException.class)
                .hasMessageContaining("99");

        verify(postRepository, times(1)).findById(99L);
    }


    @Test
    void updatePost_shouldUpdateAndReturnPostResponse_whenPostExists() {
        PostRequest updateRequest = new PostRequest();
        updateRequest.setTitle("Updated Title");
        updateRequest.setText("Updated content");
        updateRequest.setTags(Arrays.asList("updated"));

        Post updatedPost = new Post();
        updatedPost.setId(1L);
        updatedPost.setTitle("Updated Title");
        updatedPost.setText("Updated content");
        updatedPost.setTags(Arrays.asList("updated"));
        updatedPost.setLikesCount(0);
        updatedPost.setCommentsCount(0);

        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(postRepository.save(any(Post.class))).thenReturn(updatedPost);

        PostResponse response = postService.updatePost(1L, updateRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Updated Title");
        assertThat(response.getText()).isEqualTo("Updated content");
        assertThat(response.getTags()).containsExactly("updated");

        verify(postRepository, times(1)).findById(1L);
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    void updatePost_shouldThrowPostNotFoundException_whenPostDoesNotExist() {
        PostRequest updateRequest = new PostRequest();
        updateRequest.setTitle("Updated Title");
        updateRequest.setText("Updated content");

        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.updatePost(99L, updateRequest))
                .isInstanceOf(PostNotFoundException.class)
                .hasMessageContaining("99");

        verify(postRepository, times(1)).findById(99L);
        verify(postRepository, never()).save(any(Post.class));
    }


    @Test
    void deletePost_shouldDeletePostAndComments_whenPostExists() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        doNothing().when(commentRepository).deleteByPostId(1L);
        doNothing().when(postRepository).deleteById(1L);

        postService.deletePost(1L);

        verify(postRepository, times(1)).findById(1L);
        verify(commentRepository, times(1)).deleteByPostId(1L);
        verify(postRepository, times(1)).deleteById(1L);
    }

    @Test
    void deletePost_shouldThrowPostNotFoundException_whenPostDoesNotExist() {
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.deletePost(99L))
                .isInstanceOf(PostNotFoundException.class)
                .hasMessageContaining("99");

        verify(postRepository, times(1)).findById(99L);
        verify(commentRepository, never()).deleteByPostId(anyLong());
        verify(postRepository, never()).deleteById(anyLong());
    }


    @Test
    void likePost_shouldIncrementLikesAndReturnNewCount_whenPostExists() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        doNothing().when(postRepository).incrementLikes(1L);

        int likesCount = postService.likePost(1L);

        assertThat(likesCount).isEqualTo(1); // было 0, стало 1

        verify(postRepository, times(1)).findById(1L);
        verify(postRepository, times(1)).incrementLikes(1L);
    }

    @Test
    void likePost_shouldThrowPostNotFoundException_whenPostDoesNotExist() {
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.likePost(99L))
                .isInstanceOf(PostNotFoundException.class)
                .hasMessageContaining("99");

        verify(postRepository, times(1)).findById(99L);
        verify(postRepository, never()).incrementLikes(anyLong());
    }


    @Test
    void getPosts_shouldReturnPageResponse_withoutSearch() {
        List<Post> posts = Arrays.asList(testPost);
        when(postRepository.findAll(0, 5, null, null)).thenReturn(posts);
        when(postRepository.count(null, null)).thenReturn(1);

        PostsPageResponse response = postService.getPosts("", 1, 5);

        assertThat(response).isNotNull();
        assertThat(response.getPosts()).hasSize(1);
        assertThat(response.isHasPrev()).isFalse();
        assertThat(response.isHasNext()).isFalse();
        assertThat(response.getLastPage()).isEqualTo(1);

        verify(postRepository, times(1)).findAll(0, 5, null, null);
        verify(postRepository, times(1)).count(null, null);
    }

    @Test
    void getPosts_shouldReturnPageResponse_withSearchQuery() {
        List<Post> posts = Arrays.asList(testPost);
        when(postRepository.findAll(0, 5, "test", null)).thenReturn(posts);
        when(postRepository.count("test", null)).thenReturn(1);

        PostsPageResponse response = postService.getPosts("test", 1, 5);

        assertThat(response).isNotNull();
        assertThat(response.getPosts()).hasSize(1);

        verify(postRepository, times(1)).findAll(0, 5, "test", null);
        verify(postRepository, times(1)).count("test", null);
    }

    @Test
    void getPosts_shouldReturnPageResponse_withTagSearch() {
        List<Post> posts = Arrays.asList(testPost);
        List<String> tags = Arrays.asList("java");
        when(postRepository.findAll(0, 5, null, tags)).thenReturn(posts);
        when(postRepository.count(null, tags)).thenReturn(1);

        PostsPageResponse response = postService.getPosts("#java", 1, 5);

        assertThat(response).isNotNull();
        assertThat(response.getPosts()).hasSize(1);

        verify(postRepository, times(1)).findAll(0, 5, null, tags);
        verify(postRepository, times(1)).count(null, tags);
    }

    @Test
    void getPosts_shouldReturnPageResponse_withMultiplePages() {
        List<Post> posts = Arrays.asList(testPost);
        when(postRepository.findAll(0, 5, null, null)).thenReturn(posts);
        when(postRepository.count(null, null)).thenReturn(10);

        PostsPageResponse response = postService.getPosts("", 1, 5);

        assertThat(response).isNotNull();
        assertThat(response.getPosts()).hasSize(1);
        assertThat(response.isHasPrev()).isFalse();
        assertThat(response.isHasNext()).isTrue();
        assertThat(response.getLastPage()).isEqualTo(2);
    }

    @Test
    void getPosts_shouldReturnEmptyPage_whenNoPosts() {
        when(postRepository.findAll(0, 5, null, null)).thenReturn(Arrays.asList());
        when(postRepository.count(null, null)).thenReturn(0);

        PostsPageResponse response = postService.getPosts("", 1, 5);

        assertThat(response).isNotNull();
        assertThat(response.getPosts()).isEmpty();
        assertThat(response.isHasPrev()).isFalse();
        assertThat(response.isHasNext()).isFalse();
        assertThat(response.getLastPage()).isEqualTo(1);
    }


    @Test
    void updateImage_shouldUpdateImage_whenPostExistsAndImageValid() {
        byte[] image = "test image".getBytes();
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));
        doNothing().when(postRepository).updateImage(1L, image);

        postService.updateImage(1L, image);

        verify(postRepository, times(1)).findById(1L);
        verify(postRepository, times(1)).updateImage(1L, image);
    }

    @Test
    void updateImage_shouldThrowPostNotFoundException_whenPostDoesNotExist() {
        byte[] image = "test image".getBytes();
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.updateImage(99L, image))
                .isInstanceOf(PostNotFoundException.class)
                .hasMessageContaining("99");

        verify(postRepository, times(1)).findById(99L);
        verify(postRepository, never()).updateImage(anyLong(), any());
    }

    @Test
    void updateImage_shouldThrowIllegalArgumentException_whenImageEmpty() {
        byte[] emptyImage = new byte[0];
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));

        assertThatThrownBy(() -> postService.updateImage(1L, emptyImage))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Image is empty");

        verify(postRepository, times(1)).findById(1L);
        verify(postRepository, never()).updateImage(anyLong(), any());
    }

    @Test
    void updateImage_shouldThrowIllegalArgumentException_whenImageNull() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(testPost));

        assertThatThrownBy(() -> postService.updateImage(1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Image is empty");

        verify(postRepository, times(1)).findById(1L);
        verify(postRepository, never()).updateImage(anyLong(), any());
    }


    @Test
    void getImage_shouldReturnImage_whenPostExistsAndImagePresent() {
        byte[] image = "test image".getBytes();
        when(postRepository.findImageById(1L)).thenReturn(Optional.of(image));

        byte[] result = postService.getImage(1L);

        assertThat(result).isEqualTo(image);
        verify(postRepository, times(1)).findImageById(1L);
    }

    @Test
    void getImage_shouldThrowPostNotFoundException_whenPostDoesNotExist() {
        when(postRepository.findImageById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getImage(99L))
                .isInstanceOf(PostNotFoundException.class)
                .hasMessageContaining("99");

        verify(postRepository, times(1)).findImageById(99L);
    }


    @Test
    void convertToResponse_shouldTruncateLongText_whenFullTextFalse() {
        Post longTextPost = new Post();
        longTextPost.setId(1L);
        longTextPost.setTitle("Long Post");
        String longText = "a".repeat(200);
        longTextPost.setText(longText);
        longTextPost.setTags(Arrays.asList("test"));
        longTextPost.setLikesCount(5);
        longTextPost.setCommentsCount(2);

        when(postRepository.findById(1L)).thenReturn(Optional.of(longTextPost));

        PostResponse response = postService.getPost(1L); // fullText = true, не обрезается

        assertThat(response.getText()).hasSize(200);
    }

    @Test
    void convertToResponse_shouldNotTruncateShortText() {
        Post shortTextPost = new Post();
        shortTextPost.setId(1L);
        shortTextPost.setTitle("Short Post");
        String shortText = "Short text";
        shortTextPost.setText(shortText);
        shortTextPost.setTags(Arrays.asList("test"));
        shortTextPost.setLikesCount(5);
        shortTextPost.setCommentsCount(2);

        when(postRepository.findById(1L)).thenReturn(Optional.of(shortTextPost));

        PostResponse response = postService.getPost(1L);

        assertThat(response.getText()).isEqualTo(shortText);
    }
}