package service;

import java.io.IOException;
import java.util.List;

import domain.Feed;
import exceptions.ValidationException;
import repository.FeedRepository;
import repository.JdbcFeedRepository;
import util.FileStorageService;

/**
 * 기존 feedAdd.jsp는 파일 업로드 파싱, 이미지 저장, DB 저장, 화면 전환을
 * 한 파일에서 모두 처리했다. 그중 "이미지를 저장하고 그 파일명으로 피드를
 * 만든다"는 도메인 절차를 이 클래스로 옮겨, Facade처럼 publish() 한 번의
 * 호출로 캡슐화했다. 호출부는 저장 순서를 몰라도 된다.
 */
public class FeedService {

    private static final int MAX_CONTENT_LENGTH = 4096;
    private static final int DEFAULT_PAGE_SIZE = 50;

    private final FeedRepository feedRepository;

    public FeedService() {
        this(new JdbcFeedRepository());
    }

    public FeedService(FeedRepository feedRepository) {
        this.feedRepository = feedRepository;
    }

    /**
     * @param authorId        반드시 세션에서 얻은 로그인 사용자 아이디여야 한다.
     *                        요청 파라미터를 그대로 넘기면 타인 사칭이 가능해진다.
     * @param originalFileName 업로드 파일명(확장자 판별에만 사용, 경로에는 쓰지 않는다)
     * @param imageBytes      이미지가 없으면 null 또는 빈 배열
     */
    public void publish(String authorId, String content, String webAppRoot,
                        String originalFileName, byte[] imageBytes) throws IOException {

        if (authorId == null || authorId.isBlank()) {
            throw new ValidationException("로그인이 필요합니다.");
        }
        if (content == null || content.isBlank()) {
            throw new ValidationException("내용을 입력해 주세요.");
        }
        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new ValidationException("내용은 " + MAX_CONTENT_LENGTH + "자를 넘을 수 없습니다.");
        }

        String storedFileName = null;
        if (imageBytes != null && imageBytes.length > 0) {
            storedFileName = FileStorageService.saveImage(webAppRoot, originalFileName, imageBytes);
        }

        feedRepository.save(Feed.builder()
                .authorId(authorId)
                .content(content)
                .imageFileName(storedFileName)
                .build());
    }

    public List<Feed> getRecentFeeds() {
        return feedRepository.findAllOrderByCreatedAtDesc(0, DEFAULT_PAGE_SIZE);
    }

    public List<Feed> getRecentFeeds(int page) {
        int safePage = Math.max(page, 0);
        return feedRepository.findAllOrderByCreatedAtDesc(safePage * DEFAULT_PAGE_SIZE, DEFAULT_PAGE_SIZE);
    }

    public List<Feed> getFeedsOf(String authorId) {
        return feedRepository.findByAuthorId(authorId, 0, DEFAULT_PAGE_SIZE);
    }
}
