package com.skillora.course.mapper;

import com.skillora.course.api.dto.ResourceCreateRequest;
import com.skillora.course.api.dto.ResourceResponse;
import com.skillora.course.domain.entity.Resource;

import java.util.List;

public final class ResourceMapper {

    private ResourceMapper() {
    }

    public static Resource toEntity(ResourceCreateRequest request) {
        Resource resource = new Resource();
        resource.setName(request.name());
        resource.setType(request.type());
        resource.setUrl(request.url());
        resource.setSize(request.size());
        return resource;
    }

    public static ResourceResponse toResponse(Resource resource) {
        if (resource == null) {
            return null;
        }
        return new ResourceResponse(
                resource.getId(),
                resource.getCourse() == null ? null : resource.getCourse().getId(),
                resource.getLesson() == null ? null : resource.getLesson().getId(),
                resource.getName(),
                resource.getType(),
                resource.getUrl(),
                resource.getSize(),
                resource.getCreatedAt()
        );
    }

    public static List<ResourceResponse> toResponses(List<Resource> resources) {
        if (resources == null) {
            return List.of();
        }
        return resources.stream().map(ResourceMapper::toResponse).toList();
    }
}
