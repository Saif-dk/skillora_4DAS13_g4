package com.elearning.forum.service;

import com.elearning.forum.dto.CategoryResponse;
import com.elearning.forum.dto.CreateCategoryRequest;
import com.elearning.forum.exception.ConflictException;
import com.elearning.forum.model.ForumCategory;
import com.elearning.forum.repository.ForumCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final ForumCategoryRepository categoryRepository;

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public CategoryResponse createCategory(CreateCategoryRequest request) {
        if (categoryRepository.existsByName(request.name())) {
            throw new ConflictException("A category with name '" + request.name() + "' already exists");
        }
        ForumCategory category = ForumCategory.builder()
                .name(request.name())
                .description(request.description())
                .build();
        ForumCategory saved = categoryRepository.save(category);
        return toResponse(saved);
    }

    public CategoryResponse toResponse(ForumCategory category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getDescription());
    }
}
