package tn.esprit.examen.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "examens")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Examen {
    @Id
    private String id;          // ObjectId Mongo (String). Voir remarque en bas sur Long.
    private String titre;
    private String description;
    private LocalDateTime dateExamen;
    private int duree;          // en minutes
    private double noteTotale;  // somme des points des questions
    private StatutExamen statut;
    private String courseId;
    private String teacherId;
}