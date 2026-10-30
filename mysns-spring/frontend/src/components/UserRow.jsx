import { Link } from 'react-router-dom';
import Avatar from './Avatar';

export default function UserRow({ user }) {
  return (
    <Link to={`/users/${encodeURIComponent(user.id)}`} className="card user-row">
      <Avatar name={user.name} />
      <div>
        <div className="user-name">{user.name}</div>
        <div className="user-id">{user.id}</div>
      </div>
    </Link>
  );
}
