import { useEffect, useRef, useState } from 'react';
import { api } from '../api';
import { useAuth } from '../auth';
import FeedList from '../components/FeedList';

const MAX_IMAGE_BYTES = 5 * 1024 * 1024;

function Composer({ onPosted }) {
  const [content, setContent] = useState('');
  const [image, setImage] = useState(null);
  const [preview, setPreview] = useState(null);
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const fileInput = useRef(null);

  // 미리보기용 object URL은 바뀔 때마다 해제해 메모리 누수를 막는다.
  useEffect(() => {
    if (!image) {
      setPreview(null);
      return undefined;
    }
    const url = URL.createObjectURL(image);
    setPreview(url);
    return () => URL.revokeObjectURL(url);
  }, [image]);

  const handleFile = (e) => {
    const file = e.target.files[0] ?? null;
    if (file && file.size > MAX_IMAGE_BYTES) {
      setError('이미지 용량이 5MB를 초과했습니다.');
      e.target.value = '';
      return;
    }
    setError('');
    setImage(file);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      await api.createFeed(content, image);
      setContent('');
      setImage(null);
      if (fileInput.current) fileInput.current.value = '';
      onPosted();
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form className="card form" onSubmit={handleSubmit}>
      <textarea placeholder="이곳에 글을 작성해 주세요." rows={3} maxLength={4096} value={content} onChange={(e) => setContent(e.target.value)} required />
      {preview && <img className="feed-image" src={preview} alt="미리보기" />}
      <input ref={fileInput} type="file" accept="image/jpeg,image/png,image/gif,image/webp" onChange={handleFile} />
      <p className="desc">이미지는 선택 사항이며 최대 5MB입니다.</p>
      {error && <p className="error">{error}</p>}
      <button type="submit" className="btn btn-block" disabled={submitting || !content.trim()}>
        {submitting ? '올리는 중...' : '업로드하기'}
      </button>
    </form>
  );
}

export default function FeedPage() {
  const { user } = useAuth();
  const [reloadKey, setReloadKey] = useState(0);

  return (
    <>
      <h1 className="page-title">{user.name}님의 피드</h1>
      <Composer onPosted={() => setReloadKey((k) => k + 1)} />
      <FeedList reloadKey={reloadKey} emptyText="아직 등록된 피드가 없습니다. 첫 번째 글을 남겨보세요." />
    </>
  );
}
