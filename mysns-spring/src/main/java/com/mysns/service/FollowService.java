package com.mysns.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mysns.domain.User;
import com.mysns.exception.UserNotFoundException;
import com.mysns.exception.ValidationException;
import com.mysns.repository.FollowRepository;
import com.mysns.repository.UserRepository;

@Service
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    public FollowService(FollowRepository followRepository, UserRepository userRepository) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
    }

    public void follow(String followerId, String followeeId) {
        if (followerId.equals(followeeId)) {
            throw new ValidationException("자기 자신은 팔로우할 수 없습니다.");
        }
        if (!userRepository.existsById(followeeId)) {
            throw new UserNotFoundException(followeeId);
        }
        followRepository.follow(followerId, followeeId);
    }

    public void unfollow(String followerId, String followeeId) {
        followRepository.unfollow(followerId, followeeId);
    }

    public boolean isFollowing(String followerId, String followeeId) {
        return followRepository.isFollowing(followerId, followeeId);
    }

    public int getFollowerCount(String userId) {
        return followRepository.countFollowers(userId);
    }

    public int getFollowingCount(String userId) {
        return followRepository.countFollowing(userId);
    }

    public List<User> getFollowers(String userId) {
        return followRepository.findFollowers(userId);
    }

    public List<User> getFollowing(String userId) {
        return followRepository.findFollowing(userId);
    }
}
