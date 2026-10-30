import { useState } from 'react';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import { useAuth } from '../auth';

export default function LoginPage() {
  const { user, login } = useAuth();
  const navigate = useNavigate();
  const [id, setId] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  if (user) return <Navigate to="/" replace />;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      await login(id, password);
      navigate('/');
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <>
      <form className="card form" onSubmit={handleSubmit}>
        <h1>로그인</h1>
        <input type="email" placeholder="아이디(이메일주소)" value={id} onChange={(e) => setId(e.target.value)} required autoFocus />
        <input type="password" placeholder="패스워드" value={password} onChange={(e) => setPassword(e.target.value)} required />
        {error && <p className="error">{error}</p>}
        <button type="submit" className="btn btn-block" disabled={submitting}>로그인하기</button>
      </form>
      <div className="card form">
        <p className="desc">아이디가 없으세요? 간단한 가입절차를 통해 MySNS의 회원이 되실 수 있습니다.</p>
        <Link to="/signup" className="btn btn-outline btn-block">회원가입하기</Link>
      </div>
    </>
  );
}
