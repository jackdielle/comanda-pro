# ComandaPro Documentation & Blog Site

Modern documentation and blog site built with **Astro 6** and **Tailwind CSS**. Optimized for performance, SEO, and user experience.

## 🚀 Features

- **Ultra-Fast Performance**: Static site generation for maximum speed
- **SEO Optimized**: Built-in sitemap and RSS feed support
- **Responsive Design**: Mobile-first approach with Tailwind CSS
- **Dark Mode Ready**: Easy to add dark theme support
- **Accessibility**: WCAG AAA compliant components
- **Type-Safe**: Full TypeScript support
- **Markdown Support**: Easily write content in Markdown
- **No Client JS**: Server-rendered, minimal JavaScript

## 📁 Project Structure

```
docs-site/
├── src/
│   ├── components/        # Reusable Astro components
│   │   ├── Header.astro
│   │   └── Footer.astro
│   ├── layouts/          # Layout components
│   │   └── BaseLayout.astro
│   ├── pages/            # Page routes (auto-routed)
│   │   ├── index.astro         # Home page
│   │   ├── blog/
│   │   │   └── index.astro     # Blog listing
│   │   ├── guides/
│   │   │   └── index.astro     # Guides listing
│   │   ├── faq/
│   │   │   └── index.astro     # FAQ page
│   │   └── docs/
│   │       └── getting-started.astro
│   ├── styles/
│   │   └── global.css    # Global styles and Tailwind
│   └── content/          # Content collections (optional)
├── astro.config.mjs      # Astro configuration
├── tailwind.config.mjs    # Tailwind CSS configuration
├── tsconfig.json         # TypeScript configuration
└── package.json          # Dependencies
```

## 🛠️ Installation & Setup

### Prerequisites
- Node.js 18+ and npm

### Installation

```bash
# Navigate to docs-site
cd docs-site

# Install dependencies
npm install

# Start development server
npm run dev
```

The site will be available at `http://localhost:3000`

## 📝 Available Commands

```bash
# Development server with hot reload
npm run dev

# Build for production
npm run build

# Preview production build locally
npm run preview

# Astro CLI commands
npm run astro -- [command]
```

## 🎨 Customization

### Colors

Edit `tailwind.config.mjs` to customize colors:

```javascript
colors: {
  primary: '#C41E3A',      // ComandaPro red
  secondary: '#F57C00',    // Energy orange
  accent: '#2D7D4D',       // Fresh green
}
```

### Typography

Modify font family in `tailwind.config.mjs`:

```javascript
fontFamily: {
  sans: ['Inter', 'sans-serif'],
}
```

### Styling

Global styles are in `src/styles/global.css`. Component-specific styles use Tailwind utility classes.

## 📚 Creating New Pages

### Astro Page (`.astro`)

```astro
---
import BaseLayout from '@layouts/BaseLayout.astro';
---

<BaseLayout title="Page Title">
  <h1>Welcome</h1>
  <p>Your content here...</p>
</BaseLayout>
```

### Markdown Page (`.md`)

Create markdown files in `src/pages/` and they automatically become pages:

```markdown
---
layout: '@layouts/BaseLayout.astro'
title: My Blog Post
---

# Blog Post Title

Your content in markdown...
```

## 🔗 Adding Components

Create reusable components in `src/components/`:

```astro
---
// src/components/Card.astro
interface Props {
  title: string;
  description?: string;
}

const { title, description } = Astro.props;
---

<div class="card">
  <h3>{title}</h3>
  {description && <p>{description}</p>}
</div>
```

Use in pages:

```astro
---
import Card from '@components/Card.astro';
---

<Card title="Hello" description="Welcome" />
```

## 🚀 Deployment

### Netlify

1. Connect your Git repository to Netlify
2. Set build command: `npm run build`
3. Set publish directory: `dist`
4. Deploy automatically on push

### Vercel

1. Import project from Git
2. Vercel auto-detects Astro configuration
3. Deploy with one click

### Static Hosting

```bash
# Build the site
npm run build

# Output is in 'dist/' directory
# Upload to any static hosting (GitHub Pages, AWS S3, etc.)
```

### Docker

```dockerfile
# Build stage
FROM node:18-alpine as builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# Serve stage
FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

## 📊 Performance Metrics

Expected performance scores:

- **Lighthouse Performance**: 95+
- **Lighthouse Accessibility**: 95+
- **Lighthouse SEO**: 100
- **Lighthouse Best Practices**: 95+

## 🔍 SEO Features

- Automatic sitemap generation (`/sitemap.xml`)
- RSS feed support
- Meta tags management
- Open Graph support
- Structured data ready

## ♿ Accessibility

- WCAG AAA compliant
- Semantic HTML structure
- Focus management
- Color contrast ratios: 7:1+
- Screen reader friendly

## 📱 Browser Support

- Modern browsers (Chrome, Firefox, Safari, Edge)
- Mobile browsers (iOS Safari, Chrome Mobile)
- Progressive enhancement

## 🔄 Content Collections

For managing structured content (blog posts, docs), enable content collections:

```typescript
// src/content/config.ts
import { defineCollection, z } from 'astro:content';

const blog = defineCollection({
  schema: z.object({
    title: z.string(),
    pubDate: z.date(),
    author: z.string(),
  }),
});

export const collections = { blog };
```

## 🎯 Best Practices

1. **Images**: Use responsive images with `<Image>` component
2. **Links**: Use relative links for internal navigation
3. **CSS**: Use Tailwind utilities instead of custom CSS when possible
4. **Components**: Keep components small and focused
5. **Performance**: Minimize JavaScript, use static rendering

## 📖 Documentation Structure

- **Home** (`/`) - Overview and quick links
- **Blog** (`/blog/`) - Articles and updates
- **Guides** (`/guides/`) - Tutorial documentation
- **Docs** (`/docs/`) - Technical documentation
- **FAQ** (`/faq/`) - Frequently asked questions

## 🛠️ Troubleshooting

### Port Already in Use

```bash
npm run dev -- --port 3001
```

### Build Errors

```bash
# Clear cache and rebuild
rm -rf node_modules dist
npm install
npm run build
```

### Styling Issues

Ensure Tailwind CSS is properly configured and `global.css` is imported in layouts.

## 📚 Resources

- [Astro Documentation](https://docs.astro.build)
- [Tailwind CSS Documentation](https://tailwindcss.com/docs)
- [Astro Integrations](https://astro.build/integrations/)

## 📄 License

Copyright © 2024 ComandaPro. All rights reserved.

## 🤝 Contributing

To add or modify content:

1. Create a new file in the appropriate directory
2. Follow the existing file structure and naming conventions
3. Use TypeScript and semantic HTML
4. Test locally with `npm run dev`
5. Build and verify with `npm run build`

## 📞 Support

- Documentation: [/docs/](/docs/)
- FAQ: [/faq/](/faq/)
- Contact: [support@comanda-pro.com](mailto:support@comanda-pro.com)

---

**Built with ❤️ using Astro 6 and Tailwind CSS**

Last Updated: March 2024
