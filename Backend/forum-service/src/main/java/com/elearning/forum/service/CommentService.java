package com.elearning.forum.service;

import com.elearning.forum.dto.CommentResponse;
import com.elearning.forum.dto.CreateCommentRequest;
import com.elearning.forum.dto.PageResponse;
import com.elearning.forum.exception.ConflictException;
import com.elearning.forum.exception.ForbiddenOperationException;
import com.elearning.forum.exception.ResourceNotFoundException;
import com.elearning.forum.model.Comment;
import com.elearning.forum.model.Post;
import com.elearning.forum.repository.CommentRepository;
import com.elearning.forum.repository.PostRepository;
import com.elearning.forum.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    // ── Create Comment ───────────────────────────────────────────────────────

    public CommentResponse createComment(String postId, CreateCommentRequest request, AuthUser author) {
        Post post = findPostOrThrow(postId);

        if (post.isLocked()) {
            throw new ConflictException("Cannot comment on a locked post");
        }

        Comment comment = Comment.builder()
                .postId(postId)
                .content(request.content())
                .authorId(author.userId())
                .authorEmail(author.email())
                .authorRole(author.role())
                .build();

        Comment saved = commentRepository.save(comment);

        // Increment post comment count
        post.setCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);

        return toResponse(saved);
    }

    // ── List Comments ────────────────────────────────────────────────────────

    public PageResponse<CommentResponse> getCommentsByPost(String postId, int page, int size) {
        // Ensure post exists
        findPostOrThrow(postId);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.asc("createdAt")));
        Page<Comment> commentPage = commentRepository.findByPostId(postId, pageable);

        return new PageResponse<>(
                commentPage.getContent().stream().map(this::toResponse).toList(),
                commentPage.getNumber(),
                commentPage.getSize(),
                commentPage.getTotalElements(),
                commentPage.getTotalPages()
        );
    }

    // ── Delete Comment ───────────────────────────────────────────────────────

    public void deleteComment(String commentId, AuthUser deleter) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", commentId));

        if (!comment.getAuthorId().equals(deleter.userId()) && !deleter.isAdmin()) {
            throw new ForbiddenOperationException("You are not allowed to delete this comment");
        }

        // Decrement post comment count
        postRepository.findById(comment.getPostId()).ifPresent(post -> {
            int newCount = Math.max(0, post.getCommentCount() - 1);
            post.setCommentCount(newCount);
            postRepository.save(post);
        });

        commentRepository.delete(comment);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Post findPostOrThrow(String postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", postId));
    }

    private CommentResponse toResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getPostId(),
                comment.getContent(),
                comment.getAuthorId(),
                comment.getAuthorEmail(),
                comment.getAuthorRole(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
