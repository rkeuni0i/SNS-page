import { useEffect, useState } from 'react';
import { api } from '../api';
import UserRow from '../components/UserRow';

export default function UsersPage() {
  const [users, setUsers] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    api.users().then(setUsers).catch((e) => setError(e.message));
  }, []);

  return (
    <>
      <h1 className="page-title">가입자</h1>
      {error && <p className="error">{error}</p>}
      {users?.map((u) => <UserRow key={u.id} user={u} />)}
    </>
  );
}
