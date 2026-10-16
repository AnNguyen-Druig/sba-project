import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { RouterProvider } from 'react-router/dom'
import { QueryClientProvider } from '@tanstack/react-query'
import { router } from './router'
import { queryClient } from './services/query-client'
import { recoverSession } from './services/http'
import { hasMocks } from './services/config'
import { useSession } from './features/auth/session-store'
import './index.css'
async function start() {
  if (hasMocks) {
    const { worker } = await import('./mocks/browser')
    await worker.start({ onUnhandledFrame: 'bypass', quiet: true })
  }
  void recoverSession().catch(() => {
    if (!useSession.getState().accessToken) useSession.getState().clear()
  })
  const root = document.getElementById('root')
  if (!root) throw new Error('Không tìm thấy phần tử #root.')
  createRoot(root).render(
    <StrictMode>
      <QueryClientProvider client={queryClient}>
        <RouterProvider router={router} />
      </QueryClientProvider>
    </StrictMode>,
  )
}
void start().catch(() => {
  const root = document.getElementById('root')
  if (root) {
    root.textContent = 'Chưa khởi động được TroPro. Kiểm tra cấu hình API và tải lại trang.'
    root.setAttribute('role', 'alert')
  }
})
