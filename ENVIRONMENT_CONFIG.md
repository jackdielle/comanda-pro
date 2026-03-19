# Environment Configuration Guide

## Problem Solved ✅

Previously, updating the frontend API URL to the Render backend would break local development. Now it works in both contexts automatically.

## How It Works

Angular's environment system automatically selects the right configuration:

```
Local Development                   Production Build
────────────────                   ────────────────
ng serve                           ng build --configuration production
        ↓                                    ↓
environment.ts                     environment.prod.ts
        ↓                                    ↓
apiUrl: '/api'                      apiUrl: 'https://...onrender.com/api'
        ↓                                    ↓
Backend: http://localhost:8080      Backend: https://comanda-pro.onrender.com
        ↓                                    ↓
Works locally ✅                    Works on cloud ✅
```

---

## Files Overview

### Development Environment

**File**: `frontend/src/environments/environment.ts`

```typescript
export const environment = {
  production: false,
  apiUrl: '/api'  // Relative URL - works with local proxy
};
```

**Used by**:
- `ng serve` (development server)
- Local testing on `http://localhost:4200`
- Proxies to backend on `http://localhost:8080/api`

---

### Production Environment

**File**: `frontend/src/environments/environment.prod.ts`

```typescript
export const environment = {
  production: true,
  apiUrl: 'https://comanda-pro.onrender.com/api'  // ← UPDATE THIS
};
```

**Used by**:
- `ng build --configuration production`
- Vercel deployment
- Points to your actual Render backend URL

**You only need to update this once after deploying the backend to Render.**

---

## Usage in Code

### api.service.ts

```typescript
import { environment } from '../../environments/environment';

const API_URL = environment.apiUrl;

// Now automatically uses:
// - '/api' during local development
// - 'https://comanda-pro.onrender.com/api' in production
```

### auth.service.ts

```typescript
import { environment } from '../../environments/environment';

export class AuthService {
  private apiUrl = environment.apiUrl;

  // Same behavior: correct URL in both contexts
}
```

---

## Build Configurations

### angular.json Configuration

```json
{
  "build": {
    "configurations": {
      "production": {
        "fileReplacements": [
          {
            "replace": "src/environments/environment.ts",
            "with": "src/environments/environment.prod.ts"
          }
        ]
      }
    }
  }
}
```

**What this does**:
- During development build: Use `environment.ts`
- During production build: Replace `environment.ts` with `environment.prod.ts`
- Angular CLI handles this automatically

---

## Local Development Workflow

### Step 1: Start Backend
```bash
cd backend
mvn spring-boot:run
# Backend runs on http://localhost:8080
```

### Step 2: Start Frontend
```bash
cd frontend
ng serve
# Frontend runs on http://localhost:4200
# Proxy config forwards /api to localhost:8080
```

### Step 3: Test Locally
- Open `http://localhost:4200` in browser
- Login, create orders, etc.
- API calls go to `http://localhost:8080/api` ✅
- No hardcoded URLs, no manual switching!

---

## Production Deployment Workflow

### Before First Production Deploy

1. Deploy backend to Render (get URL like `https://comanda-pro.onrender.com`)
2. Update `environment.prod.ts` with your actual Render URL:
   ```typescript
   apiUrl: 'https://your-actual-url.onrender.com/api'
   ```
3. Commit and push to GitHub

### During Production Build

```bash
# Vercel runs this automatically:
ng build --configuration production

# Which automatically:
# 1. Replaces environment.ts with environment.prod.ts
# 2. Uses your Render URL
# 3. Bundles for production
```

### Result

- Frontend on Vercel calls backend on Render ✅
- Correct URL injected automatically ✅
- No manual configuration needed ✅

---

## Never Do This

❌ **Don't hardcode backend URL in services**:
```typescript
// BAD - breaks local development
const API_URL = 'https://comanda-pro.onrender.com/api';
```

❌ **Don't manually switch between URLs**:
```typescript
// BAD - error-prone
if (isDevelopment) { apiUrl = '/api'; }
else { apiUrl = 'https://...'; }
```

✅ **Do use environment files** (already set up for you)

---

## Updating for Your Render URL

Once backend is deployed:

1. **Get your Render URL**: `https://comanda-pro.onrender.com` (or your actual URL)
2. **Update**: `frontend/src/environments/environment.prod.ts`
   ```typescript
   apiUrl: 'https://comanda-pro.onrender.com/api'  // Replace if different
   ```
3. **Commit**:
   ```bash
   git add frontend/src/environments/environment.prod.ts
   git commit -m "Update Render backend URL"
   git push origin main
   ```
4. **Vercel auto-redeploys** with the new URL ✅

---

## Troubleshooting

### Local Development: API Calls Fail

**Check**:
1. Backend running? `curl http://localhost:8080/api/health`
2. Frontend using correct proxy? Check `proxy.conf.json` exists
3. Browser DevTools → Network → see requests to `/api/...` (relative)

**Fix**:
```bash
# Kill and restart both:
mvn spring-boot:run    # Terminal 1
ng serve               # Terminal 2
```

### Production: API Calls Fail with CORS Error

**Check**:
1. Vercel URL is in Render's `CORS_ALLOWED_ORIGINS`
2. `environment.prod.ts` has correct Render URL
3. Backend CORS configured correctly

**Fix**:
1. Update `environment.prod.ts` with correct URL
2. Update CORS in Render dashboard
3. Redeploy frontend: `git push origin main`

### "Cannot find module '@angular/core'"

Likely after pulling new environment files.

**Fix**:
```bash
cd frontend
npm install
ng serve
```

---

## Summary

| Aspect | Local Dev | Production |
|--------|-----------|-----------|
| **File used** | `environment.ts` | `environment.prod.ts` |
| **API URL** | `/api` (relative) | `https://...onrender.com/api` (absolute) |
| **Backend** | `localhost:8080` | `comanda-pro.onrender.com` |
| **Proxy** | `proxy.conf.json` | Direct HTTPS request |
| **Build** | `ng serve` | `ng build --configuration production` |
| **Works?** | ✅ Yes | ✅ Yes |

**Zero manual switching. Everything automatic. 🎉**
