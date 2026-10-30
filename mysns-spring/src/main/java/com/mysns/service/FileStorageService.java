package com.mysns.service;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mysns.exception.ValidationException;

/**
 * 업로드 파일명은 경로 계산에 쓰지 않는다(Path Traversal 방지).
 * 저장 파일명은 서버가 UUID로 생성하고, 확장자는 화이트리스트로만 허용한다.
 */
@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final long MAX_BYTES = 5L * 1024 * 1024;

    private final Path imagesDir;

    public FileStorageService(@Value("${mysns.upload-dir}") String uploadDir) {
        this.imagesDir = Paths.get(uploadDir).normalize().toAbsolutePath();
    }

    public Path getImagesDir() {
        return imagesDir;
    }

    /** @return DB에 기록할 저장 파일명(UUID 기반) */
    public String saveImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ValidationException("빈 이미지 파일입니다.");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new ValidationException("이미지 용량이 5MB를 초과했습니다.");
        }

        String extension = extractExtension(file.getOriginalFilename());
        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ValidationException("허용되지 않는 이미지 형식입니다(jpg, jpeg, png, gif, webp만 가능).");
        }

        try {
            Files.createDirectories(imagesDir);
            String storedFileName = UUID.randomUUID() + "." + extension;
            Path target = imagesDir.resolve(storedFileName).normalize().toAbsolutePath();
            if (!target.startsWith(imagesDir)) {
                throw new ValidationException("잘못된 파일 경로입니다.");
            }
            try (OutputStream out = Files.newOutputStream(target,
                    StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
                file.getInputStream().transferTo(out);
            }
            return storedFileName;
        } catch (IOException e) {
            throw new UncheckedIOException("이미지 저장에 실패했습니다.", e);
        }
    }

    private static String extractExtension(String fileName) {
        if (fileName == null || fileName.isBlank()) return null;

        // 윈도우 브라우저는 "C:\path\to\photo.jpg" 처럼 전체 경로를 보내기도 한다.
        String base = fileName.replace('\\', '/');
        base = base.substring(base.lastIndexOf('/') + 1);

        int dot = base.lastIndexOf('.');
        if (dot < 0 || dot == base.length() - 1) return null;
        return base.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
