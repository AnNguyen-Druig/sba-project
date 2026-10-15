import { featuredRoomsMock } from '../data/rooms.mock'
import type { RoomListing } from '../types'

/**
 * Lớp gọi dữ liệu phòng. Hiện trả dữ liệu giả; khi nối backend chỉ cần thay thân hàm
 * bằng fetch tới API tìm phòng, component không phải sửa.
 *
 * Xem thử các trạng thái bằng query string: ?demo=loading | empty | error
 */
const MOCK_DELAY_MS = 600

function demoMode(): string | null {
  return new URLSearchParams(window.location.search).get('demo')
}

const wait = (ms: number) => new Promise<void>((resolve) => setTimeout(resolve, ms))

export async function getFeaturedRooms(): Promise<RoomListing[]> {
  const demo = demoMode()
  await wait(demo === 'loading' ? 60_000 : MOCK_DELAY_MS)

  if (demo === 'error') throw new Error('MOCK_NETWORK_ERROR')
  if (demo === 'empty') return []
  return featuredRoomsMock
}
