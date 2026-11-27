package com.elearning.complaint.config;

import com.elearning.complaint.entity.ComplaintCategory;
import com.elearning.complaint.entity.ComplaintStatus;
import com.elearning.complaint.repository.ComplaintCategoryRepository;
import com.elearning.complaint.repository.ComplaintStatusRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ComplaintCategoryRepository categoryRepository;
    private final ComplaintStatusRepository statusRepository;

    public DataInitializer(ComplaintCategoryRepository categoryRepository,
                           ComplaintStatusRepository statusRepository) {
        this.categoryRepository = categoryRepository;
        this.statusRepository = statusRepository;
    }

    @Override
    public void run(String... args) {
        initCategories();
        initStatuses();
    }

    private void initCategories() {
        List<String> categories = List.of("PAIEMENT", "COURS", "EXAMEN", "TECHNIQUE", "AUTRE");
        for (String libelle : categories) {
            if (!categoryRepository.existsByLibelle(libelle)) {
                categoryRepository.save(ComplaintCategory.builder().libelle(libelle).build());
            }
        }
    }

    private void initStatuses() {
        List<String> statuses = List.of("OPEN", "IN_PROGRESS", "ACCEPTED", "REJECTED");
        for (String libelle : statuses) {
            if (!statusRepository.existsByLibelle(libelle)) {
                statusRepository.save(ComplaintStatus.builder().libelle(libelle).build());
            }
        }
    }
}
