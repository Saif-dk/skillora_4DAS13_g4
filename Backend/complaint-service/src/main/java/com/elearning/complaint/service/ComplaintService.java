package com.elearning.complaint.service;

import com.elearning.complaint.dto.*;
import com.elearning.complaint.entity.*;
import com.elearning.complaint.exception.*;
import com.elearning.complaint.repository.*;
import com.elearning.complaint.security.AuthUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ComplaintService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private final ComplaintRepository complaintRepository;
    private final ComplaintCategoryRepository categoryRepository;
    private final ComplaintStatusRepository statusRepository;

    public ComplaintService(ComplaintRepository complaintRepository,
                            ComplaintCategoryRepository categoryRepository,
                            ComplaintStatusRepository statusRepository) {
        this.complaintRepository = complaintRepository;
        this.categoryRepository = categoryRepository;
        this.statusRepository = statusRepository;
    }

    // ─── Reference Data ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(c -> new CategoryResponse(c.getId(), c.getLibelle()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StatusResponse> getAllStatuses() {
        return statusRepository.findAll().stream()
                .map(s -> new StatusResponse(s.getId(), s.getLibelle()))
                .collect(Collectors.toList());
    }

    // ─── Create Complaint ────────────────────────────────────────────────────────

    public ComplaintResponse createComplaint(CreateComplaintRequest request, AuthUser authUser) {
        if ("ADMIN".equals(authUser.role())) {
            throw new ForbiddenOperationException("Les administrateurs ne peuvent pas soumettre de réclamations.");
        }

        ComplaintCategory category = categoryRepository.findById(request.categoryId().toString())
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable : " + request.categoryId()));

        ComplaintStatus openStatus = statusRepository.findByLibelle("OPEN")
                .orElseThrow(() -> new ResourceNotFoundException("Statut OPEN introuvable. Veuillez initialiser les données."));

        Complaint complaint = Complaint.builder()
                .reference(generateReference())
                .requesterId(authUser.userId())
                .subject(request.subject())
                .description(request.description())
                .category(category)
                .status(openStatus)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Complaint saved = complaintRepository.save(complaint);
        return toComplaintResponse(saved);
    }

    // ─── List Complaints ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ComplaintResponse> getComplaints(AuthUser authUser, String statusFilter) {
        List<Complaint> complaints;

        if (statusFilter != null) {
            ComplaintStatus status = statusRepository.findByLibelle(statusFilter.toUpperCase())
                    .orElseThrow(() -> new BadRequestException("Statut de filtre inconnu : " + statusFilter));

            if ("ADMIN".equals(authUser.role())) {
                complaints = complaintRepository.findByStatus(status);
            } else {
                complaints = complaintRepository.findByRequesterIdAndStatus(authUser.userId(), status);
            }
        } else {
            if ("ADMIN".equals(authUser.role())) {
                complaints = complaintRepository.findAll();
            } else {
                complaints = complaintRepository.findByRequesterId(authUser.userId());
            }
        }

        return complaints.stream()
                .map(this::toComplaintResponse)
                .collect(Collectors.toList());
    }

    // ─── Get Single Complaint ────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ComplaintResponse getComplaintById(String id, AuthUser authUser) {
        Complaint complaint = findComplaintById(id);
        checkOwnerOrAdmin(complaint, authUser);
        return toComplaintResponse(complaint);
    }

    // ─── Add Reply ────────────────────────────────────────────────────────────────

    public ReplyResponse addReply(String complaintId, ReplyRequest request, AuthUser authUser) {
        Complaint complaint = findComplaintById(complaintId);

        boolean isOwner = complaint.getRequesterId().equals(authUser.userId());
        boolean isAdmin = "ADMIN".equals(authUser.role());

        if (!isOwner && !isAdmin) {
            throw new ForbiddenOperationException("Vous n'êtes pas autorisé à répondre à cette réclamation.");
        }

        String currentStatus = complaint.getStatus().getLibelle();
        if ("ACCEPTED".equals(currentStatus) || "REJECTED".equals(currentStatus)) {
            throw new ConflictException("Impossible de répondre : la réclamation est déjà " + currentStatus + ".");
        }

        // If admin replies and status is OPEN → switch to IN_PROGRESS
        if (isAdmin && "OPEN".equals(currentStatus)) {
            ComplaintStatus inProgress = statusRepository.findByLibelle("IN_PROGRESS")
                    .orElseThrow(() -> new ResourceNotFoundException("Statut IN_PROGRESS introuvable."));
            complaint.setStatus(inProgress);
            complaint.setAssignedAdminId(authUser.userId());
            complaint.setUpdatedAt(Instant.now());
        }

        ComplaintReply reply = ComplaintReply.builder()
                .complaint(complaint)
                .senderId(authUser.userId())
                .senderRole(authUser.role())
                .content(request.content())
                .createdAt(Instant.now())
                .build();

        complaint.getReplies().add(reply);
        complaintRepository.save(complaint);

        return toReplyResponse(reply);
    }

    // ─── Admin: Update Status ─────────────────────────────────────────────────────

    public ComplaintResponse updateStatus(String complaintId, UpdateStatusRequest request, AuthUser authUser) {
        Complaint complaint = findComplaintById(complaintId);

        String newStatusLabel = request.status().toUpperCase();
        if ("OPEN".equals(newStatusLabel) || (!List.of("IN_PROGRESS", "ACCEPTED", "REJECTED").contains(newStatusLabel))) {
            throw new BadRequestException("Statut invalide : '" + request.status() + "'. Valeurs acceptées : IN_PROGRESS, ACCEPTED, REJECTED.");
        }

        String currentStatus = complaint.getStatus().getLibelle();
        if ("ACCEPTED".equals(currentStatus) || "REJECTED".equals(currentStatus)) {
            throw new ConflictException("La réclamation est déjà clôturée avec le statut " + currentStatus + ".");
        }

        ComplaintStatus newStatus = statusRepository.findByLibelle(newStatusLabel)
                .orElseThrow(() -> new ResourceNotFoundException("Statut introuvable : " + newStatusLabel));

        complaint.setStatus(newStatus);
        complaint.setAssignedAdminId(authUser.userId());
        complaint.setUpdatedAt(Instant.now());

        return toComplaintResponse(complaintRepository.save(complaint));
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────────

    private Complaint findComplaintById(String id) {
        return complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Réclamation introuvable : " + id));
    }

    private void checkOwnerOrAdmin(Complaint complaint, AuthUser authUser) {
        boolean isOwner = complaint.getRequesterId().equals(authUser.userId());
        boolean isAdmin = "ADMIN".equals(authUser.role());
        if (!isOwner && !isAdmin) {
            throw new ForbiddenOperationException("Vous n'êtes pas autorisé à accéder à cette réclamation.");
        }
    }

    private String generateReference() {
        StringBuilder sb = new StringBuilder("REC-");
        for (int i = 0; i < 8; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    private ComplaintResponse toComplaintResponse(Complaint c) {
        List<ReplyResponse> replies = c.getReplies().stream()
                .map(this::toReplyResponse)
                .collect(Collectors.toList());

        return new ComplaintResponse(
                c.getId(),
                c.getReference(),
                c.getRequesterId(),
                c.getSubject(),
                c.getDescription(),
                c.getCategory() != null ? c.getCategory().getLibelle() : null,
                c.getStatus() != null ? c.getStatus().getLibelle() : null,
                c.getAssignedAdminId(),
                c.getCreatedAt(),
                c.getUpdatedAt(),
                replies
        );
    }

    private ReplyResponse toReplyResponse(ComplaintReply r) {
        return new ReplyResponse(
                r.getId(),
                r.getSenderId(),
                r.getSenderRole(),
                r.getContent(),
                r.getCreatedAt()
        );
    }
}
