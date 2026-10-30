package com.mysns.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mysns.service.FollowService;

@RestController
@RequestMapping("/api/follows")
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    /** 이미 팔로우 중이어도 오류 없이 204를 돌려준다(멱등). */
    @PostMapping("/{userId}")
    public ResponseEntity<Void> follow(@PathVariable String userId, @AuthenticationPrincipal String myId) {
        followService.follow(myId, userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> unfollow(@PathVariable String userId, @AuthenticationPrincipal String myId) {
        followService.unfollow(myId, userId);
        return ResponseEntity.noContent().build();
    }
}
