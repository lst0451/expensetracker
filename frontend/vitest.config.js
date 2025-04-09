import { defineConfig } from 'vitest/config'
import vue2 from '@vitejs/plugin-vue2'
import path from 'path'

export default defineConfig({
    plugins: [vue2()],
    test: {
        globals: true,
        environment: 'jsdom'
    },
    resolve: {
        alias: {
            '@': path.resolve(__dirname, './src')
        }
    }
})