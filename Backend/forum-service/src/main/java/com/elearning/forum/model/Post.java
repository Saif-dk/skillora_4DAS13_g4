package com.elearning.forum.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "posts")
@CompoundIndexes({
        @CompoundIndex(name = "category_created_idx", def = "{'categoryId': 1, 'createdAt': -1}")
})
public class Post {

    @Id
    private String id;

    private String title;

    private String content;

    @Indexed
    private String categoryId;

    private String authorId;

    private String authorEmail;

    private String authorRole;

    @Builder.Default
    private boolean pinned = false;

    @Builder.Default
    private boolean locked = false;

    @Builder.Default
    private Set<String> likedBy = new HashSet<>();

    @Builder.Default
    private int commentCount = 0;

    @Indexed
    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
