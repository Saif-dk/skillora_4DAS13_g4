package com.elearning.forum.repository;

import com.elearning.forum.model.ForumCategory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ForumCategoryRepository extends MongoRepository<ForumCategory, String> {

    boolean existsByName(String name);

    Optional<ForumCategory> findByName(String name);
}
