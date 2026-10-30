package com.mysns.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mysns.dto.FeedResponse;
import com.mysns.service.FeedService;

@RestController
@RequestMapping("/api/feeds")
public class FeedController {

    private final FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

    /** 최신순 피드. authorId를 주면 해당 회원의 피드만 조회한다. */
    @GetMapping
    public List<FeedResponse> list(@RequestParam(defaultValue = "0") int page,
                                   @RequestParam(required = false) String authorId) {
        var feeds = (authorId == null || authorId.isBlank())
                ? feedService.getFeeds(page)
                : feedService.getFeedsOf(authorId, page);
        return feeds.stream().map(FeedResponse::from).toList();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> create(@RequestParam String content,
                                       @RequestParam(required = false) MultipartFile image,
                                       @AuthenticationPrincipal String myId) {
        // 작성자는 요청 값이 아니라 토큰에서 확정한다. 요청 값을 쓰면 타인 사칭이 가능해진다.
        feedService.publish(myId, content, image);
        return ResponseEntity.status(201).build();
    }
}
