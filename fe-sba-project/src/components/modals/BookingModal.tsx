import React, { useState } from 'react';
import { Room } from '../../types';
import { facilityInfo } from '../../data/mockData';

interface BookingModalProps {
  room: Room | null;
  isOpen: boolean;
  onClose: () => void;
  onSuccess: (info: { name: string; phone: string; time: string }) => void;
}

export const BookingModal: React.FC<BookingModalProps> = ({ room, isOpen, onClose, onSuccess }) => {
  const [name, setName] = useState('');
  const [phone, setPhone] = useState('');
  const [time, setTime] = useState('Hôm nay (18:30 - 19:30)');
  const [note, setNote] = useState('');

  if (!isOpen || !room) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSuccess({ name, phone, time });
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-black/60 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="relative bg-surface-container-lowest w-full max-w-lg rounded-2xl shadow-2xl border border-surface-container overflow-hidden my-8 animate-in fade-in zoom-in-95 duration-200">
        <div className="p-6 bg-surface-container-low flex items-start justify-between border-b border-surface-container">
          <div>
            <span className="text-[11px] font-bold text-primary uppercase tracking-wider">Lịch hẹn xem phòng trực tiếp</span>
            <h3 className="font-headline text-lg font-bold text-on-surface mt-0.5">
              Khảo sát {room.roomNumber} • {room.area}m²
            </h3>
            <p className="text-xs text-on-surface-variant">{facilityInfo.address}</p>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="w-8 h-8 rounded-full bg-surface-container hover:bg-surface-container-high flex items-center justify-center text-on-surface-variant cursor-pointer"
          >
            <span className="material-symbols-outlined text-lg">close</span>
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 flex flex-col gap-4">
          <div className="p-3 rounded-xl bg-primary/5 border border-primary/20 flex items-center gap-3">
            <span className="material-symbols-outlined text-primary text-2xl">verified_user</span>
            <div className="text-xs text-on-surface">
              <span className="font-bold text-primary block">Trực tiếp Quản lý cơ sở tiếp đón</span>
              <span>Không qua môi giới, không đặt cọc online trước.</span>
            </div>
          </div>

          <div className="flex flex-col gap-1">
            <label className="text-xs text-on-surface font-semibold">Họ và tên của bạn *</label>
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Nguyễn Văn A"
              required
              className="w-full bg-surface-container rounded-lg px-3 py-2 text-sm text-on-surface focus:outline-none focus:ring-2 focus:ring-primary border-0"
            />
          </div>

          <div className="flex flex-col gap-1">
            <label className="text-xs text-on-surface font-semibold">Số điện thoại liên hệ (có Zalo) *</label>
            <input
              type="tel"
              value={phone}
              onChange={(e) => setPhone(e.target.value)}
              placeholder="09xx xxx xxx"
              required
              className="w-full bg-surface-container rounded-lg px-3 py-2 text-sm text-on-surface focus:outline-none focus:ring-2 focus:ring-primary border-0"
            />
          </div>

          <div className="flex flex-col gap-1">
            <label className="text-xs text-on-surface font-semibold">Thời gian thuận tiện ghé xem phòng</label>
            <select
              value={time}
              onChange={(e) => setTime(e.target.value)}
              className="w-full bg-surface-container rounded-lg px-3 py-2 text-sm text-on-surface focus:outline-none focus:ring-2 focus:ring-primary border-0 cursor-pointer"
            >
              <option value="Hôm nay (18:30 - 19:30)">Hôm nay (18:30 - 19:30)</option>
              <option value="Ngày mai (09:00 - 11:30)">Ngày mai (09:00 - 11:30)</option>
              <option value="Ngày mai (14:00 - 17:00)">Ngày mai (14:00 - 17:00)</option>
              <option value="Ngày mai (18:00 - 20:00)">Ngày mai (18:00 - 20:00)</option>
              <option value="Cuối tuần này (Thứ 7 / CN)">Cuối tuần này (Thứ 7 / CN)</option>
            </select>
          </div>

          <div className="flex flex-col gap-1">
            <label className="text-xs text-on-surface font-semibold">Ghi chú thêm</label>
            <textarea
              value={note}
              onChange={(e) => setNote(e.target.value)}
              placeholder="Ví dụ: Em muốn xem thêm khu để xe máy và khóa cửa..."
              rows={2}
              className="w-full bg-surface-container rounded-lg p-3 text-xs text-on-surface focus:outline-none focus:ring-2 focus:ring-primary border-0"
            />
          </div>

          <div className="pt-2 flex items-center justify-end gap-3 border-t border-surface-container">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 rounded-lg bg-surface-container text-on-surface-variant hover:bg-surface-container-high text-xs font-semibold cursor-pointer"
            >
              Hủy
            </button>
            <button
              type="submit"
              className="px-5 py-2.5 rounded-lg bg-primary hover:bg-primary-container text-white text-xs font-bold shadow-md transition-all flex items-center gap-1.5 cursor-pointer"
            >
              <span className="material-symbols-outlined text-base">calendar_month</span>
              <span>Xác nhận Hẹn Xem Phòng</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
