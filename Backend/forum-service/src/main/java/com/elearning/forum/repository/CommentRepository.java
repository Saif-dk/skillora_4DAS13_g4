package com.elearning.forum.repository;

import com.elearning.forum.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CommentRepository extends MongoRepository<Comment, String> {

    Page<Comment> findByPostId(String postId, Pageable pageable);

    void deleteAllByPostId(String postId);

    long countByPostId(String postId);
}
