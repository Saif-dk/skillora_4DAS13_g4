package com.elearning.complaint.repository;

import com.elearning.complaint.entity.ComplaintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ComplaintStatusRepository extends JpaRepository<ComplaintStatus, String> {
    Optional<ComplaintStatus> findByLibelle(String libelle);
    boolean existsByLibelle(String libelle);
}
