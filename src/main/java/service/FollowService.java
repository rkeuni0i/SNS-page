package service;

import java.util.ArrayList;
import java.util.List;

import domain.User;
import exceptions.UserNotFoundException;
import exceptions.ValidationException;
import repository.FollowRepository;
import repository.JdbcFollowRepository;
import repository.UserRepository;

/**
 * 팔로우 도메인 규칙을 담당한다. docs/ERD.md '확장 시 설계' 절에서 설계한
 * follow(follower_id, followee_id) 테이블 위에 얹은 서비스 계층이다.
 *
 * DB 제약(복합 PK, 자기 자신 팔로우 금지 CHECK)이 최종 방어선이지만,
 * 여기서도 먼저 검사해 사용자에게 이해 가능한 메시지를 준다 — 회원가입의
 * existsById 선검사와 같은 이유다(UserService 주석 참고).
 */
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    public FollowService() {
        this(new JdbcFollowRepository(), new repository.JdbcUserRepository());
    }

    public FollowService(FollowRepository followRepository, UserRepository userRepository) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
    }

    public void follow(String followerId, String followeeId) {
        requireLoggedIn(followerId);
        if (followerId.equals(followeeId)) {
            throw new ValidationException("자기 자신은 팔로우할 수 없습니다.");
        }
        if (!userRepository.existsById(followeeId)) {
            throw new UserNotFoundException(followeeId);
        }
        followRepository.follow(followerId, followeeId);
    }

    public void unfollow(String followerId, String followeeId) {
        requireLoggedIn(followerId);
        followRepository.unfollow(followerId, followeeId);
    }

    public boolean isFollowing(String followerId, String followeeId) {
        if (followerId == null) return false;
        return followRepository.isFollowing(followerId, followeeId);
    }

    public int getFollowerCount(String userId) {
        return followRepository.countFollowers(userId);
    }

    public int getFollowingCount(String userId) {
        return followRepository.countFollowing(userId);
    }

    /**
     * userId를 팔로우하는 회원 목록.
     * 규모가 작은 프로젝트라 N+1 조회(아이디 목록 조회 후 각 아이디를 findById)를
     * 그대로 두었다. 팔로워가 많아지면 IN 절 일괄 조회로 바꾸는 것이 다음 단계다.
     */
    public List<User> getFollowers(String userId) {
        return resolveUsers(followRepository.findFollowerIds(userId));
    }

    public List<User> getFollowing(String userId) {
        return resolveUsers(followRepository.findFolloweeIds(userId));
    }

    private List<User> resolveUsers(List<String> ids) {
        List<User> users = new ArrayList<>();
        for (String id : ids) {
            userRepository.findById(id).ifPresent(users::add);
        }
        return users;
    }

    private void requireLoggedIn(String followerId) {
        if (followerId == null || followerId.isBlank()) {
            throw new ValidationException("로그인이 필요합니다.");
        }
    }
}
