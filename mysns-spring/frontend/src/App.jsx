import { Navigate, Route, Routes } from 'react-router-dom';
import Layout from './components/Layout';
import { useAuth } from './auth';
import FeedPage from './pages/FeedPage';
import LoginPage from './pages/LoginPage';
import ProfilePage from './pages/ProfilePage';
import SignUpPage from './pages/SignUpPage';
import UsersPage from './pages/UsersPage';

function RequireAuth({ children }) {
  const { user, loading } = useAuth();
  if (loading) return <p className="empty-state">불러오는 중...</p>;
  return user ? children : <Navigate to="/login" replace />;
}

export default function App() {
  return (
    <Routes>
      <Route element={<Layout />}>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignUpPage />} />
        <Route path="/" element={<RequireAuth><FeedPage /></RequireAuth>} />
        <Route path="/users" element={<RequireAuth><UsersPage /></RequireAuth>} />
        <Route path="/users/:id" element={<RequireAuth><ProfilePage /></RequireAuth>} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Route>
    </Routes>
  );
}
