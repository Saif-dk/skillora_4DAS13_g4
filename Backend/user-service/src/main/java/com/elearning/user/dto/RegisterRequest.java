package com.elearning.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank
    @Schema(example = "Ali")
    private String firstName;

    @NotBlank
    @Schema(example = "Ben Salah")
    private String lastName;

    @NotBlank
    @Email
    @Schema(example = "ali@esprit.tn")
    private String email;

    @NotBlank
    @Size(min = 8)
    @Schema(example = "Password123")
    private String password;

    @Schema(example = "STUDENT")
    private String role;
}
