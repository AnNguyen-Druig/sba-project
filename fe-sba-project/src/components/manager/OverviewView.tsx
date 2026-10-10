import React from 'react';
import { Room, ViewMode } from '../../types';
import { facilityInfo } from '../../data/mockData';

interface OverviewViewProps {
  rooms: Room[];
  onSelectRoom: (roomId: string) => void;
  onNavigate: (view: ViewMode) => void;
  onOpenAddRoom: () => void;
}

export const OverviewView: React.FC<OverviewViewProps> = ({
  rooms,
  onSelectRoom,
  onNavigate,
  onOpenAddRoom
}) => {
  // Counts by status
  const rentedCount = rooms.filter((r) => r.status === 'rented' || r.status === 'overdue').length;
  const availableCount = rooms.filter((r) => r.status === 'available').length;
  const pendingCount = rooms.filter((r) => r.status === 'pending_contract').length;
  const maintenanceCount = rooms.filter((r) => r.status === 'maintenance').length;

  const floor4Rooms = rooms.filter((r) => r.floor === 4);
  const floor3Rooms = rooms.filter((r) => r.floor === 3);
  const floor2Rooms = rooms.filter((r) => r.floor === 2);
  const floor1Rooms = rooms.filter((r) => r.floor === 1);

  return (
    <div className="flex flex-col w-full p-6 lg:p-8 gap-6 max-w-[1600px] mx-auto">
      {/* Top Action & Location Bar */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div className="flex flex-col gap-1">
          <div className="flex items-center gap-2">
            <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold bg-primary/10 text-primary">
              <span className="w-1.5 h-1.5 rounded-full bg-primary mr-1.5 animate-pulse"></span>
              Đang vận hành thời gian thực
            </span>
            <span className="text-xs text-outline">|</span>
            <span className="text-xs text-on-surface-variant font-medium">Cập nhật 2 phút trước</span>
          </div>
          <h1 className="font-headline text-2xl lg:text-3xl font-bold text-on-surface tracking-tight mt-0.5">
            Tổng quan hoạt động — <span className="text-primary font-bold">{facilityInfo.address.split(',')[0]}, Bình Thạnh</span>
          </h1>
        </div>

        {/* Quick Actions & Month Filter */}
        <div className="flex flex-wrap items-center gap-2.5">
          <div className="relative inline-flex items-center bg-surface-container-lowest rounded-xl shadow-xs px-3.5 py-2 cursor-pointer hover:bg-surface-container-low transition-colors border border-surface-container">
            <span className="material-symbols-outlined text-primary text-base mr-2">calendar_today</span>
            <span className="text-xs text-on-surface font-bold">Tháng 10/2024</span>
            <span className="material-symbols-outlined text-outline text-sm ml-1.5">keyboard_arrow_down</span>
          </div>

          <button
            type="button"
            onClick={() => onNavigate('create-contract')}
            className="inline-flex items-center gap-1.5 px-4 py-2.5 rounded-xl bg-primary text-white text-xs font-bold shadow-sm hover:bg-primary-container transition-all cursor-pointer active:scale-95"
          >
            <span className="material-symbols-outlined text-base">edit_document</span>
            <span>+ Lập hợp đồng</span>
          </button>

          <button
            type="button"
            onClick={onOpenAddRoom}
            className="inline-flex items-center gap-1.5 px-4 py-2.5 rounded-xl bg-secondary-container text-white text-xs font-bold shadow-sm hover:opacity-95 transition-all cursor-pointer active:scale-95"
          >
            <span className="material-symbols-outlined text-base">add_circle</span>
            <span>+ Thêm phòng mới</span>
          </button>
        </div>
      </div>

      {/* Sơ đồ phòng theo Tầng & Trạng thái trực quan */}
      <div className="bg-surface-container-lowest rounded-2xl shadow-xs border border-surface-container p-6 flex flex-col gap-6">
        {/* Header & Legends */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between pb-2 gap-4 border-b border-surface-container/60">
          <div className="flex items-center gap-3">
            <div className="p-2.5 rounded-xl bg-primary/10 text-primary">
              <span className="material-symbols-outlined text-2xl">grid_view</span>
            </div>
            <div>
              <h2 className="font-headline text-lg font-bold text-on-surface">
                Sơ đồ phòng theo tầng & Trạng thái trực quan
              </h2>
              <p className="text-xs text-on-surface-variant font-medium">
                {facilityInfo.scale}
              </p>
            </div>
          </div>

          {/* Legend Chips */}
          <div className="flex flex-wrap items-center gap-2">
            <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-primary/10 text-primary">
              <span className="w-2 h-2 rounded-full bg-primary"></span>
              Đang thuê ({rentedCount})
            </span>
            <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-tertiary-fixed text-on-tertiary-fixed-variant">
              <span className="w-2 h-2 rounded-full bg-tertiary"></span>
              Trống sẵn sàng ({availableCount})
            </span>
            <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-secondary-fixed text-on-secondary-fixed-variant">
              <span className="w-2 h-2 rounded-full bg-secondary-container"></span>
              Chờ ký hợp đồng ({pendingCount})
            </span>
            <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold bg-error-container text-on-error-container">
              <span className="w-2 h-2 rounded-full bg-error"></span>
              Bảo trì / Sửa ({maintenanceCount})
            </span>
          </div>
        </div>

        {/* Floors Loop */}
        <div className="flex flex-col gap-6">
          {/* TẦNG 4 */}
          <div className="p-4 rounded-xl bg-surface-container-low/60 border border-surface-container flex flex-col gap-3">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <span className="px-2.5 py-0.5 rounded-lg bg-surface-container text-xs text-primary font-bold">
                  TẦNG 4
                </span>
                <span className="text-xs text-on-surface-variant font-medium">
                  Khu căn hộ Studio gác lửng ban công
                </span>
              </div>
              <span className="text-xs text-on-surface-variant font-medium">
                8/9 Phòng đã thuê
              </span>
            </div>

            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-5 lg:grid-cols-9 gap-2.5">
              {floor4Rooms.map((room) => (
                <div
                  key={room.id}
                  onClick={() => onSelectRoom(room.id)}
                  className={`p-2.5 rounded-xl border transition-all cursor-pointer group flex flex-col justify-between ${
                    room.status === 'available'
                      ? 'bg-gradient-to-b from-tertiary-fixed/30 to-surface-container-lowest border-tertiary/30 hover:border-tertiary shadow-sm'
                      : room.status === 'pending_contract'
                      ? 'bg-gradient-to-b from-secondary-fixed/40 to-surface-container-lowest border-secondary/30 hover:border-secondary shadow-sm'
                      : room.status === 'overdue'
                      ? 'bg-surface-container-lowest border-secondary-container/40 hover:border-secondary-container shadow-sm'
                      : 'bg-surface-container-lowest border-outline-variant/30 hover:border-primary/50 shadow-xs hover:shadow-md'
                  }`}
                >
                  <div className="flex items-start justify-between">
                    <span className={`font-headline text-sm font-bold ${
                      room.status === 'available' ? 'text-tertiary' : 'text-on-surface'
                    }`}>
                      {room.roomNumber}
                    </span>
                    {room.status === 'available' ? (
                      <span className="material-symbols-outlined text-sm text-tertiary">bolt</span>
                    ) : room.status === 'pending_contract' ? (
                      <span className="w-2 h-2 rounded-full bg-secondary-container"></span>
                    ) : room.status === 'overdue' ? (
                      <span className="material-symbols-outlined text-secondary-container text-sm">error</span>
                    ) : (
                      <span className="w-2 h-2 rounded-full bg-primary"></span>
                    )}
                  </div>

                  <div className="my-2 truncate">
                    {room.status === 'available' ? (
                      <>
                        <span className="text-[11px] font-bold text-tertiary uppercase block">TRỐNG</span>
                        <span className="text-xs text-on-surface-variant font-medium">{(room.price / 1000000).toFixed(1)} tr/th</span>
                      </>
                    ) : room.status === 'pending_contract' ? (
                      <>
                        <span className="text-[11px] font-bold text-secondary truncate block">Chờ ký OTP</span>
                        <span className="text-xs text-on-surface-variant font-medium">4.9 tr/th</span>
                      </>
                    ) : room.status === 'overdue' ? (
                      <>
                        <span className="text-[11px] font-semibold text-on-surface truncate block">{room.currentTenant}</span>
                        <span className="text-xs text-secondary-container font-bold">Nợ 5.1tr</span>
                      </>
                    ) : (
                      <>
                        <span className="text-[11px] font-semibold text-on-surface truncate block">{room.currentTenant}</span>
                        <span className="text-xs text-on-surface-variant font-medium">{(room.price / 1000000).toFixed(1)} tr/th</span>
                      </>
                    )}
                  </div>

                  <div className="pt-1 flex items-center justify-between text-[11px]">
                    {room.status === 'available' ? (
                      <span className="text-tertiary font-bold flex items-center justify-between w-full">
                        <span>Đề xuất ngay</span>
                        <span className="material-symbols-outlined text-xs group-hover:translate-x-0.5 transition-transform">arrow_forward</span>
                      </span>
                    ) : room.status === 'pending_contract' ? (
                      <span className="text-secondary font-bold flex items-center justify-between w-full">
                        <span>Hợp đồng</span>
                        <span className="material-symbols-outlined text-xs">pending_actions</span>
                      </span>
                    ) : room.status === 'overdue' ? (
                      <span className="text-secondary font-bold flex items-center justify-between w-full group-hover:underline">
                        <span>Nhắc nợ</span>
                        <span className="material-symbols-outlined text-xs">notifications_active</span>
                      </span>
                    ) : (
                      <span className="text-outline group-hover:text-primary flex items-center justify-between w-full">
                        <span>HĐ: {room.contractEnd}</span>
                        <span className="material-symbols-outlined text-xs">visibility</span>
                      </span>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* TẦNG 3 */}
          <div className="p-4 rounded-xl bg-surface-container-low/60 border border-surface-container flex flex-col gap-3">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <span className="px-2.5 py-0.5 rounded-lg bg-surface-container text-xs text-primary font-bold">
                  TẦNG 3
                </span>
                <span className="text-xs text-on-surface-variant font-medium">
                  Khu căn hộ Studio 1 phòng ngủ • 9 phòng
                </span>
              </div>
              <span className="text-xs text-on-surface-variant font-medium">
                9/9 Phòng đầy đủ
              </span>
            </div>

            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-5 lg:grid-cols-9 gap-2.5">
              {floor3Rooms.map((room) => (
                <div
                  key={room.id}
                  onClick={() => onSelectRoom(room.id)}
                  className={`p-2.5 rounded-xl border transition-all cursor-pointer group flex flex-col justify-between ${
                    room.contractExpiryDays
                      ? 'bg-gradient-to-b from-secondary-fixed/30 to-surface-container-lowest border-secondary/30 hover:border-secondary shadow-sm'
                      : 'bg-surface-container-lowest border-outline-variant/30 hover:border-primary/50 shadow-xs hover:shadow-md'
                  }`}
                >
                  <div className="flex items-start justify-between">
                    <span className="font-headline text-sm font-bold text-on-surface">
                      {room.roomNumber}
                    </span>
                    {room.contractExpiryDays ? (
                      <span className="material-symbols-outlined text-secondary text-sm">hourglass_top</span>
                    ) : (
                      <span className="w-2 h-2 rounded-full bg-primary"></span>
                    )}
                  </div>

                  <div className="my-2 truncate">
                    <span className="text-[11px] font-semibold text-on-surface truncate block">
                      {room.currentTenant}
                    </span>
                    {room.contractExpiryDays ? (
                      <span className="text-xs text-secondary font-medium">Hết hạn sau 5 ngày</span>
                    ) : (
                      <span className="text-xs text-on-surface-variant font-medium">
                        {(room.price / 1000000).toFixed(1)} tr/th
                      </span>
                    )}
                  </div>

                  <div className="pt-1 flex items-center justify-between text-[11px]">
                    {room.contractExpiryDays ? (
                      <span className="text-secondary font-bold flex items-center justify-between w-full">
                        <span>Gia hạn ngay</span>
                        <span className="material-symbols-outlined text-xs">autorenew</span>
                      </span>
                    ) : (
                      <span className="text-outline group-hover:text-primary flex items-center justify-between w-full">
                        <span>HĐ: {room.contractEnd}</span>
                        <span className="material-symbols-outlined text-xs">visibility</span>
                      </span>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* TẦNG 2 */}
          <div className="p-4 rounded-xl bg-surface-container-low/60 border border-surface-container flex flex-col gap-3">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <span className="px-2.5 py-0.5 rounded-lg bg-surface-container text-xs text-primary font-bold">
                  TẦNG 2
                </span>
                <span className="text-xs text-on-surface-variant font-medium">
                  Khu căn hộ tiêu chuẩn ban công thoáng • 9 phòng
                </span>
              </div>
              <span className="text-xs text-on-surface-variant font-medium">
                8/9 Phòng có khách (1 sửa chữa)
              </span>
            </div>

            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-5 lg:grid-cols-9 gap-2.5">
              {floor2Rooms.map((room) => {
                const isUnderRepair = room.status === 'maintenance' || room.roomNumber === 'P.206';
                const isWaitingOtp = room.roomNumber === 'P.205';

                return (
                  <div
                    key={room.id}
                    onClick={() => onSelectRoom(room.id)}
                    className={`p-2.5 rounded-xl border transition-all cursor-pointer group flex flex-col justify-between ${
                      isUnderRepair
                        ? 'bg-gradient-to-b from-error-container/50 to-surface-container-lowest border-error/30 hover:border-error shadow-sm'
                        : isWaitingOtp
                        ? 'bg-gradient-to-b from-secondary-fixed/40 to-surface-container-lowest border-secondary/30 hover:border-secondary shadow-sm'
                        : room.status === 'available'
                        ? 'bg-gradient-to-b from-tertiary-fixed/30 to-surface-container-lowest border-tertiary/30 hover:border-tertiary shadow-sm'
                        : 'bg-surface-container-lowest border-outline-variant/30 hover:border-primary/50 shadow-xs hover:shadow-md'
                    }`}
                  >
                    <div className="flex items-start justify-between">
                      <span className={`font-headline text-sm font-bold ${
                        isUnderRepair ? 'text-error' : isWaitingOtp ? 'text-secondary' : 'text-on-surface'
                      }`}>
                        {room.roomNumber}
                      </span>
                      {isUnderRepair ? (
                        <span className="material-symbols-outlined text-error text-sm">plumbing</span>
                      ) : isWaitingOtp ? (
                        <span className="material-symbols-outlined text-secondary text-sm">mark_email_unread</span>
                      ) : (
                        <span className="w-2 h-2 rounded-full bg-primary"></span>
                      )}
                    </div>

                    <div className="my-2 truncate">
                      {isUnderRepair ? (
                        <>
                          <span className="text-[11px] font-bold text-error truncate block">ĐANG SỬA CHỮA</span>
                          <span className="text-xs text-on-surface-variant">Sửa máy lạnh Daikin</span>
                        </>
                      ) : isWaitingOtp ? (
                        <>
                          <span className="text-[11px] font-bold text-secondary truncate block">Chờ OTP khách</span>
                          <span className="text-xs text-on-surface-variant font-medium">4.5 tr/th</span>
                        </>
                      ) : (
                        <>
                          <span className="text-[11px] font-semibold text-on-surface truncate block">{room.currentTenant}</span>
                          <span className="text-xs text-on-surface-variant font-medium">{(room.price / 1000000).toFixed(1)} tr/th</span>
                        </>
                      )}
                    </div>

                    <div className="pt-1 flex items-center justify-between text-[11px]">
                      {isUnderRepair ? (
                        <span className="text-error font-bold flex items-center justify-between w-full">
                          <span>Tiến độ CSVC</span>
                          <span className="material-symbols-outlined text-xs">build</span>
                        </span>
                      ) : isWaitingOtp ? (
                        <span className="text-secondary font-bold flex items-center justify-between w-full">
                          <span>Gửi lại OTP</span>
                          <span className="material-symbols-outlined text-xs">send</span>
                        </span>
                      ) : (
                        <span className="text-outline group-hover:text-primary flex items-center justify-between w-full">
                          <span>HĐ: {room.contractEnd}</span>
                          <span className="material-symbols-outlined text-xs">visibility</span>
                        </span>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          {/* TẦNG 1 */}
          <div className="p-4 rounded-xl bg-surface-container-low/60 border border-surface-container flex flex-col gap-3">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <span className="px-2.5 py-0.5 rounded-lg bg-surface-container text-xs text-primary font-bold">
                  TẦNG 1
                </span>
                <span className="text-xs text-on-surface-variant font-medium">
                  Khu tầng trệt + Căn hộ có lối đi riêng • 9 phòng
                </span>
              </div>
              <span className="text-xs text-on-surface-variant font-medium">
                8/9 Phòng có khách (1 phòng trống)
              </span>
            </div>

            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-5 lg:grid-cols-9 gap-2.5">
              {floor1Rooms.map((room) => {
                const isReadyAvailable = room.status === 'available';

                return (
                  <div
                    key={room.id}
                    onClick={() => onSelectRoom(room.id)}
                    className={`p-2.5 rounded-xl border transition-all cursor-pointer group flex flex-col justify-between ${
                      isReadyAvailable
                        ? 'bg-gradient-to-b from-tertiary-fixed/30 to-surface-container-lowest border-tertiary/30 hover:border-tertiary shadow-sm'
                        : 'bg-surface-container-lowest border-outline-variant/30 hover:border-primary/50 shadow-xs hover:shadow-md'
                    }`}
                  >
                    <div className="flex items-start justify-between">
                      <span className={`font-headline text-sm font-bold ${
                        isReadyAvailable ? 'text-tertiary' : 'text-on-surface'
                      }`}>
                        {room.roomNumber}
                      </span>
                      {isReadyAvailable ? (
                        <span className="material-symbols-outlined text-base text-tertiary">vpn_key</span>
                      ) : (
                        <span className="w-2 h-2 rounded-full bg-primary"></span>
                      )}
                    </div>

                    <div className="my-2 truncate">
                      {isReadyAvailable ? (
                        <>
                          <span className="text-[11px] font-bold text-tertiary uppercase block">TRỐNG SẴN SÀNG</span>
                          <span className="text-xs text-on-surface-variant font-medium">{(room.price / 1000000).toFixed(1)} tr/th</span>
                        </>
                      ) : (
                        <>
                          <span className="text-[11px] font-semibold text-on-surface truncate block">{room.currentTenant}</span>
                          <span className="text-xs text-on-surface-variant font-medium">{(room.price / 1000000).toFixed(1)} tr/th</span>
                        </>
                      )}
                    </div>

                    <div className="pt-1 flex items-center justify-between text-[11px]">
                      {isReadyAvailable ? (
                        <span className="text-tertiary font-bold flex items-center justify-between w-full">
                          <span>Tạo hợp đồng</span>
                          <span className="material-symbols-outlined text-xs">add</span>
                        </span>
                      ) : (
                        <span className="text-outline group-hover:text-primary flex items-center justify-between w-full">
                          <span>HĐ: {room.contractEnd}</span>
                          <span className="material-symbols-outlined text-xs">visibility</span>
                        </span>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
