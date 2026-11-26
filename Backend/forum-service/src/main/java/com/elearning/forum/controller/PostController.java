package com.elearning.forum.controller;

import com.elearning.forum.dto.*;
import com.elearning.forum.security.AuthUser;
import com.elearning.forum.service.CommentService;
import com.elearning.forum.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/forum/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final CommentService commentService;

    /**
     * POST /api/v1/forum/posts
     * Create a new post. STUDENT, TEACHER, ADMIN.
     */
    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @Valid @RequestBody CreatePostRequest request,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        PostResponse created = postService.createPost(request, authUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * GET /api/v1/forum/posts
     * List posts with optional pagination, categoryId filter and title search.
     * Pinned posts appear first.
     */
    @GetMapping
    public ResponseEntity<PageResponse<PostResponse>> getPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        PageResponse<PostResponse> result = postService.getPosts(
                page, size, categoryId, search, authUser.userId()
        );
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/v1/forum/posts/{id}
     * Get a single post by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPost(
            @PathVariable String id,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ResponseEntity.ok(postService.getPostById(id, authUser.userId()));
    }

    /**
     * PUT /api/v1/forum/posts/{id}
     * Update title/content. Author or ADMIN. Locked posts cannot be edited (unless ADMIN).
     */
    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable String id,
            @Valid @RequestBody UpdatePostRequest request,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ResponseEntity.ok(postService.updatePost(id, request, authUser));
    }

    /**
     * DELETE /api/v1/forum/posts/{id}
     * Delete a post and all its comments. Author or ADMIN.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable String id,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        postService.deletePost(id, authUser);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/v1/forum/posts/{id}/like
     * Toggle like on a post.
     */
    @PostMapping("/{id}/like")
    public ResponseEntity<LikeResponse> toggleLike(
            @PathVariable String id,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ResponseEntity.ok(postService.toggleLike(id, authUser.userId()));
    }

    /**
     * POST /api/v1/forum/posts/{id}/comments
     * Add a comment to a post. Fails if post is locked.
     */
    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentResponse> addComment(
            @PathVariable String id,
            @Valid @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        CommentResponse created = commentService.createComment(id, request, authUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * GET /api/v1/forum/posts/{id}/comments
     * List comments for a post, oldest first.
     */
    @GetMapping("/{id}/comments")
    public ResponseEntity<PageResponse<CommentResponse>> getComments(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(commentService.getCommentsByPost(id, page, size));
    }
}
