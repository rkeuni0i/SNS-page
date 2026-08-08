package util;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * 기존 코드는 `if (_ds == null) _ds = lookup(...)` 형태의 지연 초기화를
 * 아무런 동기화 없이 수행했다. 톰캣은 요청마다 스레드를 배정하므로,
 * 서버 기동 직후 여러 요청이 동시에 들어오면
 *   (1) 두 스레드가 동시에 null 검사를 통과해 JNDI lookup을 중복 수행하거나,
 *   (2) 한 스레드가 필드에 쓴 값이 다른 스레드에게 보이지 않는
 *       가시성(visibility) 문제
 * 가 발생할 수 있다.
 *
 * DataSource 자체는 커넥션 풀을 내장한 스레드 세이프 객체이므로 문제를
 * "필드 하나를 한 번만 안전하게 초기화한다"로 좁힐 수 있고,
 * volatile + double-checked locking으로 매 호출 락 비용 없이 해결했다.
 */
public final class ConnectionPool {

    /** volatile이 없으면 DCL은 반쪽짜리 초기화 객체를 노출할 수 있다. */
    private static volatile DataSource dataSource;

    private static final String JNDI_NAME = "java:comp/env/jdbc/mysns";

    private ConnectionPool() {
        throw new AssertionError("유틸리티 클래스는 인스턴스화할 수 없습니다.");
    }

    public static Connection get() throws NamingException, SQLException {
        return getDataSource().getConnection();
    }

    private static DataSource getDataSource() throws NamingException {
        DataSource local = dataSource;   // volatile 읽기를 1회로 줄이는 관용적 최적화
        if (local == null) {
            synchronized (ConnectionPool.class) {
                local = dataSource;
                if (local == null) {
                    local = (DataSource) new InitialContext().lookup(JNDI_NAME);
                    dataSource = local;
                }
            }
        }
        return local;
    }
}
