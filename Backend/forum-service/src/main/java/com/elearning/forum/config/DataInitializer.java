package com.elearning.forum.config;

import com.elearning.forum.model.ForumCategory;
import com.elearning.forum.repository.ForumCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds the database with default forum categories at startup if they don't exist.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ForumCategoryRepository categoryRepository;

    private static final List<String[]> DEFAULT_CATEGORIES = List.of(
            new String[]{"Général",          "Discussions générales sur la plateforme e-learning"},
            new String[]{"Cours",            "Questions et ressources relatives aux cours"},
            new String[]{"Examens",          "Préparation aux examens et corrections"},
            new String[]{"Projets",          "Collaboration et aide sur les projets"},
            new String[]{"Aide technique",   "Problèmes techniques et support informatique"},
            new String[]{"Annonces",         "Annonces officielles de l'administration"}
    );

    @Override
    public void run(String... args) {
        int created = 0;
        for (String[] entry : DEFAULT_CATEGORIES) {
            String name = entry[0];
            String desc = entry[1];
            if (!categoryRepository.existsByName(name)) {
                categoryRepository.save(
                        ForumCategory.builder()
                                .name(name)
                                .description(desc)
                                .build()
                );
                log.info("Created default category: '{}'", name);
                created++;
            }
        }
        if (created == 0) {
            log.info("All default forum categories already exist — skipping seed.");
        } else {
            log.info("Seeded {} forum categories.", created);
        }
    }
}
