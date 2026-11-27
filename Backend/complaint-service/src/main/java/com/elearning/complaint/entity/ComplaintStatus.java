package com.elearning.complaint.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "complaint_statuses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false)
    private String libelle;
}
