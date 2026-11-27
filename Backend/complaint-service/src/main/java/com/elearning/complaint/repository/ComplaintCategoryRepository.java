package com.elearning.complaint.repository;

import com.elearning.complaint.entity.ComplaintCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ComplaintCategoryRepository extends JpaRepository<ComplaintCategory, String> {
    Optional<ComplaintCategory> findByLibelle(String libelle);
    boolean existsByLibelle(String libelle);
}
