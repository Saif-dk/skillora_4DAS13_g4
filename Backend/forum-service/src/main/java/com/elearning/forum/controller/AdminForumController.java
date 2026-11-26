package com.elearning.forum.controller;

import com.elearning.forum.dto.LockPostRequest;
import com.elearning.forum.dto.PinPostRequest;
import com.elearning.forum.dto.PostResponse;
import com.elearning.forum.security.AuthUser;
import com.elearning.forum.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/forum/posts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminForumController {

    private final PostService postService;

    /**
     * PATCH /api/v1/admin/forum/posts/{id}/pin
     * Pin or unpin a post. ADMIN only.
     */
    @PatchMapping("/{id}/pin")
    public ResponseEntity<PostResponse> pinPost(
            @PathVariable String id,
            @Valid @RequestBody PinPostRequest request,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ResponseEntity.ok(postService.pinPost(id, request.pinned(), authUser.userId()));
    }

    /**
     * PATCH /api/v1/admin/forum/posts/{id}/lock
     * Lock or unlock a post. ADMIN only.
     */
    @PatchMapping("/{id}/lock")
    public ResponseEntity<PostResponse> lockPost(
            @PathVariable String id,
            @Valid @RequestBody LockPostRequest request,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ResponseEntity.ok(postService.lockPost(id, request.locked(), authUser.userId()));
    }
}
