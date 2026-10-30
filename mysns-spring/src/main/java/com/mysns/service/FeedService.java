package com.mysns.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mysns.domain.Feed;
import com.mysns.exception.ValidationException;
import com.mysns.repository.FeedRepository;

@Service
public class FeedService {

    private static final int MAX_CONTENT_LENGTH = 4096;
    static final int PAGE_SIZE = 50;

    private final FeedRepository feedRepository;
    private final FileStorageService fileStorageService;

    public FeedService(FeedRepository feedRepository, FileStorageService fileStorageService) {
        this.feedRepository = feedRepository;
        this.fileStorageService = fileStorageService;
    }

    /** @param authorId 반드시 세션에서 얻은 로그인 사용자 아이디여야 한다(요청 값을 쓰면 사칭이 가능). */
    public void publish(String authorId, String content, MultipartFile image) {
        if (content == null || content.isBlank()) {
            throw new ValidationException("내용을 입력해 주세요.");
        }
        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new ValidationException("내용은 " + MAX_CONTENT_LENGTH + "자를 넘을 수 없습니다.");
        }

        String storedFileName = null;
        if (image != null && !image.isEmpty()) {
            storedFileName = fileStorageService.saveImage(image);
        }
        feedRepository.save(authorId, content, storedFileName);
    }

    public List<Feed> getFeeds(int page) {
        return feedRepository.findAllOrderByCreatedAtDesc(offset(page), PAGE_SIZE);
    }

    public List<Feed> getFeedsOf(String authorId, int page) {
        return feedRepository.findByAuthorId(authorId, offset(page), PAGE_SIZE);
    }

    private int offset(int page) {
        return Math.max(page, 0) * PAGE_SIZE;
    }
}
