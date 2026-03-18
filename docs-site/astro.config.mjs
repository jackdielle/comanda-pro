import { defineConfig } from 'astro/config';
import tailwind from '@astrojs/tailwind';
import sitemap from '@astrojs/sitemap';

// https://astro.build/config
export default defineConfig({
  site: 'https://docs.comanda-pro.com',
  integrations: [
    tailwind(),
    sitemap()
  ],
  vite: {
    ssr: {
      external: ['svgo']
    }
  },
  output: 'static',
  trailingSlash: 'ignore'
});
