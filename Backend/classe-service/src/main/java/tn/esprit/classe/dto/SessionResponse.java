package tn.esprit.classe.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionResponse {

    private Long id;
    private Long classroomId;
    private String title;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String meetingLink;
    private String status;
}
