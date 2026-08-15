package repository;

import java.util.List;

/**
 * follow(follower_id, followee_id) 복합 PK 테이블에 대한 접근을 담당한다.
 * docs/ERD.md '확장 시 설계' 절의 설계를 그대로 구현한다.
 */
public interface FollowRepository {

    /** @return 이번 호출로 실제 관계가 새로 생겼으면 true, 이미 팔로우 중이었으면 false */
    boolean follow(String followerId, String followeeId);

    /** @return 이번 호출로 실제 관계가 삭제됐으면 true, 원래 팔로우 중이 아니었으면 false */
    boolean unfollow(String followerId, String followeeId);

    boolean isFollowing(String followerId, String followeeId);

    int countFollowers(String userId);

    int countFollowing(String userId);

    /** userId 를 팔로우하는 사람들의 아이디 목록(최신순). */
    List<String> findFollowerIds(String userId);

    /** userId 가 팔로우하는 사람들의 아이디 목록(최신순). */
    List<String> findFolloweeIds(String userId);
}
