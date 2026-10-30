export class ApiError extends Error {
  constructor(status, message) {
    super(message);
    this.status = status;
  }
}

// access 토큰은 메모리에만 둔다(localStorage에 두면 XSS로 탈취될 수 있다).
// 새로고침으로 사라지면 HttpOnly 쿠키의 refresh 토큰으로 다시 받는다.
let accessToken = null;
let onAuthLost = () => {};

/** 재발급까지 실패해 로그인이 풀렸을 때 호출할 함수를 등록한다. */
export const setAuthLostHandler = (handler) => {
  onAuthLost = handler;
};

async function send(method, url, body) {
  const options = { method, headers: {}, credentials: 'same-origin' };
  if (accessToken) options.headers.Authorization = `Bearer ${accessToken}`;
  if (body instanceof FormData) {
    options.body = body; // Content-Type(boundary)은 브라우저가 설정한다
  } else if (body !== undefined) {
    options.headers['Content-Type'] = 'application/json';
    options.body = JSON.stringify(body);
  }

  const res = await fetch(url, options);
  const text = await res.text();
  return { res, data: text ? JSON.parse(text) : null };
}

const fail = ({ res, data }) => new ApiError(res.status, data?.message ?? '요청에 실패했습니다.');

// 동시에 여러 요청이 만료되어도 재발급은 한 번만 보낸다.
// refresh 토큰은 1회용(rotation)이라 같은 토큰으로 두 번 보내면 두 번째가 거부된다.
let refreshing = null;

function refresh() {
  if (!refreshing) {
    refreshing = send('POST', '/api/auth/refresh')
      .then((result) => {
        if (!result.res.ok) throw fail(result);
        accessToken = result.data.accessToken;
        return result.data;
      })
      .finally(() => {
        refreshing = null;
      });
  }
  return refreshing;
}

async function request(method, url, body, { retry = true } = {}) {
  let result = await send(method, url, body);

  // 서버가 "토큰이 없거나 만료"라고 알려 온 경우(WWW-Authenticate)에만 재발급 후 한 번 재시도한다.
  if (result.res.status === 401 && retry && result.res.headers.has('WWW-Authenticate')) {
    try {
      await refresh();
    } catch {
      accessToken = null;
      onAuthLost();
      throw fail(result);
    }
    result = await send(method, url, body);
  }

  if (!result.res.ok) throw fail(result);
  return result.data;
}

const userUrl = (id) => `/api/users/${encodeURIComponent(id)}`;
const NO_RETRY = { retry: false };

export const api = {
  /** 새로고침 후 로그인 상태 복원. refresh 쿠키가 유효하면 사용자 정보를 돌려준다. */
  restore: () => refresh().then((data) => data.user),
  login: async (id, password) => {
    const data = await request('POST', '/api/auth/login', { id, password }, NO_RETRY);
    accessToken = data.accessToken;
    return data.user;
  },
  logout: async () => {
    try {
      await request('POST', '/api/auth/logout', undefined, NO_RETRY);
    } finally {
      accessToken = null;
    }
  },
  signUp: (form) => request('POST', '/api/users', form, NO_RETRY),
  withdraw: async (password) => {
    await request('DELETE', '/api/users/me', { password });
    accessToken = null;
  },

  users: () => request('GET', '/api/users'),
  profile: (id) => request('GET', userUrl(id)),
  followers: (id) => request('GET', `${userUrl(id)}/followers`),
  following: (id) => request('GET', `${userUrl(id)}/following`),
  follow: (id) => request('POST', `/api/follows/${encodeURIComponent(id)}`),
  unfollow: (id) => request('DELETE', `/api/follows/${encodeURIComponent(id)}`),

  feeds: (page = 0, authorId) => {
    const params = new URLSearchParams({ page });
    if (authorId) params.set('authorId', authorId);
    return request('GET', `/api/feeds?${params}`);
  },
  createFeed: (content, image) => {
    const form = new FormData();
    form.append('content', content);
    if (image) form.append('image', image);
    return request('POST', '/api/feeds', form);
  },
};
