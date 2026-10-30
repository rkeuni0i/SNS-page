import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

const backend = process.env.BACKEND_URL ?? 'http://localhost:8081';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    // 개발 중에는 API/이미지 요청을 Spring Boot로 넘긴다(세션 쿠키가 같은 출처로 동작).
    proxy: {
      '/api': backend,
      '/images': backend,
    },
  },
  build: {
    // 빌드 결과물을 Spring Boot가 정적 파일로 서빙한다 → jar 하나로 배포.
    outDir: '../src/main/resources/static',
    emptyOutDir: true,
  },
});
