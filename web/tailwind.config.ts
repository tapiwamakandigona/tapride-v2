import type { Config } from 'tailwindcss';

const config: Config = {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        brand: {
          50: '#fef9ee',
          100: '#fdf0d0',
          200: '#f9dd9d',
          300: '#f5c561',
          400: '#f0a929',
          500: '#ec8f0a',
          600: '#d16d06',
          700: '#ad4e09',
          800: '#8c3e0f',
          900: '#733410',
        },
      },
    },
  },
  plugins: [],
};

export default config;
