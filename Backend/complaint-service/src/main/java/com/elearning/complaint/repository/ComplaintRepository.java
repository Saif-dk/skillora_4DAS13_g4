package com.elearning.complaint.repository;

import com.elearning.complaint.entity.Complaint;
import com.elearning.complaint.entity.ComplaintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, String> {
    List<Complaint> findByRequesterId(String requesterId);
    List<Complaint> findByStatus(ComplaintStatus status);
    List<Complaint> findByRequesterIdAndStatus(String requesterId, ComplaintStatus status);
}
