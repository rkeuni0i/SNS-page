import { useCallback, useEffect, useState } from 'react';
import { api } from '../api';
import FeedCard from './FeedCard';

const PAGE_SIZE = 50; // FeedService.PAGE_SIZE 와 같아야 "더 보기" 여부를 판단할 수 있다

/** authorId가 있으면 그 회원의 피드, 없으면 전체 피드. reloadKey가 바뀌면 처음부터 다시 읽는다. */
export default function FeedList({ authorId, reloadKey = 0, emptyText }) {
  const [feeds, setFeeds] = useState([]);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = useCallback(async (nextPage, replace) => {
    setLoading(true);
    setError('');
    try {
      const data = await api.feeds(nextPage, authorId);
      setFeeds((prev) => (replace ? data : [...prev, ...data]));
      setPage(nextPage);
      setHasMore(data.length === PAGE_SIZE);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  }, [authorId]);

  useEffect(() => {
    load(0, true);
  }, [load, reloadKey]);

  return (
    <>
      {error && <p className="error">{error}</p>}
      {!loading && !error && feeds.length === 0 && <div className="empty-state">{emptyText}</div>}
      {feeds.map((feed) => <FeedCard key={feed.no} feed={feed} />)}
      {hasMore && (
        <button type="button" className="btn btn-outline btn-block" disabled={loading} onClick={() => load(page + 1, false)}>
          {loading ? '불러오는 중...' : '더 보기'}
        </button>
      )}
    </>
  );
}
