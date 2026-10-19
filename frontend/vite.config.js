import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const ici = path.dirname(fileURLToPath(import.meta.url));

// Développement : `npm run dev` (http://localhost:5173) — les appels /api et /uploads sont relayés vers Spring Boot (8080).
// Production    : `mvn spring-boot:run` / `mvn package` lancent `npm run build` (frontend-maven-plugin) ;
//                 Vite écrit alors l'interface dans target/classes/static, que Spring Boot sert directement
//                 (une seule application sur http://localhost:8080). Rien n'est généré dans src/.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': 'http://localhost:8080',
      '/uploads': 'http://localhost:8080',
    },
  },
  build: {
    outDir: path.resolve(ici, '../target/classes/static'),
    emptyOutDir: true,
  },
});
