package com.elearning.forum.service;

import com.elearning.forum.dto.*;
import com.elearning.forum.exception.ConflictException;
import com.elearning.forum.exception.ForbiddenOperationException;
import com.elearning.forum.exception.ResourceNotFoundException;
import com.elearning.forum.model.ForumCategory;
import com.elearning.forum.model.Post;
import com.elearning.forum.repository.CommentRepository;
import com.elearning.forum.repository.ForumCategoryRepository;
import com.elearning.forum.repository.PostRepository;
import com.elearning.forum.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final ForumCategoryRepository categoryRepository;
    private final CommentRepository commentRepository;

    // ── Create ───────────────────────────────────────────────────────────────

    public PostResponse createPost(CreatePostRequest request, AuthUser author) {
        ForumCategory category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("ForumCategory", request.categoryId()));

        Post post = Post.builder()
                .title(request.title())
                .content(request.content())
                .categoryId(request.categoryId())
                .authorId(author.userId())
                .authorEmail(author.email())
                .authorRole(author.role())
                .build();

        Post saved = postRepository.save(post);
        return toResponse(saved, category.getName(), author.userId());
    }

    // ── List (paginated, filtered, sorted) ──────────────────────────────────

    public PageResponse<PostResponse> getPosts(
            int page, int size,
            String categoryId, String search,
            String currentUserId
    ) {
        // We always fetch with pinned-first sort: sort by pinned desc, createdAt desc
        // MongoDB doesn't support sorting on boolean easily via derived queries,
        // so we sort in-memory after fetching — but for large datasets we use a Sort
        // that sorts pinned desc (true > false in MongoDB with -1 means false first,
        // so we use a workaround: fetch and sort in memory for correctness)
        Pageable pageable = PageRequest.of(page, size, Sort.by(
                Sort.Order.desc("pinned"),
                Sort.Order.desc("createdAt")
        ));

        Page<Post> postPage;
        boolean hasCategoryId = StringUtils.hasText(categoryId);
        boolean hasSearch = StringUtils.hasText(search);

        if (hasCategoryId && hasSearch) {
            postPage = postRepository.findByCategoryIdAndTitleContainingIgnoreCase(categoryId, search, pageable);
        } else if (hasCategoryId) {
            postPage = postRepository.findByCategoryId(categoryId, pageable);
        } else if (hasSearch) {
            postPage = postRepository.findByTitleContainingIgnoreCase(search, pageable);
        } else {
            postPage = postRepository.findAll(pageable);
        }

        // Enrich with category names
        List<PostResponse> content = postPage.getContent().stream()
                .map(post -> {
                    String catName = categoryRepository.findById(post.getCategoryId())
                            .map(ForumCategory::getName)
                            .orElse("Unknown");
                    return toResponse(post, catName, currentUserId);
                })
                .toList();

        return new PageResponse<>(
                content,
                postPage.getNumber(),
                postPage.getSize(),
                postPage.getTotalElements(),
                postPage.getTotalPages()
        );
    }

    // ── Get by ID ────────────────────────────────────────────────────────────

    public PostResponse getPostById(String postId, String currentUserId) {
        Post post = findPostOrThrow(postId);
        String catName = categoryRepository.findById(post.getCategoryId())
                .map(ForumCategory::getName)
                .orElse("Unknown");
        return toResponse(post, catName, currentUserId);
    }

    // ── Update ───────────────────────────────────────────────────────────────

    public PostResponse updatePost(String postId, UpdatePostRequest request, AuthUser editor) {
        Post post = findPostOrThrow(postId);

        // Only author or ADMIN may edit
        if (!post.getAuthorId().equals(editor.userId()) && !editor.isAdmin()) {
            throw new ForbiddenOperationException("You are not allowed to edit this post");
        }
        // Locked posts cannot be edited unless ADMIN
        if (post.isLocked() && !editor.isAdmin()) {
            throw new ConflictException("This post is locked and cannot be edited");
        }

        post.setTitle(request.title());
        post.setContent(request.content());
        Post saved = postRepository.save(post);

        String catName = categoryRepository.findById(saved.getCategoryId())
                .map(ForumCategory::getName)
                .orElse("Unknown");
        return toResponse(saved, catName, editor.userId());
    }

    // ── Delete ───────────────────────────────────────────────────────────────

    public void deletePost(String postId, AuthUser deleter) {
        Post post = findPostOrThrow(postId);

        if (!post.getAuthorId().equals(deleter.userId()) && !deleter.isAdmin()) {
            throw new ForbiddenOperationException("You are not allowed to delete this post");
        }

        commentRepository.deleteAllByPostId(postId);
        postRepository.delete(post);
    }

    // ── Like (toggle) ────────────────────────────────────────────────────────

    public LikeResponse toggleLike(String postId, String userId) {
        Post post = findPostOrThrow(postId);

        boolean alreadyLiked = post.getLikedBy().contains(userId);
        if (alreadyLiked) {
            post.getLikedBy().remove(userId);
        } else {
            post.getLikedBy().add(userId);
        }
        postRepository.save(post);

        return new LikeResponse(!alreadyLiked, post.getLikedBy().size());
    }

    // ── Admin: Pin / Lock ────────────────────────────────────────────────────

    public PostResponse pinPost(String postId, boolean pinned, String adminUserId) {
        Post post = findPostOrThrow(postId);
        post.setPinned(pinned);
        Post saved = postRepository.save(post);
        String catName = categoryRepository.findById(saved.getCategoryId())
                .map(ForumCategory::getName)
                .orElse("Unknown");
        return toResponse(saved, catName, adminUserId);
    }

    public PostResponse lockPost(String postId, boolean locked, String adminUserId) {
        Post post = findPostOrThrow(postId);
        post.setLocked(locked);
        Post saved = postRepository.save(post);
        String catName = categoryRepository.findById(saved.getCategoryId())
                .map(ForumCategory::getName)
                .orElse("Unknown");
        return toResponse(saved, catName, adminUserId);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    public Post findPostOrThrow(String postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", postId));
    }

    public PostResponse toResponse(Post post, String categoryName, String currentUserId) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getCategoryId(),
                categoryName,
                post.getAuthorId(),
                post.getAuthorEmail(),
                post.getAuthorRole(),
                post.isPinned(),
                post.isLocked(),
                post.getLikedBy().size(),
                currentUserId != null && post.getLikedBy().contains(currentUserId),
                post.getCommentCount(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
