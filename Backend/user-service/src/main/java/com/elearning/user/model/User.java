package com.elearning.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    private String roleId;

    private String firstName;

    private String lastName;

    @Indexed(unique = true)
    private String email;

    private String passwordHash;

    @Builder.Default
    private String status = "ACTIVE";

    @CreatedDate
    private Instant createdAt;
}
