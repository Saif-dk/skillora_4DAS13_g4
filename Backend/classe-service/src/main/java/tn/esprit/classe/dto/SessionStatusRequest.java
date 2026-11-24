package tn.esprit.classe.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.esprit.classe.model.SessionStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionStatusRequest {

    @NotNull(message = "Le statut est obligatoire")
    private SessionStatus status;
}
