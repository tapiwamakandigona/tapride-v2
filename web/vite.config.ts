import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  // Base path for GitHub Pages deployment (repo name)
  base: process.env.GITHUB_ACTIONS ? '/tapride-v2/' : '/',
  resolve: {
    alias: {
      '@': '/src',
    },
  },
});
