import { Link } from 'react-router-dom';

// "2026-10-30T11:34:48" → "2026-10-30 11:34"
const formatTime = (iso) => (iso ? iso.replace('T', ' ').slice(0, 16) : '');

export default function FeedCard({ feed }) {
  return (
    <article className="card feed-card">
      <div className="feed-meta">
        <Link className="feed-author" to={`/users/${encodeURIComponent(feed.authorId)}`}>
          {feed.authorId}
        </Link>
        <span className="feed-time">{formatTime(feed.createdAt)}</span>
      </div>
      {feed.imageUrl && <img className="feed-image" src={feed.imageUrl} alt="" />}
      {/* React가 텍스트를 이스케이프하므로 XSS 걱정 없이 그대로 출력한다(줄바꿈은 CSS pre-wrap). */}
      <div className="feed-content">{feed.content}</div>
    </article>
  );
}
