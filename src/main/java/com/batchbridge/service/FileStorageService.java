package com.batchbridge.service;

import com.batchbridge.exception.FileStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private static final List<String> ALLOWED_EXTENSIONS =
            Arrays.asList("pdf", "doc", "docx", "ppt", "pptx", "txt", "zip");

    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024;

    public String storeFile(MultipartFile file) {
        validateFile(file);

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = getExtension(originalFileName);
        String uniqueFileName = UUID.randomUUID().toString() + "." + extension;

        try {
            if (originalFileName.contains("..")) {
                throw new FileStorageException("Invalid file path: " + originalFileName);
            }

            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            Path targetLocation = uploadPath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation);

            return uniqueFileName;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + originalFileName, ex);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(fileName);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new FileStorageException("File not found: " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new FileStorageException("File not found: " + fileName, ex);
        }
    }

    public void deleteFile(String fileName) {
        try {
            Path filePath = Paths.get(uploadDir).toAbsolutePath().normalize().resolve(fileName);
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new FileStorageException("Could not delete file " + fileName, ex);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileStorageException("Cannot upload empty file");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileStorageException("File size exceeds maximum limit of 20MB");
        }
        String extension = getExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new FileStorageException("File type not allowed. Allowed types: " + ALLOWED_EXTENSIONS);
        }
    }

    private String getExtension(String fileName) {
        int lastIndex = fileName.lastIndexOf('.');
        if (lastIndex == -1) return "";
        return fileName.substring(lastIndex + 1);
    }
}
