package ${packageName}.file;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ${entityName}FileHandler {

    @Value("${r"${"}file.upload.path:./uploads}")
    private String uploadPath;

    @Value("${r"${"}file.max.size:10485760}") // 10MB default
    private long maxFileSize;

    public FileUploadResult uploadFile(MultipartFile file, String category) throws IOException {
        log.info("Uploading file: {} for category: {}", file.getOriginalFilename(), category);
        
        // Validate file
        validateFile(file);
        
        // Generate unique filename
        String fileName = generateUniqueFileName(file.getOriginalFilename());
        
        // Create category directory if not exists
        Path categoryPath = Paths.get(uploadPath, category);
        Files.createDirectories(categoryPath);
        
        // Save file
        Path filePath = categoryPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        log.info("File uploaded successfully: {}", filePath);
        
        return FileUploadResult.builder()
                .originalFileName(file.getOriginalFilename())
                .savedFileName(fileName)
                .filePath(filePath.toString())
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .build();
    }

    public byte[] downloadFile(String filePath) throws IOException {
        log.info("Downloading file: {}", filePath);
        
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IOException("File not found: " + filePath);
        }
        
        return Files.readAllBytes(path);
    }

    public void deleteFile(String filePath) throws IOException {
        log.info("Deleting file: {}", filePath);
        
        Path path = Paths.get(filePath);
        Files.deleteIfExists(path);
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size: " + maxFileSize);
        }
        
        // Add more validations as needed (file type, etc.)
    }

    private String generateUniqueFileName(String originalFileName) {
        String extension = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFileName.substring(dotIndex);
        }
        
        return UUID.randomUUID().toString() + extension;
    }

    @lombok.Data
    @lombok.Builder
    public static class FileUploadResult {
        private String originalFileName;
        private String savedFileName;
        private String filePath;
        private long fileSize;
        private String contentType;
    }
}
