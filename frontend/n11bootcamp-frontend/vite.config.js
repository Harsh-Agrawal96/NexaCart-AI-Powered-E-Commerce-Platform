import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      // Avoid browser CORS/preflight issues during local dev by proxying API calls through Vite.
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
        secure: false,
      },
      "/actuator": {
        target: "http://localhost:8080",
        changeOrigin: true,
        secure: false,
      },
      // Gateway üzerinden mikroservis route’ları (AdminServices api-docs ping için şart)
      "/user-service": {
        target: "http://localhost:8080",
        changeOrigin: true,
        secure: false,
      },
      "/product-service": {
        target: "http://localhost:8080",
        changeOrigin: true,
        secure: false,
      },
      "/cart-service": {
        target: "http://localhost:8080",
        changeOrigin: true,
        secure: false,
      },
      "/order-service": {
        target: "http://localhost:8080",
        changeOrigin: true,
        secure: false,
      },
      "/payment-service": {
        target: "http://localhost:8080",
        changeOrigin: true,
        secure: false,
      },
      "/log-service": {
        target: "http://localhost:8080",
        changeOrigin: true,
        secure: false,
      },
      "/review-service": {
        target: "http://localhost:8080",
        changeOrigin: true,
        secure: false,
      },
      "/ai-service": {
        target: "http://localhost:8080",
        changeOrigin: true,
        secure: false,
      },
    },
  },
})
