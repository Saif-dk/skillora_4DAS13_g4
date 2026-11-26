package com.elearning.forum.repository;

import com.elearning.forum.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface PostRepository extends MongoRepository<Post, String> {

    Page<Post> findByCategoryId(String categoryId, Pageable pageable);

    @Query("{'title': {$regex: ?0, $options: 'i'}}")
    Page<Post> findByTitleContainingIgnoreCase(String search, Pageable pageable);

    @Query("{'categoryId': ?0, 'title': {$regex: ?1, $options: 'i'}}")
    Page<Post> findByCategoryIdAndTitleContainingIgnoreCase(String categoryId, String search, Pageable pageable);

    void deleteAllByCategoryId(String categoryId);
}
