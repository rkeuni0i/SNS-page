import { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api } from '../api';
import { useAuth } from '../auth';
import Avatar from '../components/Avatar';
import FeedList from '../components/FeedList';
import UserRow from '../components/UserRow';

function WithdrawForm({ onDone }) {
  const [open, setOpen] = useState(false);
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!window.confirm('정말 탈퇴하시겠습니까? 작성한 피드도 모두 삭제됩니다.')) return;
    setError('');
    try {
      await api.withdraw(password);
      onDone();
    } catch (err) {
      setError(err.message);
    }
  };

  if (!open) {
    return <button type="button" className="link-btn danger" onClick={() => setOpen(true)}>회원 탈퇴</button>;
  }
  return (
    <form className="form" onSubmit={handleSubmit}>
      <input type="password" placeholder="비밀번호를 다시 입력해 주세요" value={password} onChange={(e) => setPassword(e.target.value)} required autoFocus />
      {error && <p className="error">{error}</p>}
      <button type="submit" className="btn btn-danger btn-block">탈퇴하기</button>
    </form>
  );
}

export default function ProfilePage() {
  const { id } = useParams();
  const { user: me, setUser } = useAuth();
  const navigate = useNavigate();
  const [profile, setProfile] = useState(null);
  const [error, setError] = useState('');
  const [list, setList] = useState(null); // { title, users }

  const isSelf = me.id === id;

  const loadProfile = useCallback(() => {
    setError('');
    return api.profile(id).then(setProfile).catch((e) => setError(e.message));
  }, [id]);

  useEffect(() => {
    setProfile(null);
    setList(null);
    loadProfile();
  }, [loadProfile]);

  const toggleFollow = async () => {
    try {
      await (profile.following ? api.unfollow(id) : api.follow(id));
      await loadProfile();
    } catch (e) {
      setError(e.message);
    }
  };

  const showList = async (kind) => {
    const title = kind === 'followers' ? '팔로워' : '팔로잉';
    if (list?.title === title) return setList(null);
    try {
      const users = await (kind === 'followers' ? api.followers(id) : api.following(id));
      setList({ title, users });
    } catch (e) {
      setError(e.message);
    }
  };

  const handleWithdrawn = () => {
    setUser(null); // 서버가 세션을 이미 폐기했다
    navigate('/login');
  };

  if (error && !profile) {
    return (
      <div className="card message-card">
        <p className="error">{error}</p>
        <Link to="/users" className="btn btn-outline">가입자 목록으로</Link>
      </div>
    );
  }
  if (!profile) return <p className="empty-state">불러오는 중...</p>;

  return (
    <>
      <section className="card profile-card">
        <Avatar name={profile.name} large />
        <div className="profile-name">{profile.name}</div>
        <div className="user-id">{profile.id}</div>
        <div className="profile-joined">가입일 {profile.createdAt?.slice(0, 10)}</div>
        <div className="profile-stats">
          <button type="button" className="stat" onClick={() => showList('followers')}>
            <span className="stat-num">{profile.followerCount}</span><span className="stat-label">팔로워</span>
          </button>
          <button type="button" className="stat" onClick={() => showList('following')}>
            <span className="stat-num">{profile.followingCount}</span><span className="stat-label">팔로잉</span>
          </button>
        </div>
        {error && <p className="error">{error}</p>}
        {isSelf ? (
          <WithdrawForm onDone={handleWithdrawn} />
        ) : (
          <button type="button" className={profile.following ? 'btn btn-outline' : 'btn'} onClick={toggleFollow}>
            {profile.following ? '팔로잉 취소' : '팔로우'}
          </button>
        )}
      </section>

      {list && (
        <>
          <h2 className="page-title">{list.title}</h2>
          {list.users.length === 0 && <div className="empty-state">아직 없습니다.</div>}
          {list.users.map((u) => <UserRow key={u.id} user={u} />)}
        </>
      )}

      <h2 className="page-title">{isSelf ? '내 피드' : `${profile.name}님의 피드`}</h2>
      <FeedList authorId={id} emptyText="아직 작성한 피드가 없습니다." />
    </>
  );
}
