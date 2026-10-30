import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api } from '../api';

export default function SignUpPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ id: '', password: '', passwordConfirm: '', name: '' });
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const set = (key) => (e) => setForm({ ...form, [key]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      await api.signUp(form);
      alert('가입이 완료되었습니다. 로그인해 주세요.');
      navigate('/login');
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form className="card form" onSubmit={handleSubmit}>
      <h1>회원가입</h1>
      <input type="email" placeholder="아이디(이메일주소)" value={form.id} onChange={set('id')} required autoFocus />
      <input type="password" placeholder="패스워드(8자 이상)" value={form.password} onChange={set('password')} required />
      <input type="password" placeholder="패스워드 확인" value={form.passwordConfirm} onChange={set('passwordConfirm')} required />
      <input type="text" placeholder="이름" maxLength={32} value={form.name} onChange={set('name')} required />
      {error && <p className="error">{error}</p>}
      <button type="submit" className="btn btn-block" disabled={submitting}>가입하기</button>
      <Link to="/login" className="btn btn-outline btn-block">로그인으로 돌아가기</Link>
    </form>
  );
}
