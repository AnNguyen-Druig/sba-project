import { createBrowserRouter } from 'react-router'

import { HomePage } from '@/features/home/home-page'
import { PublicLayout } from '@/layouts/public-layout'
import { ComingSoonPage } from '@/pages/coming-soon-page'
import { NotFoundPage } from '@/pages/not-found-page'

export const router = createBrowserRouter([
  {
    element: <PublicLayout />,
    children: [
      { index: true, element: <HomePage /> },
      { path: 'rooms', element: <ComingSoonPage title="Tìm phòng" /> },
      { path: 'rooms/:roomId', element: <ComingSoonPage title="Chi tiết phòng" /> },
      { path: 'login', element: <ComingSoonPage title="Đăng nhập" /> },
      { path: 'register', element: <ComingSoonPage title="Đăng ký" /> },
      { path: 'tenant/my-room', element: <ComingSoonPage title="Phòng của tôi" /> },
      { path: '*', element: <NotFoundPage /> },
    ],
  },
])
