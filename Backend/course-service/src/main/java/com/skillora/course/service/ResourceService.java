package com.skillora.course.service;

import com.skillora.course.api.dto.ResourceCreateRequest;
import com.skillora.course.api.dto.ResourceResponse;

public interface ResourceService {

    ResourceResponse createResource(ResourceCreateRequest request, Long userId, String role);

    ResourceResponse getResourceById(Long resourceId, Long userId, String role);

    void deleteResource(Long resourceId, Long userId, String role);
}
