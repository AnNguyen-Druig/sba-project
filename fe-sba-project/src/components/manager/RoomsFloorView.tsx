import React, { useState } from 'react';
import { Room } from '../../types';
import { facilityInfo } from '../../data/mockData';

interface RoomsFloorViewProps {
  rooms: Room[];
  selectedRoomId: string;
  onSelectRoom: (roomId: string) => void;
  onOpenRoomDetail: (roomId: string) => void;
  onOpenAddRoom: () => void;
}

export const RoomsFloorView: React.FC<RoomsFloorViewProps> = ({
  rooms,
  selectedRoomId,
  onSelectRoom,
  onOpenRoomDetail,
  onOpenAddRoom
}) => {
  const [searchTerm, setSearchTerm] = useState('Nguyễn Hoàng Long');
  const [statusFilter, setStatusFilter] = useState<'all' | 'rented' | 'available' | 'pending' | 'maintenance'>('all');
  const [expandedFloors, setExpandedFloors] = useState<{ [floor: number]: boolean }>({
    1: false,
    2: true, // Floor 2 expanded by default as in design
    3: false
  });

  const toggleFloor = (floor: number) => {
    setExpandedFloors((prev) => ({
      ...prev,
      [floor]: !prev[floor]
    }));
  };

  const filteredRooms = rooms.filter((r) => {
    if (searchTerm.trim()) {
      const matchName = r.currentTenant?.toLowerCase().includes(searchTerm.toLowerCase());
      const matchRoom = r.roomNumber.toLowerCase().includes(searchTerm.toLowerCase());
      if (!matchName && !matchRoom) return false;
    }
    if (statusFilter === 'rented') return r.status === 'rented';
    if (statusFilter === 'available') return r.status === 'available';
    if (statusFilter === 'pending') return r.status === 'pending_contract';
    if (statusFilter === 'maintenance') return r.status === 'maintenance';
    return true;
  });

  return (
    <div className="flex flex-col w-full p-6 lg:p-8 gap-6 max-w-[1600px] mx-auto">
      {/* Page Title & Header Actions */}
      <div className="flex flex-col xl:flex-row xl:items-center justify-between gap-4">
        <div className="flex flex-col gap-1">
          <div className="flex items-center gap-2">
            <span className="text-xs uppercase tracking-wider text-primary font-bold">
              TroPro Sài Gòn • Cơ sở Đinh Bộ Lĩnh
            </span>
            <span className="w-1.5 h-1.5 rounded-full bg-outline-variant"></span>
            <span className="text-xs text-on-surface-variant font-medium">Quy mô: 38 Phòng (3 Tầng)</span>
          </div>
          <h1 className="font-headline text-2xl lg:text-3xl font-bold text-on-surface tracking-tight">
            Sơ đồ Phòng & Hồ sơ Khách thuê
          </h1>
        </div>

        <div className="flex flex-wrap items-center gap-2.5">
          <button
            type="button"
            onClick={() => alert('Đang xuất danh sách tạm trú định dạng chuẩn CA Phường (Excel/PDF)...')}
            className="inline-flex items-center gap-2 px-3.5 py-2 rounded-xl bg-surface-container-lowest text-primary font-semibold text-xs border border-surface-container shadow-xs hover:bg-surface-container transition-all cursor-pointer"
          >
            <span className="material-symbols-outlined text-base">download</span>
            <span>Xuất DS tạm trú (Excel/PDF)</span>
          </button>

          <button
            type="button"
            onClick={() => alert('Mở camera quét thẻ Căn cước công dân gắn chip hoặc QR Code...')}
            className="inline-flex items-center gap-2 px-3.5 py-2 rounded-xl bg-surface-container-lowest text-on-surface-variant font-semibold text-xs border border-surface-container shadow-xs hover:bg-surface-container transition-all cursor-pointer"
          >
            <span className="material-symbols-outlined text-base">qr_code_scanner</span>
            <span>Quét CCCD</span>
          </button>

          <button
            type="button"
            onClick={onOpenAddRoom}
            className="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl bg-secondary-container text-white font-bold text-xs shadow-md hover:bg-secondary transition-all cursor-pointer active:scale-95"
          >
            <span className="material-symbols-outlined text-base">add</span>
            <span>+ Thêm phòng / Khách thuê</span>
          </button>
        </div>
      </div>

      {/* Filter Bar & Quick Status Legend */}
      <div className="p-4 rounded-xl bg-surface-container-lowest border border-surface-container shadow-xs flex flex-col xl:flex-row gap-4 items-stretch xl:items-center justify-between">
        <div className="flex flex-wrap items-center gap-3">
          <div className="relative min-w-[280px]">
            <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline text-base">
              search
            </span>
            <input
              type="text"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              placeholder="Tìm tên, SĐT, số CCCD, mã phòng..."
              className="w-full pl-9 pr-8 py-2 rounded-lg bg-surface-container-low text-xs text-on-surface placeholder:text-outline focus:outline-none focus:bg-surface-container transition-colors"
            />
            {searchTerm && (
              <button
                onClick={() => setSearchTerm('')}
                className="absolute right-2.5 top-1/2 -translate-y-1/2 text-outline hover:text-on-surface text-xs"
              >
                <span className="material-symbols-outlined text-sm">close</span>
              </button>
            )}
          </div>

          <div className="flex items-center p-1 bg-surface-container rounded-lg text-xs">
            <button
              type="button"
              onClick={() => setStatusFilter('all')}
              className={`px-3 py-1.5 rounded font-semibold transition-all flex items-center gap-1.5 cursor-pointer ${
                statusFilter === 'all'
                  ? 'bg-surface-container-lowest text-primary shadow-xs'
                  : 'text-on-surface-variant hover:text-on-surface'
              }`}
            >
              <span>Tất cả</span>
              <span className="px-1.5 py-0.2 rounded-full bg-primary/10 text-primary text-[10px] font-bold">38</span>
            </button>

            <button
              type="button"
              onClick={() => setStatusFilter('rented')}
              className={`px-3 py-1.5 rounded transition-all flex items-center gap-1.5 cursor-pointer ${
                statusFilter === 'rented'
                  ? 'bg-surface-container-lowest text-primary font-semibold shadow-xs'
                  : 'text-on-surface-variant hover:text-on-surface'
              }`}
            >
              <span className="w-2 h-2 rounded-full bg-primary"></span>
              <span>Đang thuê</span>
              <span className="text-[10px] text-on-surface-variant font-medium">(34)</span>
            </button>

            <button
              type="button"
              onClick={() => setStatusFilter('available')}
              className={`px-3 py-1.5 rounded transition-all flex items-center gap-1.5 cursor-pointer ${
                statusFilter === 'available'
                  ? 'bg-surface-container-lowest text-emerald-700 font-semibold shadow-xs'
                  : 'text-on-surface-variant hover:text-on-surface'
              }`}
            >
              <span className="w-2 h-2 rounded-full bg-emerald-500"></span>
              <span>Trống</span>
              <span className="text-[10px] text-emerald-700 font-semibold">(2)</span>
            </button>

            <button
              type="button"
              onClick={() => setStatusFilter('pending')}
              className={`px-3 py-1.5 rounded transition-all flex items-center gap-1.5 cursor-pointer ${
                statusFilter === 'pending'
                  ? 'bg-surface-container-lowest text-amber-700 font-semibold shadow-xs'
                  : 'text-on-surface-variant hover:text-on-surface'
              }`}
            >
              <span className="w-2 h-2 rounded-full bg-amber-500"></span>
              <span>Chờ ký cọc</span>
              <span className="text-[10px] text-amber-700 font-semibold">(1)</span>
            </button>

            <button
              type="button"
              onClick={() => setStatusFilter('maintenance')}
              className={`px-3 py-1.5 rounded transition-all flex items-center gap-1.5 cursor-pointer ${
                statusFilter === 'maintenance'
                  ? 'bg-surface-container-lowest text-secondary font-semibold shadow-xs'
                  : 'text-on-surface-variant hover:text-on-surface'
              }`}
            >
              <span className="w-2 h-2 rounded-full bg-secondary"></span>
              <span>Bảo trì</span>
              <span className="text-[10px] text-secondary font-semibold">(1)</span>
            </button>
          </div>
        </div>
      </div>

      {/* Collapsible Floor Folders */}
      <div className="flex flex-col gap-4">
        {/* TẦNG 1 FOLDER (COLLAPSIBLE) */}
        <div className="rounded-2xl bg-surface-container-lowest shadow-xs border border-surface-container overflow-hidden transition-all">
          <div
            onClick={() => toggleFloor(1)}
            className="p-4 flex items-center justify-between cursor-pointer hover:bg-surface-container-low/60 transition-colors"
          >
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-surface-container flex items-center justify-center text-primary font-bold">
                <span className="material-symbols-outlined">apartment</span>
              </div>
              <div className="flex flex-col">
                <div className="flex items-center gap-2">
                  <span className="font-headline text-base text-on-surface font-bold">
                    Tầng 1 - Khu trệt & Lối đi riêng
                  </span>
                  <span className="px-2 py-0.5 rounded-full bg-surface-container text-on-surface-variant text-[11px] font-semibold">
                    12 phòng
                  </span>
                </div>
                <span className="text-xs text-on-surface-variant">
                  Thích hợp kinh doanh nhỏ, có nhà xe nội bộ và camera an ninh 24/7
                </span>
              </div>
            </div>

            <div className="flex items-center gap-4">
              <div className="hidden sm:flex items-center gap-2 text-xs">
                <span className="px-2.5 py-0.5 rounded-full bg-primary/10 text-primary font-semibold">11 Đang thuê</span>
                <span className="px-2.5 py-0.5 rounded-full bg-emerald-500/10 text-emerald-700 font-semibold">1 Trống</span>
              </div>
              <span className={`material-symbols-outlined text-on-surface-variant text-xl transition-transform ${
                expandedFloors[1] ? 'rotate-180' : ''
              }`}>
                expand_more
              </span>
            </div>
          </div>

          {expandedFloors[1] && (
            <div className="p-4 bg-surface-container-lowest border-t border-surface-container">
              <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3">
                {rooms.filter((r) => r.floor === 1).map((room) => (
                  <div
                    key={room.id}
                    onClick={() => onOpenRoomDetail(room.id)}
                    className="p-4 rounded-xl border border-surface-container hover:border-primary/50 shadow-xs hover:shadow-md transition-all cursor-pointer flex flex-col justify-between gap-3 bg-surface-container-lowest"
                  >
                    <div className="flex items-start justify-between">
                      <div>
                        <span className="font-headline text-lg font-bold text-on-surface">{room.roomNumber}</span>
                        <span className="block text-xs text-on-surface-variant">{room.area}m² • Trệt</span>
                      </div>
                      <span className={`px-2 py-0.5 rounded-full text-xs font-semibold ${
                        room.status === 'available' ? 'bg-emerald-500/10 text-emerald-700' : 'bg-primary/10 text-primary'
                      }`}>
                        {room.status === 'available' ? 'Trống' : 'Đang thuê'}
                      </span>
                    </div>

                    <div className="flex flex-col gap-0.5">
                      <span className="text-xs font-semibold text-on-surface truncate">
                        {room.currentTenant || 'Sẵn sàng dọn vào'}
                      </span>
                      <span className="text-xs text-on-surface-variant">
                        {room.price.toLocaleString()} đ/tháng
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        {/* TẦNG 2 FOLDER (EXPANDED BY DEFAULT) */}
        <div className="rounded-2xl bg-surface-container-lowest shadow-sm border-2 border-primary/30 overflow-hidden transition-all">
          <div
            onClick={() => toggleFloor(2)}
            className="p-4 bg-surface-container-low/50 border-b border-surface-container flex items-center justify-between cursor-pointer hover:bg-surface-container-low transition-colors"
          >
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-primary-container text-white flex items-center justify-center font-bold shadow-xs">
                <span className="material-symbols-outlined">apartment</span>
              </div>
              <div className="flex flex-col">
                <div className="flex items-center gap-2">
                  <span className="font-headline text-base text-on-surface font-bold">
                    Tầng 2 - Căn hộ tiêu chuẩn ban công
                  </span>
                  <span className="px-2 py-0.5 rounded-full bg-primary/10 text-primary text-[11px] font-semibold">
                    14 phòng
                  </span>
                  <span className="text-[10px] font-bold text-primary uppercase bg-primary-fixed/30 px-2 py-0.5 rounded">
                    Đang chọn
                  </span>
                </div>
                <span className="text-xs text-on-surface-variant">
                  Phòng có gác đúc, ban công thoáng gió, công tơ điện thông minh
                </span>
              </div>
            </div>

            <div className="flex items-center gap-4">
              <div className="hidden sm:flex items-center gap-2 text-xs">
                <span className="px-2.5 py-0.5 rounded-full bg-primary/10 text-primary font-semibold">12 Đang thuê</span>
                <span className="px-2.5 py-0.5 rounded-full bg-emerald-500/10 text-emerald-700 font-semibold">1 Trống</span>
                <span className="px-2.5 py-0.5 rounded-full bg-secondary/10 text-secondary font-semibold">1 Bảo trì</span>
              </div>
              <span className={`material-symbols-outlined text-primary text-xl transition-transform ${
                expandedFloors[2] ? 'rotate-180' : ''
              }`}>
                expand_more
              </span>
            </div>
          </div>

          {expandedFloors[2] && (
            <div className="p-4 bg-surface-container-lowest">
              <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3">
                {/* Room 201 */}
                <div
                  onClick={() => onOpenRoomDetail('r-201')}
                  className="p-4 rounded-xl bg-surface-container-lowest border border-surface-container hover:border-primary/50 shadow-xs hover:shadow-md transition-all cursor-pointer flex flex-col justify-between gap-3"
                >
                  <div className="flex items-start justify-between">
                    <div>
                      <span className="font-headline text-lg text-on-surface font-bold">P.201</span>
                      <span className="block text-xs text-on-surface-variant">26m² • Studio</span>
                    </div>
                    <span className="px-2 py-0.5 rounded-full bg-primary/10 text-primary text-xs font-semibold">
                      Đang thuê
                    </span>
                  </div>
                  <div className="flex flex-col gap-0.5">
                    <span className="text-xs text-on-surface font-semibold truncate">Võ Hoài Nam</span>
                    <span className="text-xs text-on-surface-variant">4.800.000 đ/tháng</span>
                  </div>
                  <div className="flex items-center justify-between pt-2 border-t border-surface-container text-xs">
                    <span className="text-on-surface-variant flex items-center gap-1">
                      <span className="material-symbols-outlined text-sm">group</span> 2/2 người
                    </span>
                    <span className="text-primary font-semibold">Còn 4 tháng</span>
                  </div>
                </div>

                {/* Room 202 */}
                <div
                  onClick={() => onOpenRoomDetail('r-202')}
                  className="p-4 rounded-xl bg-surface-container-lowest border border-surface-container hover:border-primary/50 shadow-xs hover:shadow-md transition-all cursor-pointer flex flex-col justify-between gap-3"
                >
                  <div className="flex items-start justify-between">
                    <div>
                      <span className="font-headline text-lg text-on-surface font-bold">P.202</span>
                      <span className="block text-xs text-on-surface-variant">30m² • Gác lửng</span>
                    </div>
                    <span className="px-2 py-0.5 rounded-full bg-primary/10 text-primary text-xs font-semibold">
                      Đang thuê
                    </span>
                  </div>
                  <div className="flex flex-col gap-0.5">
                    <span className="text-xs text-on-surface font-semibold truncate">Lê Thúy An</span>
                    <span className="text-xs text-on-surface-variant">5.500.000 đ/tháng</span>
                  </div>
                  <div className="flex items-center justify-between pt-2 border-t border-surface-container text-xs">
                    <span className="text-on-surface-variant flex items-center gap-1">
                      <span className="material-symbols-outlined text-sm">group</span> 3/3 người
                    </span>
                    <span className="text-primary font-semibold">Còn 8 tháng</span>
                  </div>
                </div>

                {/* Room 203 (Trống) */}
                <div
                  onClick={() => onOpenRoomDetail('r-203')}
                  className="p-4 rounded-xl bg-surface-container-lowest border border-emerald-500/30 hover:border-emerald-500 shadow-xs hover:shadow-md transition-all cursor-pointer flex flex-col justify-between gap-3"
                >
                  <div className="flex items-start justify-between">
                    <div>
                      <span className="font-headline text-lg text-on-surface font-bold">P.203</span>
                      <span className="block text-xs text-on-surface-variant">25m² • Cửa sổ lớn</span>
                    </div>
                    <span className="px-2 py-0.5 rounded-full bg-emerald-500/10 text-emerald-700 text-xs font-semibold">
                      Trống
                    </span>
                  </div>
                  <div className="flex flex-col gap-0.5">
                    <span className="text-xs text-emerald-700 font-semibold truncate">Sẵn sàng nhận cọc</span>
                    <span className="text-xs text-on-surface-variant">4.600.000 đ/tháng</span>
                  </div>
                  <div className="flex items-center justify-between pt-2 border-t border-surface-container text-xs">
                    <span className="text-emerald-700 flex items-center gap-1 font-semibold">
                      <span className="material-symbols-outlined text-sm">check</span> Đã vệ sinh
                    </span>
                    <span className="text-on-surface-variant">Tối đa 2</span>
                  </div>
                </div>

                {/* Room 204 (ACTIVE SELECTED ROOM IN DESIGN) */}
                <div
                  onClick={() => onOpenRoomDetail('r-204')}
                  className="p-4 rounded-xl bg-surface-container-lowest border-2 border-primary ring-2 ring-primary/20 shadow-md transition-all cursor-pointer flex flex-col justify-between gap-3 relative overflow-hidden"
                >
                  <div className="absolute top-0 right-0 w-7 h-7 bg-primary text-white rounded-bl-xl flex items-center justify-center">
                    <span className="material-symbols-outlined text-sm">check</span>
                  </div>
                  <div className="flex items-start justify-between">
                    <div>
                      <div className="flex items-center gap-1.5">
                        <span className="font-headline text-lg text-primary font-bold">P.204</span>
                        <span className="w-2 h-2 rounded-full bg-primary"></span>
                      </div>
                      <span className="block text-xs text-on-surface-variant">28m² • Ban công riêng</span>
                    </div>
                    <span className="px-2 py-0.5 rounded-full bg-primary/10 text-primary text-xs font-semibold mr-5">
                      Đang thuê
                    </span>
                  </div>
                  <div className="flex flex-col gap-0.5">
                    <span className="text-xs text-on-surface font-bold truncate">Nguyễn Hoàng Long</span>
                    <span className="font-headline text-base text-primary font-bold">
                      5.200.000 đ<span className="text-xs font-normal text-on-surface-variant">/tháng</span>
                    </span>
                  </div>
                  <div className="flex items-center justify-between pt-2 border-t border-surface-container text-xs">
                    <span className="text-on-surface font-medium flex items-center gap-1">
                      <span className="material-symbols-outlined text-sm text-primary">group</span> 2/3 người
                    </span>
                    <span className="text-primary font-bold">Còn 11 tháng</span>
                  </div>
                </div>

                {/* Room 205 */}
                <div
                  onClick={() => onOpenRoomDetail('r-205')}
                  className="p-4 rounded-xl bg-surface-container-lowest border border-surface-container hover:border-primary/50 shadow-xs hover:shadow-md transition-all cursor-pointer flex flex-col justify-between gap-3"
                >
                  <div className="flex items-start justify-between">
                    <div>
                      <span className="font-headline text-lg text-on-surface font-bold">P.205</span>
                      <span className="block text-xs text-on-surface-variant">32m² • 1 Khách 1 Ngủ</span>
                    </div>
                    <span className="px-2 py-0.5 rounded-full bg-primary/10 text-primary text-xs font-semibold">
                      Đang thuê
                    </span>
                  </div>
                  <div className="flex flex-col gap-0.5">
                    <span className="text-xs text-on-surface font-semibold truncate">Đặng Quốc Hưng</span>
                    <span className="text-xs text-on-surface-variant">6.000.000 đ/tháng</span>
                  </div>
                  <div className="flex items-center justify-between pt-2 border-t border-surface-container text-xs">
                    <span className="text-on-surface-variant flex items-center gap-1">
                      <span className="material-symbols-outlined text-sm">group</span> 2/3 người
                    </span>
                    <span className="text-primary font-semibold">Còn 2 tháng</span>
                  </div>
                </div>

                {/* Room 206 (Maintenance) */}
                <div
                  onClick={() => onOpenRoomDetail('r-206')}
                  className="p-4 rounded-xl bg-surface-container-lowest border border-secondary/30 hover:border-secondary shadow-xs hover:shadow-md transition-all cursor-pointer flex flex-col justify-between gap-3"
                >
                  <div className="flex items-start justify-between">
                    <div>
                      <span className="font-headline text-lg text-on-surface font-bold">P.206</span>
                      <span className="block text-xs text-on-surface-variant">28m² • Ban công</span>
                    </div>
                    <span className="px-2 py-0.5 rounded-full bg-secondary/10 text-secondary text-xs font-semibold">
                      Bảo trì
                    </span>
                  </div>
                  <div className="flex flex-col gap-0.5">
                    <span className="text-xs text-secondary font-semibold truncate">Sửa máy lạnh Daikin</span>
                    <span className="text-xs text-on-surface-variant">Xong trước 18:00</span>
                  </div>
                  <div className="flex items-center justify-between pt-2 border-t border-surface-container text-xs">
                    <span className="text-on-surface-variant flex items-center gap-1">
                      <span className="material-symbols-outlined text-sm">engineering</span> Thợ điện lạnh
                    </span>
                    <span className="text-secondary font-semibold">Tạm dừng</span>
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* TẦNG 3 FOLDER (COLLAPSIBLE) */}
        <div className="rounded-2xl bg-surface-container-lowest shadow-xs border border-surface-container overflow-hidden transition-all">
          <div
            onClick={() => toggleFloor(3)}
            className="p-4 flex items-center justify-between cursor-pointer hover:bg-surface-container-low/60 transition-colors"
          >
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-surface-container flex items-center justify-center text-primary font-bold">
                <span className="material-symbols-outlined">apartment</span>
              </div>
              <div className="flex flex-col">
                <div className="flex items-center gap-2">
                  <span className="font-headline text-base text-on-surface font-bold">
                    Tầng 3 - Căn hộ Studio 1PN & Sân thượng
                  </span>
                  <span className="px-2 py-0.5 rounded-full bg-surface-container text-on-surface-variant text-[11px] font-semibold">
                    12 phòng
                  </span>
                </div>
                <span className="text-xs text-on-surface-variant">
                  Không gian yên tĩnh, có khu giặt sấy chung và sân phơi có mái che
                </span>
              </div>
            </div>

            <div className="flex items-center gap-4">
              <div className="hidden sm:flex items-center gap-2 text-xs">
                <span className="px-2.5 py-0.5 rounded-full bg-primary/10 text-primary font-semibold">11 Đang thuê</span>
                <span className="px-2.5 py-0.5 rounded-full bg-amber-500/10 text-amber-700 font-semibold">1 Chờ ký cọc</span>
              </div>
              <span className={`material-symbols-outlined text-on-surface-variant text-xl transition-transform ${
                expandedFloors[3] ? 'rotate-180' : ''
              }`}>
                expand_more
              </span>
            </div>
          </div>

          {expandedFloors[3] && (
            <div className="p-4 bg-surface-container-lowest border-t border-surface-container">
              <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3">
                {rooms.filter((r) => r.floor === 3).map((room) => (
                  <div
                    key={room.id}
                    onClick={() => onOpenRoomDetail(room.id)}
                    className="p-4 rounded-xl border border-surface-container hover:border-primary/50 shadow-xs hover:shadow-md transition-all cursor-pointer flex flex-col justify-between gap-3 bg-surface-container-lowest"
                  >
                    <div className="flex items-start justify-between">
                      <div>
                        <span className="font-headline text-lg font-bold text-on-surface">{room.roomNumber}</span>
                        <span className="block text-xs text-on-surface-variant">{room.area}m² • Studio 1PN</span>
                      </div>
                      <span className="px-2 py-0.5 rounded-full bg-primary/10 text-primary text-xs font-semibold">
                        Đang thuê
                      </span>
                    </div>

                    <div className="flex flex-col gap-0.5">
                      <span className="text-xs text-on-surface font-semibold truncate">
                        {room.currentTenant}
                      </span>
                      <span className="text-xs text-on-surface-variant">
                        {room.price.toLocaleString()} đ/tháng
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
