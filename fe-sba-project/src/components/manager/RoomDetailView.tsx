import React from 'react';
import { Room, Resident, InventoryItem, ViewMode } from '../../types';
import { residentsP204, inventoryP204 } from '../../data/mockData';

interface RoomDetailViewProps {
  room: Room;
  onBackToRooms: () => void;
  onNavigate: (view: ViewMode) => void;
  onOpenMaintenance: (roomNumber: string) => void;
}

export const RoomDetailView: React.FC<RoomDetailViewProps> = ({
  room,
  onBackToRooms,
  onNavigate,
  onOpenMaintenance
}) => {
  const residents: Resident[] = residentsP204;
  const inventory: InventoryItem[] = inventoryP204;

  return (
    <div className="flex flex-col w-full pb-16">
      {/* Sticky Sub-Header & Breadcrumb Bar */}
      <div className="px-6 lg:px-8 py-4 bg-surface-container-lowest/90 backdrop-blur-md sticky top-16 z-30 flex flex-col gap-3 shadow-xs border-b border-surface-container">
        <div className="flex items-center justify-between">
          <nav className="flex items-center gap-1.5 text-on-surface-variant text-xs font-medium">
            <button
              type="button"
              onClick={onBackToRooms}
              className="hover:text-primary transition-colors flex items-center gap-1 cursor-pointer"
            >
              <span className="material-symbols-outlined text-base">domain</span>
              <span>Sơ đồ & Phòng</span>
            </button>
            <span className="material-symbols-outlined text-xs text-outline">chevron_right</span>
            <span className="hover:text-primary cursor-pointer transition-colors">Tầng {room.floor}</span>
            <span className="material-symbols-outlined text-xs text-outline">chevron_right</span>
            <span className="text-primary font-bold">{room.roomNumber}</span>
          </nav>

          <button
            type="button"
            onClick={onBackToRooms}
            className="inline-flex items-center gap-1 px-3 py-1.5 rounded-lg bg-surface-container text-tertiary hover:bg-surface-container-high transition-colors text-xs font-semibold cursor-pointer"
          >
            <span className="material-symbols-outlined text-base">arrow_back</span>
            <span>Quay lại sơ đồ phòng</span>
          </button>
        </div>

        {/* Title and Action Bar */}
        <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4 pt-1">
          <div className="flex flex-col gap-1">
            <div className="flex items-center gap-3 flex-wrap">
              <h1 className="font-headline text-2xl lg:text-3xl font-bold text-on-surface tracking-tight">
                {room.roomNumber} – {room.type} ({room.floorName})
              </h1>
              <span className="inline-flex items-center gap-1.5 px-3 py-0.5 rounded-full bg-primary/10 text-primary text-xs font-bold">
                <span className="w-2 h-2 rounded-full bg-primary animate-pulse"></span>
                Đang thuê
              </span>
              <span className="px-2.5 py-0.5 rounded-md bg-surface-container-high text-on-surface-variant text-[11px] font-mono font-medium">
                Mã định danh: RM-BT-{room.roomNumber.replace('P.', '')}
              </span>
            </div>

            <div className="flex items-center gap-4 text-on-surface-variant text-xs pt-1 flex-wrap">
              <span className="flex items-center gap-1">
                <span className="material-symbols-outlined text-base text-tertiary">straighten</span>
                <span>Diện tích: <strong className="text-on-surface font-bold">{room.area} m²</strong></span>
              </span>
              <span className="w-1 h-1 rounded-full bg-outline-variant"></span>
              <span className="flex items-center gap-1">
                <span className="material-symbols-outlined text-base text-tertiary">group</span>
                <span>Sức chứa: <strong className="text-on-surface font-bold">{room.capacity || '2/3 người'}</strong></span>
              </span>
              <span className="w-1 h-1 rounded-full bg-outline-variant"></span>
              <span className="flex items-center gap-1">
                <span className="material-symbols-outlined text-base text-tertiary">payments</span>
                <span>Giá niêm yết: <strong className="text-primary font-bold">{room.price.toLocaleString()} đ/tháng</strong></span>
              </span>
            </div>
          </div>

          {/* Action Button Group */}
          <div className="flex items-center gap-2 flex-wrap self-start lg:self-center">
            <button
              type="button"
              onClick={() => onOpenMaintenance(room.roomNumber)}
              className="inline-flex items-center gap-1.5 px-3 py-2 rounded-lg bg-surface-container text-on-surface hover:bg-surface-container-high transition-colors text-xs font-semibold cursor-pointer"
            >
              <span className="material-symbols-outlined text-base text-outline">build</span>
              <span>Báo bảo trì / Sửa chữa</span>
            </button>

            <button
              type="button"
              onClick={() => onNavigate('billing')}
              className="inline-flex items-center gap-1.5 px-3 py-2 rounded-lg bg-tertiary text-white hover:bg-tertiary-container transition-colors shadow-xs text-xs font-semibold cursor-pointer"
            >
              <span className="material-symbols-outlined text-base">receipt_long</span>
              <span>Lập hóa đơn nhanh</span>
            </button>

            <button
              type="button"
              onClick={() => window.print()}
              className="inline-flex items-center justify-center p-2 rounded-lg bg-surface-container text-on-surface hover:bg-surface-container-high transition-colors cursor-pointer"
              title="In hồ sơ phòng"
            >
              <span className="material-symbols-outlined text-lg">print</span>
            </button>

            <button
              type="button"
              onClick={() => onNavigate('create-contract')}
              className="inline-flex items-center gap-1.5 px-4 py-2 rounded-lg bg-secondary-fixed text-on-secondary-fixed font-bold text-xs hover:bg-secondary-fixed-dim transition-colors shadow-sm cursor-pointer"
            >
              <span className="material-symbols-outlined text-base font-bold">add_circle</span>
              <span>+ Gia hạn / Tạo phụ lục HĐ</span>
            </button>
          </div>
        </div>
      </div>

      {/* Main Content Grid (65% / 35%) */}
      <div className="p-6 lg:p-8 grid grid-cols-1 xl:grid-cols-12 gap-8 max-w-[1600px] mx-auto w-full">
        {/* LEFT COLUMN: Operations, Living Space, Resident Profiles & Inventory (8 cols) */}
        <div className="xl:col-span-8 flex flex-col gap-6">
          {/* Room Visuals & Quick Feature Snapshot */}
          <section className="bg-surface-container-lowest rounded-2xl shadow-xs border border-surface-container overflow-hidden flex flex-col">
            <div className="relative h-72 md:h-80 w-full overflow-hidden">
              <img
                src={room.images?.[0] || 'https://lh3.googleusercontent.com/aida-public/AB6AXuBX48w5Qt9WTnrvfCdiinYN3oge4Yn7L_PBd3kwT-yUbwYRPVf_qT7mnpzgMDriQm9plqZUK-NfPL3ktWg1yy48LgaemQfzEa0R6wkAZEWQztEr-E_7RDzkDa3kex1TdLyVmWn1U5z-_-uD28G5SnBgWCgrZWyPePVvWq4Lkm9P2D3rAn15U7RJD7-80VmB-xAZncrR69LgTX0N6xhj2ncoN6hHILqBik1wMjcrEw2-zMi09y1s3Dq2'}
                alt="Room visual"
                className="w-full h-full object-cover transform hover:scale-105 transition-transform duration-700 ease-out"
              />
              <div className="absolute inset-0 bg-gradient-to-t from-black/75 via-black/25 to-transparent"></div>

              <div className="absolute top-4 left-4 flex gap-2 flex-wrap">
                <span className="px-3 py-1 rounded-md bg-white/90 backdrop-blur-md text-on-surface text-xs font-bold flex items-center gap-1 shadow-sm">
                  <span className="material-symbols-outlined text-sm text-primary">balcony</span>
                  Ban công Đông Nam thoáng mát
                </span>
                <span className="px-3 py-1 rounded-md bg-white/90 backdrop-blur-md text-on-surface text-xs font-bold flex items-center gap-1 shadow-sm">
                  <span className="material-symbols-outlined text-sm text-secondary">wifi</span>
                  Bộ Mesh Wifi P204 riêng biệt
                </span>
              </div>

              <div className="absolute bottom-4 left-4 right-4 flex items-end justify-between">
                <div>
                  <p className="text-[11px] uppercase tracking-wider text-surface-variant font-medium">Bố trí công năng</p>
                  <h3 className="font-headline text-lg lg:text-xl font-bold text-white drop-shadow-sm">
                    Không gian sống Khép kín · Full Nội thất Décor
                  </h3>
                </div>
                <button
                  type="button"
                  onClick={() => alert('Mở thư viện 8 ảnh bàn giao thiết bị phòng P.204')}
                  className="px-3 py-1.5 rounded-lg bg-white/90 backdrop-blur-md text-on-surface text-xs font-bold hover:bg-white transition-colors flex items-center gap-1.5 cursor-pointer shadow-sm"
                >
                  <span className="material-symbols-outlined text-base">photo_library</span>
                  <span>Xem 8 ảnh bàn giao</span>
                </button>
              </div>
            </div>

            {/* Room Specs Ribbon */}
            <div className="p-6 grid grid-cols-2 sm:grid-cols-3 md:grid-cols-6 gap-4 bg-surface-container-low/60 border-t border-surface-container">
              <div className="flex flex-col gap-0.5">
                <span className="text-[11px] text-on-surface-variant flex items-center gap-1 font-medium">
                  <span className="material-symbols-outlined text-sm text-primary">ac_unit</span> Điều hòa
                </span>
                <span className="text-xs font-bold text-on-surface">Daikin Inverter 1.5HP</span>
              </div>

              <div className="flex flex-col gap-0.5">
                <span className="text-[11px] text-on-surface-variant flex items-center gap-1 font-medium">
                  <span className="material-symbols-outlined text-sm text-primary">kitchen</span> Tủ lạnh
                </span>
                <span className="text-xs font-bold text-on-surface">Aqua 140L 2 ngăn</span>
              </div>

              <div className="flex flex-col gap-0.5">
                <span className="text-[11px] text-on-surface-variant flex items-center gap-1 font-medium">
                  <span className="material-symbols-outlined text-sm text-primary">water_heater</span> Bình nóng lạnh
                </span>
                <span className="text-xs font-bold text-on-surface">Ariston 20L Nano</span>
              </div>

              <div className="flex flex-col gap-0.5">
                <span className="text-[11px] text-on-surface-variant flex items-center gap-1 font-medium">
                  <span className="material-symbols-outlined text-sm text-primary">fingerprint</span> Khóa cửa
                </span>
                <span className="text-xs font-bold text-on-surface flex items-center gap-1">
                  Kaadas K9 <span className="text-[10px] font-bold text-primary bg-primary/10 px-1 rounded">Pin 85%</span>
                </span>
              </div>

              <div className="flex flex-col gap-0.5">
                <span className="text-[11px] text-on-surface-variant flex items-center gap-1 font-medium">
                  <span className="material-symbols-outlined text-sm text-primary">cooking</span> Bếp nấu
                </span>
                <span className="text-xs font-bold text-on-surface">Bếp từ đôi âm kính</span>
              </div>

              <div className="flex flex-col gap-0.5">
                <span className="text-[11px] text-on-surface-variant flex items-center gap-1 font-medium">
                  <span className="material-symbols-outlined text-sm text-primary">bed</span> Giường & Tủ
                </span>
                <span className="text-xs font-bold text-on-surface">Gỗ sồi tự nhiên 1m6</span>
              </div>
            </div>
          </section>

          {/* Resident Profiles: Representative & Roommate (SRS FR-MGR-05, BR-03) */}
          <section className="bg-surface-container-lowest rounded-2xl shadow-xs border border-surface-container p-6 flex flex-col gap-6">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
              <div className="flex items-center gap-3">
                <span className="w-10 h-10 rounded-xl bg-primary/10 text-primary flex items-center justify-center">
                  <span className="material-symbols-outlined text-2xl">badge</span>
                </span>
                <div>
                  <h2 className="font-headline text-lg font-bold text-on-surface">
                    Hồ sơ Cư dân & Khai báo Tạm trú
                  </h2>
                  <p className="text-xs text-on-surface-variant">
                    Quy chuẩn dữ liệu công dân số liên kết CCCD gắn chip (SRS FR-MGR-05 & BR-03)
                  </p>
                </div>
              </div>

              <div className="flex items-center gap-2">
                <button
                  type="button"
                  onClick={() => alert('Đang xuất mẫu khai thay đổi thông tin cư trú CT01...')}
                  className="px-3 py-1.5 rounded-lg bg-surface-container-low text-tertiary hover:bg-surface-container transition-colors text-xs font-semibold flex items-center gap-1 cursor-pointer"
                >
                  <span className="material-symbols-outlined text-base">download_for_offline</span>
                  <span>Xuất file tạm trú CT01</span>
                </button>

                <button
                  type="button"
                  onClick={() => alert('Thao tác: Chọn thành viên để chuyển quyền đại diện hợp đồng')}
                  className="px-3 py-1.5 rounded-lg bg-surface-container-low text-on-surface-variant hover:bg-surface-container transition-colors text-xs font-semibold flex items-center gap-1 cursor-pointer"
                >
                  <span className="material-symbols-outlined text-base">swap_horiz</span>
                  <span>Chuyển quyền đại diện</span>
                </button>
              </div>
            </div>

            {/* Resident Cards Grid */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {residents.map((res) => (
                <div
                  key={res.id}
                  className="p-5 rounded-2xl bg-surface-container-low flex flex-col justify-between gap-4 border border-surface-container"
                >
                  <div className="flex items-start gap-4">
                    <div className="relative shrink-0">
                      <img
                        src={res.avatar}
                        alt={res.name}
                        className="w-14 h-14 rounded-full object-cover shadow-sm ring-2 ring-primary/20"
                      />
                      <span
                        className="absolute -bottom-1 -right-1 p-0.5 rounded-full bg-primary text-white shadow-xs"
                        title={res.roleLabel}
                      >
                        <span className="material-symbols-outlined text-[13px] block">
                          {res.role === 'primary' ? 'verified' : 'person'}
                        </span>
                      </span>
                    </div>

                    <div className="flex flex-col min-w-0 flex-1">
                      <div className="flex items-center justify-between gap-1">
                        <span className="font-headline text-base font-bold text-on-surface truncate">
                          {res.name}
                        </span>
                        <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-wide ${
                          res.role === 'primary' ? 'bg-primary text-white' : 'bg-surface-container text-on-surface-variant'
                        }`}>
                          {res.roleLabel}
                        </span>
                      </div>
                      <span className="text-xs text-on-surface-variant">
                        {res.occupation} · {res.workplace}
                      </span>
                      <div className="mt-2 flex flex-wrap items-center gap-x-4 gap-y-1 text-xs text-on-surface">
                        <span className="flex items-center gap-1">
                          <span className="material-symbols-outlined text-sm text-outline">call</span>
                          <strong className="font-semibold">{res.phone}</strong>
                        </span>
                        <span className="flex items-center gap-1">
                          <span className="material-symbols-outlined text-sm text-outline">home_pin</span>
                          <span>{res.hometown}</span>
                        </span>
                      </div>
                    </div>
                  </div>

                  {/* Citizen details */}
                  <div className="p-3 rounded-xl bg-surface-container-lowest flex flex-col gap-1.5 text-xs">
                    <div className="flex items-center justify-between">
                      <span className="text-on-surface-variant flex items-center gap-1">
                        <span className="material-symbols-outlined text-sm text-primary">credit_card</span> CCCD gắn chip:
                      </span>
                      <span className="font-semibold text-on-surface">
                        {res.cccd} <span className="text-primary text-[11px]">({res.cccdStatus})</span>
                      </span>
                    </div>

                    <div className="flex items-center justify-between">
                      <span className="text-on-surface-variant flex items-center gap-1">
                        <span className="material-symbols-outlined text-sm text-primary">two_wheeler</span> Phương tiện:
                      </span>
                      <span className="font-semibold text-on-surface">{res.vehicle}</span>
                    </div>

                    <div className="flex items-center justify-between">
                      <span className="text-on-surface-variant flex items-center gap-1">
                        <span className="material-symbols-outlined text-sm text-primary">local_parking</span> Slot giữ xe:
                      </span>
                      <span className="font-bold text-primary">{res.vehicleSlot}</span>
                    </div>
                  </div>

                  <div className="flex items-center justify-between pt-1 text-xs">
                    <span className="inline-flex items-center gap-1 text-primary font-semibold">
                      <span className="material-symbols-outlined text-sm">task_alt</span>
                      {res.policeApproval}
                    </span>
                    <button
                      type="button"
                      onClick={() => alert(`Xem ảnh căn cước công dân 2 mặt của ${res.name}`)}
                      className="text-tertiary hover:text-primary font-semibold inline-flex items-center gap-1 cursor-pointer"
                    >
                      <span>Xem ảnh CCCD 2 mặt</span>
                      <span className="material-symbols-outlined text-sm">visibility</span>
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </section>

          {/* Asset Inventory & Handover Minutes */}
          <section className="bg-surface-container-lowest rounded-2xl shadow-xs border border-surface-container p-6 flex flex-col gap-4">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
              <div className="flex items-center gap-3">
                <span className="w-10 h-10 rounded-xl bg-tertiary/10 text-tertiary flex items-center justify-center">
                  <span className="material-symbols-outlined text-2xl">inventory_2</span>
                </span>
                <div>
                  <h2 className="font-headline text-lg font-bold text-on-surface">
                    Danh mục CSVC & Biên bản Bàn giao phòng
                  </h2>
                  <p className="text-xs text-on-surface-variant">
                    Kiểm kê tình trạng hiện trạng trang thiết bị điện máy và nội thất rời
                  </p>
                </div>
              </div>

              {/* Handover doc */}
              <div className="flex items-center gap-2 p-2 px-3 rounded-lg bg-surface-container-high text-on-surface text-xs font-medium">
                <span className="material-symbols-outlined text-base text-secondary">picture_as_pdf</span>
                <span className="font-bold">BBBG-P204.pdf</span>
                <span className="text-primary font-semibold">(Đã ký số 2 bên)</span>
                <button
                  type="button"
                  onClick={() => alert('Đang tải file BBBG-P204.pdf...')}
                  className="text-primary hover:text-on-surface-variant ml-1 cursor-pointer"
                  title="Tải xuống"
                >
                  <span className="material-symbols-outlined text-base">download</span>
                </button>
              </div>
            </div>

            {/* Inventory List Table */}
            <div className="overflow-x-auto rounded-xl bg-surface-container-low/40 border border-surface-container">
              <table className="w-full text-left text-xs border-collapse">
                <thead>
                  <tr className="bg-surface-container text-tertiary uppercase tracking-wider text-[11px] font-bold">
                    <th className="py-3 px-4">Trang thiết bị</th>
                    <th className="py-3 px-4">Model / Seri</th>
                    <th className="py-3 px-4">Số lượng</th>
                    <th className="py-3 px-4">Hiện trạng kỹ thuật</th>
                    <th className="py-3 px-4">Bảo dưỡng gần nhất</th>
                    <th className="py-3 px-4 text-right">Tác vụ</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-surface-container font-medium text-on-surface">
                  {inventory.map((item) => (
                    <tr key={item.id} className="hover:bg-surface-container-low transition-colors">
                      <td className="py-3 px-4 font-bold text-on-surface flex items-center gap-2">
                        <span className="material-symbols-outlined text-base text-primary">
                          {item.name.includes('Máy lạnh') ? 'mode_fan' : item.name.includes('Tủ lạnh') ? 'kitchen' : item.name.includes('nước nóng') ? 'water_heater' : item.name.includes('Khóa') ? 'lock' : 'chair'}
                        </span>
                        {item.name}
                      </td>
                      <td className="py-3 px-4 text-on-surface-variant">{item.model}</td>
                      <td className="py-3 px-4">{item.quantity}</td>
                      <td className="py-3 px-4">
                        <span className="inline-flex items-center gap-1 text-primary font-semibold">
                          <span className="w-1.5 h-1.5 rounded-full bg-primary"></span>
                          {item.condition}
                        </span>
                      </td>
                      <td className="py-3 px-4 text-on-surface-variant">{item.lastMaintenance}</td>
                      <td className="py-3 px-4 text-right">
                        <button
                          type="button"
                          onClick={() => alert(`Xem chi tiết tài sản ${item.code} (${item.name})`)}
                          className="text-tertiary hover:text-primary font-bold cursor-pointer"
                        >
                          Chi tiết
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <div className="flex items-center justify-between text-on-surface-variant text-xs pt-1">
              <span className="flex items-center gap-1">
                <span className="material-symbols-outlined text-sm text-primary">verified_user</span>
                Thiết bị có dán mã QR tài sản TroPro Asset tag.
              </span>
              <button
                type="button"
                onClick={() => alert('Thao tác: Ghi nhận thêm CSVC phát sinh vào biên bản bàn giao')}
                className="text-primary hover:underline text-xs font-bold flex items-center gap-1 cursor-pointer"
              >
                <span className="material-symbols-outlined text-sm">add_box</span>
                <span>Ghi nhận thêm CSVC phát sinh</span>
              </button>
            </div>
          </section>
        </div>

        {/* RIGHT COLUMN: Contract, Financials, Meter Readings & Resident Portal (4 cols) */}
        <div className="xl:col-span-4 flex flex-col gap-6">
          {/* 1. Electronic Contract Summary */}
          <section className="bg-surface-container-lowest rounded-2xl shadow-xs border border-surface-container p-6 flex flex-col gap-4 relative overflow-hidden">
            <div className="flex items-start justify-between">
              <div className="flex items-center gap-3">
                <span className="w-10 h-10 rounded-xl bg-primary/10 text-primary flex items-center justify-center">
                  <span className="material-symbols-outlined text-2xl">description</span>
                </span>
                <div>
                  <h2 className="font-headline text-base font-bold text-on-surface">Hợp đồng điện tử</h2>
                  <span className="text-xs font-bold text-primary font-mono">HĐ-DBL-204-2024</span>
                </div>
              </div>
              <span className="px-2.5 py-0.5 rounded-full bg-primary/10 text-primary text-xs font-bold">
                Đang hiệu lực
              </span>
            </div>

            <div className="grid grid-cols-2 gap-3 p-4 rounded-xl bg-surface-container-low">
              <div className="flex flex-col">
                <span className="text-[11px] text-on-surface-variant font-medium">Thời hạn thuê:</span>
                <span className="text-xs font-bold text-on-surface mt-0.5">24 tháng</span>
                <span className="text-[11px] text-primary mt-0.5 font-semibold">Còn 11 tháng hiệu lực</span>
              </div>
              <div className="flex flex-col">
                <span className="text-[11px] text-on-surface-variant font-medium">Tiền cọc bảo đảm:</span>
                <span className="text-xs font-bold text-secondary mt-0.5">10.400.000 đ</span>
                <span className="text-[11px] text-on-surface-variant mt-0.5">Đã thu đủ (2 tháng)</span>
              </div>
              <div className="col-span-2 pt-2 mt-1 border-t border-surface-container flex items-center justify-between text-xs">
                <div className="flex flex-col">
                  <span className="text-[11px] text-on-surface-variant font-medium">Khoảng thời gian:</span>
                  <span className="text-xs text-on-surface font-semibold">15/10/2023 → 15/10/2025</span>
                </div>
                <div className="flex flex-col items-end">
                  <span className="text-[11px] text-on-surface-variant font-medium">Kỳ thu tiền:</span>
                  <span className="text-xs font-bold text-primary">Ngày 05 hàng tháng</span>
                </div>
              </div>
            </div>

            <div className="flex items-center gap-2 text-on-surface-variant text-xs">
              <span className="material-symbols-outlined text-base text-primary">lock_clock</span>
              <span>Ký số SmartOTP ngày 14/10/2023 lúc 16:42</span>
            </div>

            <div className="flex flex-col gap-2 pt-1">
              <button
                type="button"
                onClick={() => onNavigate('contracts')}
                className="w-full py-2.5 px-4 rounded-lg bg-surface-container text-on-surface hover:bg-surface-container-high transition-colors text-xs font-bold flex items-center justify-center gap-2 cursor-pointer"
              >
                <span className="material-symbols-outlined text-base text-tertiary">open_in_new</span>
                <span>Xem chi tiết hợp đồng</span>
              </button>

              <button
                type="button"
                onClick={() => alert('Đang tải file PDF HĐ-DBL-204-2024 có dấu mộc điện tử...')}
                className="w-full py-2 px-4 rounded-lg bg-surface-container-low text-on-surface-variant hover:text-on-surface hover:bg-surface-container transition-colors text-xs font-semibold flex items-center justify-center gap-2 cursor-pointer"
              >
                <span className="material-symbols-outlined text-base text-secondary">download</span>
                <span>Tải PDF có dấu mộc điện tử</span>
              </button>
            </div>
          </section>

          {/* 2. Current Month Billing & Meter Readings */}
          <section className="bg-surface-container-lowest rounded-2xl shadow-xs border border-surface-container p-6 flex flex-col gap-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <span className="w-10 h-10 rounded-xl bg-secondary-fixed/50 text-secondary flex items-center justify-center">
                  <span className="material-symbols-outlined text-2xl">receipt</span>
                </span>
                <div>
                  <h2 className="font-headline text-base font-bold text-on-surface">Hóa đơn Kỳ 10/2024</h2>
                  <span className="text-xs text-on-surface-variant">Hạn đóng: 10/10/2024</span>
                </div>
              </div>
              <span className="px-2.5 py-1 rounded-full bg-primary/10 text-primary text-xs font-bold flex items-center gap-1">
                <span className="material-symbols-outlined text-sm">check_circle</span>
                Đã thanh toán
              </span>
            </div>

            {/* Meter Breakdown Boxes */}
            <div className="flex flex-col gap-2.5 text-xs">
              {/* Electricity */}
              <div className="p-3 rounded-xl bg-surface-container-low flex flex-col gap-1">
                <div className="flex items-center justify-between font-bold">
                  <span className="text-on-surface flex items-center gap-1">
                    <span className="material-symbols-outlined text-base text-secondary">bolt</span>
                    Điện sinh hoạt (3.800 đ/kWh)
                  </span>
                  <span>627.000 đ</span>
                </div>
                <div className="flex items-center justify-between text-on-surface-variant text-[11px]">
                  <span>Chỉ số: 1.420 kWh → 1.585 kWh</span>
                  <span className="text-primary font-bold">Tiêu thụ: 165 kWh</span>
                </div>
              </div>

              {/* Water */}
              <div className="p-3 rounded-xl bg-surface-container-low flex flex-col gap-1">
                <div className="flex items-center justify-between font-bold">
                  <span className="text-on-surface flex items-center gap-1">
                    <span className="material-symbols-outlined text-base text-primary">water_drop</span>
                    Nước máy thủy cục (22.000 đ/m³)
                  </span>
                  <span>176.000 đ</span>
                </div>
                <div className="flex items-center justify-between text-on-surface-variant text-[11px]">
                  <span>Chỉ số: 112 m³ → 120 m³</span>
                  <span className="text-primary font-bold">Tiêu thụ: 8 m³</span>
                </div>
              </div>

              {/* Fixed Services */}
              <div className="p-3 rounded-xl bg-surface-container-low flex items-center justify-between font-bold">
                <span className="text-on-surface flex items-center gap-1">
                  <span className="material-symbols-outlined text-base text-tertiary">room_service</span>
                  Wifi + Vệ sinh + 2 Xe máy
                </span>
                <span>250.000 đ</span>
              </div>

              {/* Base Rent */}
              <div className="p-3 rounded-xl bg-surface-container-low flex items-center justify-between font-bold">
                <span className="text-on-surface flex items-center gap-1">
                  <span className="material-symbols-outlined text-base text-tertiary">apartment</span>
                  Tiền phòng tháng 10/2024
                </span>
                <span>5.200.000 đ</span>
              </div>
            </div>

            {/* Total Sum Calculation Ribbon */}
            <div className="p-4 rounded-xl bg-surface-container flex items-center justify-between">
              <div className="flex flex-col">
                <span className="text-[11px] text-on-surface-variant uppercase tracking-wider font-bold">
                  Tổng cộng thực thu:
                </span>
                <span className="text-[11px] text-primary font-semibold">Giao dịch qua VietQR động Techcombank</span>
              </div>
              <span className="font-headline text-xl font-bold text-primary tracking-tight">
                6.253.000 đ
              </span>
            </div>

            <button
              type="button"
              onClick={() => onNavigate('billing')}
              className="w-full py-2 px-4 rounded-lg bg-surface-container text-on-surface hover:bg-surface-container-high transition-colors text-xs font-semibold flex items-center justify-center gap-2 cursor-pointer"
            >
              <span className="material-symbols-outlined text-base">history</span>
              <span>Xem lịch sử hóa đơn 6 tháng trước</span>
            </button>
          </section>

          {/* 3. Resident Portal & Fast Community Channel */}
          <section className="bg-surface-container-lowest rounded-2xl shadow-xs border border-surface-container p-6 flex flex-col gap-4">
            <div className="flex items-center gap-3">
              <span className="w-10 h-10 rounded-xl bg-primary/10 text-primary flex items-center justify-center">
                <span className="material-symbols-outlined text-2xl">forum</span>
              </span>
              <div>
                <h2 className="font-headline text-base font-bold text-on-surface">Kênh tương tác & Nhóm Zalo</h2>
                <p className="text-xs text-on-surface-variant">Cơ sở Bình Thạnh - Đinh Bộ Lĩnh</p>
              </div>
            </div>

            <div className="p-3 rounded-xl bg-surface-container-low flex items-center justify-between">
              <div className="flex items-center gap-2.5">
                <span className="w-8 h-8 rounded-full bg-primary text-white flex items-center justify-center font-bold text-xs">
                  Z
                </span>
                <div className="flex flex-col">
                  <span className="text-xs font-bold text-on-surface">Nhóm Cư dân Tầng 2</span>
                  <span className="text-[11px] text-on-surface-variant">14 thành viên · zalo.me/g/tropro-bt2</span>
                </div>
              </div>
              <span className="material-symbols-outlined text-on-surface-variant text-base">arrow_forward</span>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <a
                href="https://zalo.me"
                target="_blank"
                rel="noopener noreferrer"
                className="py-2.5 px-3 rounded-xl bg-primary text-white text-xs font-bold text-center hover:bg-primary-container transition-colors shadow-xs flex items-center justify-center gap-1.5 cursor-pointer"
              >
                <span className="material-symbols-outlined text-base">chat</span>
                <span>Chat Zalo</span>
              </a>
              <a
                href="tel:0918421905"
                className="py-2.5 px-3 rounded-xl bg-surface-container text-on-surface text-xs font-bold text-center hover:bg-surface-container-high transition-colors flex items-center justify-center gap-1.5 cursor-pointer"
              >
                <span className="material-symbols-outlined text-base">phone_in_talk</span>
                <span>Gọi trực tiếp</span>
              </a>
            </div>
          </section>
        </div>
      </div>
    </div>
  );
};
