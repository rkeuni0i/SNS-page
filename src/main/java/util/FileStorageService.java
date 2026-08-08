package util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * 기존 FileUtil.saveImage는 업로드된 원본 파일명을 그대로 서버 경로에 사용했다.
 * 사용자가 파일명을 "../../WEB-INF/web.xml" 처럼 조작하면 웹 루트 바깥의
 * 파일을 덮어쓸 수 있는 경로 조작(Path Traversal) 취약점이 된다.
 * 또 f.mkdir()은 중간 디렉터리가 없으면 실패하는데 반환값을 확인하지 않았고,
 * FileOutputStream을 try-with-resources 없이 닫아 예외 발생 시
 * 파일 디스크립터가 누수될 수 있었다.
 *
 * 개선 사항
 *  1) 저장 파일명은 서버가 UUID로 새로 생성한다(원본 파일명은 경로 계산에 쓰지 않는다).
 *  2) 확장자는 화이트리스트로만 허용한다.
 *  3) 정규화(normalize) 후 최종 경로가 images 디렉터리 하위인지 재검증한다.
 *  4) createDirectories로 중간 경로까지 생성하고, try-with-resources로 스트림을 닫는다.
 *  5) 업로드 용량 상한을 두어 디스크 고갈을 방지한다.
 */
public final class FileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("jpg", "jpeg", "png", "gif", "webp");

    private static final long MAX_BYTES = 5L * 1024 * 1024; // 5MB

    private FileStorageService() {
        throw new AssertionError("유틸리티 클래스는 인스턴스화할 수 없습니다.");
    }

    /**
     * @return DB에 기록할 저장 파일명(UUID 기반)
     */
    public static String saveImage(String webAppRoot, String originalFileName, byte[] data)
            throws IOException {

        if (data == null || data.length == 0) {
            throw new IOException("빈 이미지 파일입니다.");
        }
        if (data.length > MAX_BYTES) {
            throw new IOException("이미지 용량이 5MB를 초과했습니다.");
        }

        String extension = extractExtension(originalFileName)
                .filter(ALLOWED_EXTENSIONS::contains)
                .orElseThrow(() -> new IOException(
                        "허용되지 않는 이미지 형식입니다(jpg, jpeg, png, gif, webp만 가능)."));

        Path imagesDir = Paths.get(webAppRoot, "images").normalize().toAbsolutePath();
        Files.createDirectories(imagesDir);

        String storedFileName = UUID.randomUUID() + "." + extension;
        Path target = imagesDir.resolve(storedFileName).normalize().toAbsolutePath();

        // 방어적 재검증: 정규화 후에도 images 디렉터리를 벗어나면 조작 시도로 간주한다.
        if (!target.startsWith(imagesDir)) {
            throw new IOException("잘못된 파일 경로입니다.");
        }

        try (OutputStream out = Files.newOutputStream(target,
                StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            out.write(data);
        }
        return storedFileName;
    }

    private static Optional<String> extractExtension(String fileName) {
        if (fileName == null || fileName.isBlank()) return Optional.empty();

        // 윈도우 브라우저는 "C:\path\to\photo.jpg" 처럼 전체 경로를 보내기도 한다.
        String base = fileName.replace('\\', '/');
        base = base.substring(base.lastIndexOf('/') + 1);

        int dot = base.lastIndexOf('.');
        if (dot < 0 || dot == base.length() - 1) return Optional.empty();
        return Optional.of(base.substring(dot + 1).toLowerCase(Locale.ROOT));
    }
}
