package tn.esprit.examen.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "questions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Question {
    @Id
    private String id;
    private String enonce;
    private String typeQuestion;   // QCM, VRAI_FAUX...
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String bonneReponse;   // A, B, C ou D
    private double points;
    @Indexed
    private String examenId;
}