package repository;

import java.util.List;
import java.util.Optional;

import domain.User;

/**
 * DIP(의존관계 역전 원칙) 적용.
 * 서비스 계층은 이 인터페이스에만 의존하고 구체적인 JDBC 구현은 알지 못한다.
 * 덕분에 테스트에서는 인메모리 Fake 구현체를 주입할 수 있고,
 * 훗날 JPA/MyBatis로 갈아끼워도 서비스 코드는 그대로 둘 수 있다.
 */
public interface UserRepository {

    void save(User user);

    boolean existsById(String id);

    Optional<User> findById(String id);

    /** @return 실제로 삭제된 행이 있으면 true */
    boolean deleteById(String id);

    List<User> findAllOrderByCreatedAtDesc();
}
