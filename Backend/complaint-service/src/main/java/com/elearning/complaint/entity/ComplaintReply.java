package com.elearning.complaint.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "complaint_replies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintReply {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false)
    private Complaint complaint;

    @Column(nullable = false)
    private String senderId;

    @Column(nullable = false)
    private String senderRole;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    private Instant createdAt;
}
