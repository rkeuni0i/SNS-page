import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { api, setAuthLostHandler } from './api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // 토큰 재발급까지 실패하면(refresh 만료·폐기) 화면을 로그아웃 상태로 돌린다.
    setAuthLostHandler(() => setUser(null));

    // access 토큰은 메모리에만 있어 새로고침하면 사라진다. refresh 쿠키로 로그인 상태를 복원한다.
    api.restore()
      .then(setUser)
      .catch(() => setUser(null))
      .finally(() => setLoading(false));
  }, []);

  const login = useCallback(async (id, password) => setUser(await api.login(id, password)), []);
  const logout = useCallback(async () => {
    try {
      await api.logout();
    } finally {
      setUser(null);
    }
  }, []);

  const value = useMemo(() => ({ user, loading, login, logout, setUser }), [user, loading, login, logout]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export const useAuth = () => useContext(AuthContext);
