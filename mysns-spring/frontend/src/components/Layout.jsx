import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth';

export default function Layout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  return (
    <>
      <header className="page-hdr">
        <div className="hdr-inner">
          <Link to="/" className="logo">MySNS</Link>
          {user && (
            <nav>
              <NavLink to="/" end>피드</NavLink>
              <NavLink to="/users" end>가입자</NavLink>
              <NavLink to={`/users/${encodeURIComponent(user.id)}`}>마이페이지</NavLink>
              <button type="button" className="link-btn" onClick={handleLogout}>로그아웃</button>
            </nav>
          )}
        </div>
      </header>
      <main className="container">
        <Outlet />
      </main>
    </>
  );
}
