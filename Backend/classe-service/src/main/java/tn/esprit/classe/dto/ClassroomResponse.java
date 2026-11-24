package tn.esprit.classe.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassroomResponse {

    private Long id;
    private String name;
    private String description;
    private Long courseId;
    private Long instructorId;
    private Integer capacity;
    private String status;
    private List<SessionResponse> sessions;
}
