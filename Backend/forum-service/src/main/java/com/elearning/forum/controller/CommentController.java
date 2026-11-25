package com.elearning.forum.controller;

import com.elearning.forum.security.AuthUser;
import com.elearning.forum.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/forum/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * DELETE /api/v1/forum/comments/{commentId}
     * Delete a comment. Author or ADMIN. Decrements post commentCount.
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable String commentId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        commentService.deleteComment(commentId, authUser);
        return ResponseEntity.noContent().build();
    }
}
