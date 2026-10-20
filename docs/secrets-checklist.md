# Deployment Secrets Checklist — Rysoo Platform

| Service | Secret Name | Purpose | Destination |
|---|---|---|---|
| **Render** | `SPRING_DATASOURCE_URL` | Supabase PostgreSQL Connection URL | Render Environment Group |
| **Render** | `SPRING_DATASOURCE_USERNAME` | PostgreSQL database user (`postgres`) | Render Environment Group |
| **Render** | `SPRING_DATASOURCE_PASSWORD` | PostgreSQL database password | Render Environment Group |
| **Render** | `SUPABASE_SERVICE_ROLE_KEY` | Private key for bucket pre-signing & admin storage | Render Environment Group |
| **Render** | `OPENAI_API_KEY` (or Anthropic/Gemini) | Server-side AI model authorization | Render Environment Group |
| **GitHub Actions** | `RENDER_API_DEPLOY_HOOK_URL` | Webhook URL triggering auto-deploy of API | GitHub Repository Secrets |
| **GitHub Actions** | `RENDER_WORKER_DEPLOY_HOOK_URL` | Webhook URL triggering auto-deploy of Worker | GitHub Repository Secrets |
| **Vercel** | `VITE_SUPABASE_URL` | Public Supabase API URL | Vercel Project Environment Variables |
| **Vercel** | `VITE_SUPABASE_ANON_KEY` | Public Supabase Anon Key | Vercel Project Environment Variables |
| **Vercel** | `VITE_API_BASE_URL` | Render API public endpoint (e.g., `https://rysoo-api.onrender.com/api/v1`) | Vercel Project Environment Variables |
| **Expo EAS** | `EXPO_PUBLIC_SUPABASE_URL` | Mobile Supabase API URL | EAS Secrets / Environment Variables |
| **Expo EAS** | `EXPO_PUBLIC_SUPABASE_ANON_KEY` | Mobile Supabase Anon Key | EAS Secrets / Environment Variables |
| **Expo EAS** | `EXPO_PUBLIC_API_BASE_URL` | Mobile Render API public endpoint | EAS Secrets / Environment Variables |
