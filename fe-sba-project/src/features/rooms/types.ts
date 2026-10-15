/** Kiểu dữ liệu phòng công khai — bám theo ROOMS / ROOM_TYPES / ROOM_SLOTS trong ERD. */
export type RentalMode = 'WHOLE_ROOM' | 'SLOT'

export type RoomStatus = 'AVAILABLE' | 'HELD' | 'OCCUPIED' | 'MAINTENANCE'

export interface RoomListing {
  id: string
  code: string
  title: string
  branchName: string
  address: string
  areaKey: string
  /** Giá theo tháng; với SLOT là giá mỗi chỗ */
  price: number
  sizeM2: number
  capacity: number
  availableSlots: number
  rentalMode: RentalMode
  roomTypeName: string
  status: RoomStatus
  imageUrl: string
  imageAlt: string
}

export interface RoomSearchFilters {
  area?: string
  price?: string
  mode?: RentalMode
}
