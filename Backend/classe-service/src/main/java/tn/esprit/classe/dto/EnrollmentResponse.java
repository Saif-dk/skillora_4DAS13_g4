package tn.esprit.classe.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentResponse {

    private Long id;
    private Long classroomId;
    private Long studentId;
    private LocalDateTime enrolledAt;
    private String status;
}
