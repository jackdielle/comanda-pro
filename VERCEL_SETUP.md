# Vercel Deployment Setup Guide

## The Problem

Exit code 127 means `ng` command not found. This happens because Vercel doesn't know:
1. Where your frontend code is (it's in `frontend/` subdirectory)
2. What the build command should be
3. Where the output files are

## The Solution ✅

Two files are now configured:

### 1. `vercel.json` (Root Directory)

```json
{
  "buildCommand": "cd frontend && npm run build",
  "outputDirectory": "frontend/dist/gestore-comande-frontend"
}
```

**What this does**:
- `buildCommand`: Navigates to `frontend/` folder, then runs the npm build script
- `outputDirectory`: Points to where Angular outputs the built files

### 2. `frontend/package.json` (Updated)

```json
"build": "ng build --configuration production"
```

Changed from `"ng build"` (dev) to `"ng build --configuration production"` (prod).

## How Vercel Now Builds

When you push to GitHub:

```
1. Vercel clones your repo
2. Reads vercel.json
3. Runs: cd frontend && npm run build
4. npm runs the build script with production config
5. Angular outputs to: frontend/dist/gestore-comande-frontend
6. Vercel serves those static files ✅
```

## Deployment Steps in Vercel UI

When connecting to GitHub in Vercel:

### Option A: Let Vercel Auto-Detect
1. Click "Connect Git Repository"
2. Select your repo
3. Vercel should auto-detect the `vercel.json` ✅
4. Click "Deploy"

### Option B: Manual Configuration (If Auto-Detect Fails)
1. In Vercel Project Settings:
   - **Root Directory**: `frontend`
   - **Build Command**: `npm run build -- --configuration production`
   - **Output Directory**: `dist/gestore-comande-frontend`
2. Click "Deploy"

**Note**: With `vercel.json` present, you shouldn't need to set these manually.

## Local Testing

Before pushing to Vercel, test the production build locally:

```bash
cd frontend

# Install dependencies
npm install

# Build for production (same as Vercel will do)
npm run build

# You should see output like:
# ✔ Compilation successful
# ✔ Build at: frontend/dist/gestore-comande-frontend

# Optional: Run locally to test
npx http-server dist/gestore-comande-frontend -p 8000
# Open http://localhost:8000
```

## Troubleshooting Build Failures

### Still Getting Error 127?

1. **Clear Vercel cache**:
   - Go to Vercel project settings
   - Click "Settings" → "Git"
   - Under "Deployments", click "Clear"
   - Redeploy

2. **Check Vercel build logs**:
   - Go to Vercel dashboard
   - Click on the failed deployment
   - Check the build logs for specific errors

3. **Verify vercel.json is in root**:
   ```bash
   ls -la vercel.json
   # Should show the file in project root, not in frontend/
   ```

### Build Fails with TypeScript Errors

1. Check your code locally:
   ```bash
   cd frontend
   ng build --configuration production
   ```

2. Fix any errors shown locally

3. Commit and push

### Build Succeeds but Site Shows Blank Page

1. Check environment variables in frontend
2. Ensure API URL in `environment.prod.ts` is correct
3. Check browser console for errors

## Environment Configuration

Your `environment.prod.ts` is already set with your Render URL:

```typescript
export const environment = {
  production: true,
  apiUrl: 'https://comanda-pro.onrender.com/api'
};
```

During the production build, this file is automatically used (thanks to the fileReplacements in `angular.json`).

## Files Reference

| File | Purpose | Location |
|------|---------|----------|
| `vercel.json` | Build configuration | Root directory |
| `package.json` | Build scripts | `frontend/` |
| `environment.prod.ts` | Production API URL | `frontend/src/environments/` |
| `angular.json` | Environment replacements | `frontend/` |

## Complete Deployment Checklist

- [ ] `vercel.json` is in project root
- [ ] `frontend/package.json` has `"build": "ng build --configuration production"`
- [ ] `frontend/src/environments/environment.prod.ts` has your Render URL
- [ ] Committed and pushed all changes to GitHub
- [ ] Connected to Vercel (or re-triggered build)
- [ ] Vercel build completed successfully
- [ ] No errors in Vercel deployment logs
- [ ] Frontend loads at vercel.app URL
- [ ] API calls work (check Network tab in DevTools)

## If Everything Still Fails

Last resort - explicitly set in Vercel UI:

1. Project Settings → General
2. **Build & Development Settings**:
   - Framework Preset: `Other`
   - Build Command: `cd frontend && npm run build`
   - Output Directory: `frontend/dist/gestore-comande-frontend`
   - Install Command: `npm install`
3. Save and redeploy

## Summary

✅ **vercel.json** tells Vercel where your frontend is
✅ **package.json** has correct build command
✅ **environment.prod.ts** has your Render URL
✅ **Push to GitHub** → Vercel auto-deploys

No more exit code 127! 🎉
