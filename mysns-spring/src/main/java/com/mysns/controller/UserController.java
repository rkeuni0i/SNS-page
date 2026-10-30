package com.mysns.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mysns.domain.User;
import com.mysns.dto.ProfileResponse;
import com.mysns.dto.SignUpRequest;
import com.mysns.dto.UserResponse;
import com.mysns.dto.WithdrawRequest;
import com.mysns.service.FollowService;
import com.mysns.service.UserService;

import jakarta.validation.Valid;

/** 인증은 SecurityConfig가 URL 단위로 처리하므로, 여기서는 로그인한 사용자 아이디(@AuthenticationPrincipal)만 받는다. */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final FollowService followService;

    public UserController(UserService userService, FollowService followService) {
        this.userService = userService;
        this.followService = followService;
    }

    @PostMapping
    public ResponseEntity<Void> signUp(@RequestBody @Valid SignUpRequest req) {
        userService.signUp(req);
        return ResponseEntity.status(201).build();
    }

    @GetMapping
    public List<UserResponse> list() {
        return userService.getAllUsers().stream().map(UserResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ProfileResponse profile(@PathVariable String id, @AuthenticationPrincipal String myId) {
        User user = userService.getById(id);
        boolean following = !myId.equals(id) && followService.isFollowing(myId, id);
        return ProfileResponse.of(user, followService.getFollowerCount(id), followService.getFollowingCount(id),
                following);
    }

    @GetMapping("/{id}/followers")
    public List<UserResponse> followers(@PathVariable String id) {
        userService.getById(id);
        return followService.getFollowers(id).stream().map(UserResponse::from).toList();
    }

    @GetMapping("/{id}/following")
    public List<UserResponse> following(@PathVariable String id) {
        userService.getById(id);
        return followService.getFollowing(id).stream().map(UserResponse::from).toList();
    }

    /** 회원 탈퇴. 본인만, 비밀번호를 재확인한 뒤에만 가능하다. refresh 토큰은 FK CASCADE로 함께 폐기된다. */
    @DeleteMapping("/me")
    public ResponseEntity<Void> withdraw(@RequestBody @Valid WithdrawRequest req, @AuthenticationPrincipal String myId) {
        userService.withdraw(myId, req.password());
        return ResponseEntity.noContent().build();
    }
}
