package com.sliit.vrs.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

// Shared utility service: handles saving uploaded vehicle photos and
// profile pictures to disk (outside the packaged jar, so uploads survive
// application restarts and redeploys) and returns the public web path
// that should be stored on the entity (e.g. Vehicle.imageUrl).
@Service
public class FileStorageService {

    // Folder on disk where files are actually stored, e.g. "uploads/vehicles"
    @Value("${app.upload.dir:uploads}")
    private String uploadRootDir;

    /**
     * Saves the file under uploadRootDir/subFolder/ with a random file name
     * (keeps the original extension) and returns the URL path the browser
     * should use to fetch it, e.g. "/uploads/vehicles/3f2a-photo.jpg".
     * See WebConfig for how "/uploads/**" is mapped back to this folder.
     */
    public String store(MultipartFile file, String subFolder) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            Path targetDir = Paths.get(uploadRootDir, subFolder);
            Files.createDirectories(targetDir);

            String original = file.getOriginalFilename();
            String extension = "";
            if (original != null && original.contains(".")) {
                extension = original.substring(original.lastIndexOf('.'));
            }
            String newFileName = UUID.randomUUID() + extension;

            Path targetPath = targetDir.resolve(newFileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            return "/uploads/" + subFolder + "/" + newFileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store uploaded file: " + e.getMessage(), e);
        }
    }
}
