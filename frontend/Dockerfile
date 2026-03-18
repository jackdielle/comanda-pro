# Stage 1: Build Angular App
FROM node:20-alpine AS builder

WORKDIR /app

# Copia package files
COPY package*.json ./

# Installa dependencies
RUN npm ci

# Copia il codice sorgente
COPY . .

# Build the app
RUN npm run build

# Stage 2: Serve with Nginx
FROM nginx:alpine

# Copia la configurazione nginx
COPY nginx.conf /etc/nginx/nginx.conf

# Copia i file compilati dalla fase di build
COPY --from=builder /app/dist/gestore-comande-frontend /usr/share/nginx/html

# Expose port
EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=10s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost/ || exit 1

# Run nginx
CMD ["nginx", "-g", "daemon off;"]
