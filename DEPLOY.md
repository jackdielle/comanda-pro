# Deployment Guide — Gestore Comande v2.0

## Overview

This guide walks through deploying the Gestore Comande application to free-tier cloud services with zero monthly costs. The application consists of:
- **Frontend**: Angular 18 static site
- **Backend**: Spring Boot 3.2 REST API
- **Database**: PostgreSQL (free tier serverless)

---

## Recommended Architecture

```
┌─────────────────────────────────────────────────────┐
│                    Your Users                       │
└─────────────────────────┬───────────────────────────┘
                          │
                    ┌─────▼──────┐
                    │   Vercel   │  (Frontend)
                    │  Angular18 │
                    └─────┬──────┘
                          │
                          │ proxied to
                          │
                    ┌─────▼──────────┐
                    │  Render.com    │  (Backend)
                    │ Spring Boot    │
                    └─────┬──────────┘
                          │
                    ┌─────▼──────┐
                    │  Neon.tech │  (Database)
                    │ PostgreSQL │
                    └────────────┘
```

### Service Selection

| Component | Service | Free Tier | Why |
|-----------|---------|-----------|-----|
| **Frontend** | Vercel | Unlimited | Static site hosting, global CDN, Git auto-deploy, zero cost forever |
| **Backend** | Render.com | 512MB RAM, sleep after 15 min inactivity | Free web services, Docker support, PostgreSQL integration |
| **Database** | Neon.tech | 512MB storage, serverless | Free PostgreSQL, never expires, auto-scaling |

---

## Prerequisites

Before starting, create free accounts at:
1. **[Vercel](https://vercel.com/signup)** — Frontend hosting
2. **[Render.com](https://dashboard.render.com/)** — Backend hosting
3. **[Neon.tech](https://console.neon.tech/auth/signup)** — PostgreSQL database
4. **GitHub account** — To connect your repositories

**Recommended**: Fork this repository to your GitHub account so you can connect it to Render and Vercel.

---

## Step 1: Prepare the Database (Neon)

### 1.1 Create a Neon Project

1. Go to **[Neon Console](https://console.neon.tech/)**
2. Click **"Create a new project"**
3. Choose:
   - **Region**: Pick the closest to your users (e.g., `us-east-1`)
   - **Database name**: `gestore_db` (or any name)
   - **Postgres version**: Leave default (14+)
4. Click **"Create project"**

### 1.2 Get Your Connection String

1. In Neon Console, click your project
2. Click **"Connection strings"** tab
3. Copy the **PostgreSQL URI** that looks like:
   ```
   postgresql://user:password@ep-xyz.us-east-1.neon.tech/gestore_db?sslmode=require
   ```
4. **Save this securely** — you'll need it for environment variables

### 1.3 Verify Connection (Optional)

Install PostgreSQL client and test:
```bash
psql "postgresql://user:password@ep-xyz.us-east-1.neon.tech/gestore_db?sslmode=require" -c "SELECT version();"
```

---

## Step 2: Prepare the Backend Code

### 2.1 Verify New Files and Dependencies

Ensure your backend has:
- ✅ **PostgreSQL dependency added** to `backend/pom.xml`
- ✅ **New file**: `backend/src/main/resources/application-prod.properties` (environment-driven config)
- ✅ **Updated**: `backend/src/main/java/com/saleorigano/config/SecurityConfig.java` (CORS configurable)

These files should already be in place from the plan preparation. If not, see **Appendix: Manual Code Changes**.

### 2.2 Verify JWT Secret Handling

Check that `application-prod.properties` reads `jwt.secret` from `${JWT_SECRET}` env var (not hardcoded).

**Do NOT commit sensitive secrets to Git.**

---

## Step 3: Prepare the Frontend Code

### 3.1 Configure Environment-Based API URL

The frontend uses Angular's environment configuration to switch API URLs automatically:
- **Development** (`ng serve`): Uses relative URL `/api` for local development
- **Production** (`ng build --configuration production`): Uses absolute Render URL for deployed app

This approach means **local development continues to work**, while the production build gets the correct backend URL.

#### What's Already Done ✅

The following files have been created/updated:

1. **`frontend/src/environments/environment.ts`** (Development)
   ```typescript
   export const environment = {
     production: false,
     apiUrl: '/api'  // Relative URL for local dev with proxy
   };
   ```

2. **`frontend/src/environments/environment.prod.ts`** (Production)
   ```typescript
   export const environment = {
     production: true,
     apiUrl: 'https://comanda-pro.onrender.com/api'  // ← Update with YOUR Render URL
   };
   ```

3. **`frontend/angular.json`** — Configured to replace environment.ts with environment.prod.ts during production build

4. **`api.service.ts` and `auth.service.ts`** — Already updated to import from environment files

### 3.2 Update Production API URL (One-Time Setup)

You only need to update this **once** after your backend is deployed:

**File**: `frontend/src/environments/environment.prod.ts` (line 3)

Replace the placeholder with your actual Render backend URL:

**Current:**
```typescript
apiUrl: 'https://comanda-pro.onrender.com/api'  // Example URL
```

**After Step 4 (Deploy Backend)**, update to your actual URL:
```typescript
apiUrl: 'https://your-actual-render-url.onrender.com/api'
```

### 3.3 Local Development Setup

**Your local `ng serve` automatically uses** `environment.ts` with `/api`:

1. Start backend: `mvn spring-boot:run` (runs on `http://localhost:8080/api`)
2. Start frontend: `ng serve` (runs on `http://localhost:4200`)
3. Frontend automatically proxies `/api` calls to backend via `proxy.conf.json` ✅
4. **No manual URL changes needed for local work**

### 3.4 Commit Changes

```bash
git add frontend/src/environments/
git commit -m "Configure environment-based API URLs"
git push origin main
```

---

## Step 4: Deploy Backend to Render

### 4.1 Create a GitHub Repository (if you haven't already)

1. Fork or create a GitHub repository with your code
2. Push all changes to `main` branch

### 4.2 Connect to Render

1. Go to **[Render Dashboard](https://dashboard.render.com/)**
2. Click **"New +"** → **"Web Service"**
3. Select **"Build and deploy from a Git repository"**
4. Click **"Connect account"** and authorize GitHub
5. Select your repository and branch (`main`)
6. Choose deploy:
   - **Environment**: `Docker`
   - **Instance Type**: Free
7. Click **"Create Web Service"** (this will deploy a default)

### 4.3 Configure Environment Variables

Before the service starts building, you need to set env vars. In Render dashboard:

1. Go to your new Web Service
2. Click **"Environment"** tab
3. Add these environment variables:

| Key | Value | Example |
|-----|-------|---------|
| `DATABASE_URL` | Neon connection string | `postgresql://user:pwd@ep-xyz.us-east-1.neon.tech/gestore_db?sslmode=require` |
| `DATABASE_USERNAME` | Extracted from connection string | `user` |
| `DATABASE_PASSWORD` | Extracted from connection string | `password123` |
| `JWT_SECRET` | Strong random 256-bit string | `YOUR_SECURE_JWT_SECRET_KEY_MIN_32_CHARS_LONG===` |
| `JWT_EXPIRATION` | Token expiration ms (default 900000 = 15 min) | `900000` |
| `JWT_REFRESH_EXPIRATION` | Refresh token expiration (default 604800000 = 7 days) | `604800000` |
| `CORS_ALLOWED_ORIGINS` | Vercel frontend URL (added after Step 5) | `https://gestore-comande.vercel.app` |
| `SPRING_PROFILES_ACTIVE` | Activate production profile | `prod` |

### 4.4 Configure Dockerfile (if needed)

If Render doesn't auto-detect the Dockerfile, create `Dockerfile` at project root:

```dockerfile
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY backend/pom.xml .
RUN mvn dependency:resolve
COPY backend/src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
```

### 4.5 Check Deployment

1. Render will auto-build and deploy when you push to GitHub
2. Watch the **"Logs"** tab in Render for build progress
3. Once deployed, your backend URL will be: `https://your-service-name.onrender.com`
4. **Copy this URL** — you'll need it for the frontend in Step 3.1

### 4.6 Test Backend Health

```bash
curl https://your-service-name.onrender.com/api/health
```

You should see: `{"status":"UP"}`

---

## Step 5: Deploy Frontend to Vercel

### 5.1 Update Frontend API URL (if not done in Step 3)

Before deploying, ensure both `api.service.ts` and `auth.service.ts` have the correct Render backend URL.

### 5.2 Connect to Vercel

1. Go to **[Vercel Dashboard](https://vercel.com/dashboard)**
2. Click **"Add New +"** → **"Project"**
3. Select your GitHub repository
4. Choose settings:
   - **Framework**: `Angular`
   - **Root Directory**: `frontend`
   - **Build Command**: `ng build --configuration production`
   - **Output Directory**: `dist`
5. Click **"Deploy"**

Vercel will build and deploy automatically. Your frontend URL will be something like:
```
https://gestore-comande.vercel.app
```

---

## Step 6: Update Backend CORS Settings

Now that your frontend is deployed, update the backend CORS settings:

1. Go to **Render Dashboard** → Your Backend Service
2. Click **"Environment"**
3. Update the `CORS_ALLOWED_ORIGINS` variable:
   ```
   https://gestore-comande.vercel.app
   ```
4. Click **"Save"** — this triggers a redeploy with the new CORS settings

---

## Step 7: Verify the Deployment

### 7.1 Test Backend Endpoints

```bash
# Health check
curl https://your-service-name.onrender.com/api/health

# Try login (should fail without credentials, but endpoint should exist)
curl -X POST https://your-service-name.onrender.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'
```

### 7.2 Test Frontend

1. Open your Vercel URL: `https://gestore-comande.vercel.app`
2. You should see the login page
3. Try logging in with your credentials
4. Verify you can see orders, customers, products (depending on your user role)

### 7.3 Check Network Requests

In browser DevTools (F12):
1. Open **Network** tab
2. Try logging in
3. You should see requests to `https://your-render-url/api/auth/login`
4. Response should return `accessToken` and `refreshToken`

---

## Troubleshooting

### Backend Not Starting

**Symptom**: Render shows build success but service won't start.

**Solutions**:
- Check **"Logs"** tab in Render for detailed error messages
- Verify all env vars are set correctly (DATABASE_URL, JWT_SECRET, etc.)
- Ensure Neon database connection string is correct (try copy-pasting from Neon again)
- Check that `application-prod.properties` exists and is properly formatted

### CORS Errors

**Symptom**: Frontend can't call backend, browser shows CORS error.

**Solutions**:
- Verify `CORS_ALLOWED_ORIGINS` in Render matches your exact Vercel URL
- Ensure backend CORS_ALLOWED_ORIGINS is set and redeployed
- Check backend logs: `CORS allowed origins: [...]`

### Database Connection Errors

**Symptom**: Backend starts but can't connect to database.

**Solutions**:
- Test Neon connection manually: `psql "your-connection-string"`
- Verify `DATABASE_URL` is correct (includes `?sslmode=require`)
- Check Neon console for connection limits (free tier allows a few connections)
- Ensure PostgreSQL driver is in `pom.xml`

### Token Expiration Issues

**Symptom**: After 15 minutes, user is logged out unexpectedly.

**Solutions**:
- This is expected behavior on Render free tier (service sleeps after 15 min inactivity)
- When backend wakes up, tokens may be invalidated
- No fix needed — expected for free tier
- Increase token expiration if desired: set `JWT_EXPIRATION` env var to larger value

### Frontend Builds Fail

**Symptom**: Vercel deploy fails during build.

**Solutions**:
- Check Vercel **"Deployments"** tab for build logs
- Ensure Angular build command is correct: `ng build --configuration production`
- Verify all dependencies are installed: `npm install` in `frontend/` directory
- Check for TypeScript errors: run `ng build` locally first

---

## Free Tier Limitations & Considerations

| Service | Limitation | Impact | Workaround |
|---------|-----------|--------|-----------|
| **Render Backend** | Sleeps after 15 min inactivity | ~10-30s delay on first request after sleep | None (expected on free tier) |
| **Render** | 512MB RAM | Should be fine for Spring Boot + small DB | Optimize or upgrade if needed |
| **Neon** | 512MB storage | Plenty for small pizza store | Monitor usage in Neon console |
| **Vercel** | Limited to 6 concurrent function requests | No issue for small team | None |
| **Bandwidth** | Free tier limits apply | Sufficient for small business | Consider upgrade if scaling |

---

## Alternative Services

If you prefer different providers, here are alternatives:

### All-in-One Alternative: Railway.app
```
Railway → Backend + Database + Storage (all in one)
- Pros: Single platform, automatic provisioning, free tier USD $5/month credit
- Cons: Limited to $5/month free credit (eventually runs out)
```

### Backend-Only Alternative: Fly.io
```
Fly.io → Backend with persistent volumes
- Pros: Better free tier than Render, built-in volumes for persistent storage
- Cons: Slightly more complex setup, less straightforward database integration
```

### Database Alternative: Supabase
```
Supabase → PostgreSQL + API (all-in-one)
- Pros: Extra features (Auth, Realtime), 500MB free storage
- Cons: Overkill if you just need PostgreSQL
```

---

## Environment Variables Reference

Complete list of environment variables for production deployment:

```bash
# Database (Required)
DATABASE_URL=postgresql://user:password@host/dbname?sslmode=require
DATABASE_USERNAME=user
DATABASE_PASSWORD=password

# JWT (Required)
JWT_SECRET=your_secure_256bit_secret_key_minimum_32_chars
JWT_EXPIRATION=900000
JWT_REFRESH_EXPIRATION=604800000

# CORS (Required)
CORS_ALLOWED_ORIGINS=https://your-vercel-url.vercel.app

# Spring (Required)
SPRING_PROFILES_ACTIVE=prod
```

---

## Post-Deployment Checklist

- [ ] Backend is deployed and running on Render
- [ ] Frontend is deployed and running on Vercel
- [ ] Database is created and accessible from backend
- [ ] CORS is configured for your Vercel frontend URL
- [ ] JWT_SECRET is set to a secure value (not default/hardcoded)
- [ ] Login works and tokens are returned
- [ ] API calls from frontend to backend succeed
- [ ] Orders, customers, and products can be accessed
- [ ] Admin functions (user management) work correctly
- [ ] No CORS errors in browser console

---

## Monitoring & Maintenance

### Check Backend Health
```bash
curl https://your-service-name.onrender.com/api/health
```

### View Backend Logs (Render Dashboard)
- Go to Web Service → "Logs" tab
- Filter by log level (INFO, ERROR, DEBUG)

### Check Database Usage (Neon)
- Go to Neon Console → Your Project
- View storage usage and connection count

### Redeploy Backend (after code changes)
```bash
git add .
git commit -m "Your changes"
git push origin main  # Render auto-deploys on push
```

### Redeploy Frontend (after code changes)
```bash
git add .
git commit -m "Your changes"
git push origin main  # Vercel auto-deploys on push
```

---

## Scaling Up (Beyond Free Tier)

When you're ready to move beyond free tier:

1. **Render Backend**: Upgrade to paid tier for always-on service (no sleep)
2. **Neon Database**: Upgrade for larger storage and more connections
3. **Vercel**: No upgrade needed (free tier is production-grade)
4. **Alternative**: Consider all-in-one like Render or Railway for simpler pricing

---

## FAQ

**Q: Why use three different services instead of one all-in-one platform?**
- A: Vercel is free forever for static sites and is the best choice for Angular. Render and Neon are complementary free services. All-in-one platforms (Railway, Fly) eventually charge you.

**Q: Can I use my own domain name?**
- A: Yes. Both Vercel and Render support custom domains. Add your domain in their settings and update CORS if needed.

**Q: What if I want to keep using H2 database?**
- A: H2 requires persistent storage (volumes) which is expensive on free tiers. PostgreSQL on Neon is a better choice. However, for local development, H2 remains ideal.

**Q: How do I back up my database?**
- A: Neon provides automatic backups. Manual backups: use `pg_dump` connected to your Neon PostgreSQL instance.

**Q: Can I deploy just the backend for now?**
- A: Yes, but update the frontend API URL to point to your backend before deploying.

---

## Support & Debugging

If something goes wrong:

1. **Check service logs**:
   - Render: Web Service → Logs tab
   - Vercel: Deployments tab → select deployment → Logs
   - Neon: Browse console for connection errors

2. **Test connectivity**:
   ```bash
   # Test backend
   curl https://your-backend-url/api/health

   # Test database (from your local machine)
   psql "your-neon-connection-string"
   ```

3. **Review environment variables**:
   - Verify all required vars are set
   - Check for typos or copy-paste errors
   - Ensure secrets are not exposed in logs

4. **Review code changes**:
   - Verify `application-prod.properties` syntax
   - Ensure SecurityConfig CORS is properly formatted
   - Check frontend API URL is correct

---

## Next Steps

Once deployed:
1. Configure backup strategy for PostgreSQL
2. Set up monitoring alerts (Render offers basic free monitoring)
3. Plan for data growth and scaling
4. Add custom domain name (optional)
5. Set up CI/CD for automated testing on push (optional)

Happy deploying! 🚀
