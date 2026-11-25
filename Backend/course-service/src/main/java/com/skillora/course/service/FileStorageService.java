package com.skillora.course.service;

import com.skillora.course.api.exception.BusinessConflictException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv", "zip",
            "mp4", "webm", "mov", "avi", "mkv", "png", "jpg", "jpeg", "gif");

    private final Path uploadDir;

    public FileStorageService(@Value("${skillora.uploads.dir:./uploads}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public StoredFile store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessConflictException("Aucun fichier recu");
        }

        String originalName = Paths.get(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename())
                .getFileName().toString();
        String extension = extension(originalName);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessConflictException("Type de fichier non pris en charge");
        }

        String safeBase = originalName.substring(0, originalName.length() - extension.length() - 1)
                .replaceAll("[^a-zA-Z0-9-_]", "_");
        if (safeBase.isBlank()) {
            safeBase = "file";
        }
        String storedName = UUID.randomUUID() + "-" + safeBase + "." + extension;

        try {
            Files.createDirectories(uploadDir);
            Path target = uploadDir.resolve(storedName);
            try (var input = file.getInputStream()) {
                Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return new StoredFile(storedName, originalName, formatSize(file.getSize()));
        } catch (IOException exception) {
            throw new IllegalStateException("Impossible d'enregistrer le fichier", exception);
        }
    }

    public record StoredFile(String storedName, String originalName, String sizeLabel) {
    }

    private String extension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        if (bytes < 1024L * 1024) {
            return String.format(Locale.ROOT, "%.1f Ko", bytes / 1024.0);
        }
        return String.format(Locale.ROOT, "%.1f Mo", bytes / (1024.0 * 1024.0));
    }
}
