# 🚀 LinkSnip Production Deployment Guide

This guide walks you through deploying LinkSnip for free using:
- **Backend + PostgreSQL + Redis**: [Render](https://render.com) (or [Railway](https://railway.app))
- **Frontend**: [Vercel](https://vercel.com)
- **Source Control**: [GitHub](https://github.com)

---

## 📋 Architecture Overview in Production

```
                                      ┌────────────────────────────────┐
                                      │        User's Browser          │
                                      └───────┬────────────────┬───────┘
                                              │                │
                        1. Visit Website      │                │ 2. Create Short URL / API
                                              ▼                ▼
                         ┌───────────────────────┐   ┌────────────────────────────────┐
                         │   Vercel (Frontend)   │   │     Render.com (Backend API)   │
                         │ linksnip.vercel.app   │   │  linksnip-api.onrender.com     │
                         └───────────────────────┘   └────────┬───────────────┬───────┘
                                                              │               │
                                                              ▼               ▼
                                                     ┌────────────────┐ ┌─────────────┐
                                                     │ PostgreSQL 16  │ │   Redis 7   │
                                                     │   (Database)   │ │   (Cache)   │
                                                     └────────────────┘ └─────────────┘
```

---

## 1️⃣ Step 1: Push Code to GitHub

Open Terminal in your Mac and run these commands to push your project to a new repository on GitHub:

```bash
cd "/Users/adarshagrawal/Desktop/untitled folder"

# 1. Initialize git
git init
git add .
git commit -m "feat: complete LinkSnip fullstack application"

# 2. Rename branch to main
git branch -M main

# 3. Create a new repository on https://github.com/new named "linksnip"
# Then link and push (replace YOUR_USERNAME with your GitHub username):
git remote add origin https://github.com/YOUR_USERNAME/linksnip.git
git push -u origin main
```

---

## 2️⃣ Step 2: Deploy Backend, PostgreSQL & Redis (on Render.com)

1. Sign up / Log in to **[Render.com](https://render.com)** (sign in with GitHub).

### A. Create PostgreSQL Database:
1. In Render Dashboard, click **New +** $\rightarrow$ **PostgreSQL**.
2. **Name**: `linksnip-db`
3. **Database**: `linksnip`
4. **User**: `linksnip`
5. **Plan**: Free
6. Click **Create Database**.
7. Once created, copy the **Internal Database URL** (e.g. `postgres://linksnip:password@dpg-...:5432/linksnip`).

### B. Create Redis Instance:
1. In Render Dashboard, click **New +** $\rightarrow$ **Redis** (or use free Redis at [Upstash.com](https://upstash.com)).
2. **Name**: `linksnip-redis`
3. **Plan**: Free
4. Click **Create Redis**.
5. Copy the **Internal Redis Host** and **Port** (or connection string).

### C. Create Web Service (Spring Boot Backend):
1. In Render Dashboard, click **New +** $\rightarrow$ **Web Service**.
2. Select your `linksnip` repository from GitHub.
3. Configure the service:
   - **Name**: `linksnip-api`
   - **Root Directory**: `url-shortener`
   - **Language / Runtime**: **Docker**
   - **Region**: Same region as your database
   - **Instance Type**: Free
4. Under **Environment Variables**, add:
   | Key | Value | Notes |
   |---|---|---|
   | `SPRING_PROFILES_ACTIVE` | `docker` | Enables container settings |
   | `SPRING_DATASOURCE_URL` | `jdbc:postgresql://HOST:PORT/linksnip` | Use the Render DB hostname/port with `jdbc:` prefix |
   | `DB_USERNAME` | `linksnip` | Database user from Step A |
   | `DB_PASSWORD` | *(your Render DB password)* | Database password from Step A |
   | `REDIS_HOST` | *(your Render Redis host)* | Redis hostname from Step B |
   | `REDIS_PORT` | `6379` | Redis port |
   | `REDIS_PASSWORD` | *(optional)* | If using Upstash or password-protected Redis |
   | `APP_BASE_URL` | `https://linksnip-api.onrender.com` | Your Render web service public URL |
   | `CORS_ALLOWED_ORIGINS` | `*` | Allows Vercel frontend |
   | `IP_HASH_PEPPER` | *(any random string)* | e.g. `prod-pepper-linksnip-secret-key-1234` |
5. Click **Create Web Service**.

> Render will build the Docker container and start your Spring Boot app. Once finished, it will display your backend URL (e.g. `https://linksnip-api.onrender.com`).

---

## 3️⃣ Step 3: Deploy Frontend (on Vercel)

1. Sign up / Log in to **[Vercel.com](https://vercel.com)** with your GitHub account.
2. Click **Add New...** $\rightarrow$ **Project**.
3. Select the `linksnip` repository.
4. In the **Configure Project** screen:
   - **Project Name**: `linksnip`
   - **Framework Preset**: **Vite** (auto-detected)
   - **Root Directory**: Click *Edit* and select **`url-shortener-frontend`**.
5. Open **Environment Variables** and add:
   | Key | Value |
   |---|---|
   | `VITE_API_BASE_URL` | `https://linksnip-api.onrender.com` *(your backend URL from Step 2)* |
6. Click **Deploy**.

---

## 4️⃣ Step 4: Verification

1. Open your Vercel deployment URL (e.g. `https://linksnip.vercel.app`).
2. Paste any long link (e.g. `https://google.com`) and click **Shorten Link**.
3. Verify:
   - ✅ Short URL is generated immediately.
   - ✅ QR code modal pops up and allows downloading the PNG.
   - ✅ Clicking or scanning the short link redirects you to the target website!
