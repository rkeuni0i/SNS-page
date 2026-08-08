package repository;

import java.util.List;

import domain.Feed;

public interface FeedRepository {

    void save(Feed feed);

    /** 최신순 피드 목록(페이지네이션). */
    List<Feed> findAllOrderByCreatedAtDesc(int offset, int limit);

    /** 특정 회원의 피드만 최신순으로 조회. (id, ts) 복합 인덱스를 사용한다. */
    List<Feed> findByAuthorId(String authorId, int offset, int limit);
}
