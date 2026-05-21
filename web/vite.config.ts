import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  // Always serve from root — custom domain tapride.tapiwa.me is at /
  base: '/',
  resolve: {
    alias: {
      '@': '/src',
    },
  },
});
