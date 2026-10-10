import React, { useState } from 'react';
import { Room } from '../../types';

interface AddRoomModalProps {
  isOpen: boolean;
  onClose: () => void;
  onAddRoom: (room: Partial<Room>) => void;
}

export const AddRoomModal: React.FC<AddRoomModalProps> = ({ isOpen, onClose, onAddRoom }) => {
  const [roomNumber, setRoomNumber] = useState('');
  const [floor, setFloor] = useState(2);
  const [area, setArea] = useState(28);
  const [price, setPrice] = useState(4800000);
  const [type, setType] = useState('Studio tiêu chuẩn ban công');
  const [status, setStatus] = useState<'available' | 'rented'>('available');

  if (!isOpen) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onAddRoom({
      roomNumber: roomNumber.startsWith('P.') ? roomNumber : `P.${roomNumber}`,
      floor,
      floorName: `Tầng ${floor}`,
      area,
      price,
      type,
      status,
      statusLabel: status === 'available' ? 'Trống sẵn sàng' : 'Đang thuê',
      capacity: '2-3 người'
    });
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="bg-surface-container-lowest w-full max-w-md rounded-2xl p-6 shadow-2xl border border-surface-container flex flex-col gap-4 animate-in fade-in zoom-in-95">
        <div className="flex items-center justify-between border-b border-surface-container pb-3">
          <div className="flex items-center gap-2">
            <span className="material-symbols-outlined text-primary text-xl">add_circle</span>
            <h3 className="font-headline text-base font-bold text-on-surface">Thêm phòng mới vào cơ sở</h3>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="w-7 h-7 rounded-full bg-surface-container flex items-center justify-center text-on-surface-variant cursor-pointer"
          >
            <span className="material-symbols-outlined text-sm">close</span>
          </button>
        </div>

        <form onSubmit={handleSubmit} className="flex flex-col gap-3 text-xs">
          <div className="flex flex-col gap-1">
            <label className="font-semibold text-on-surface">Mã số phòng (Ví dụ: 210, 410) *</label>
            <input
              type="text"
              value={roomNumber}
              onChange={(e) => setRoomNumber(e.target.value)}
              placeholder="P.210"
              required
              className="bg-surface-container rounded-xl p-2.5 font-bold text-primary focus:outline-none focus:ring-2 focus:ring-primary"
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div className="flex flex-col gap-1">
              <label className="font-semibold text-on-surface">Tầng</label>
              <select
                value={floor}
                onChange={(e) => setFloor(Number(e.target.value))}
                className="bg-surface-container rounded-xl p-2.5 font-semibold text-on-surface focus:outline-none cursor-pointer"
              >
                <option value={1}>Tầng 1</option>
                <option value={2}>Tầng 2</option>
                <option value={3}>Tầng 3</option>
                <option value={4}>Tầng 4</option>
              </select>
            </div>

            <div className="flex flex-col gap-1">
              <label className="font-semibold text-on-surface">Diện tích (m²)</label>
              <input
                type="number"
                value={area}
                onChange={(e) => setArea(Number(e.target.value))}
                className="bg-surface-container rounded-xl p-2.5 font-semibold text-on-surface focus:outline-none"
              />
            </div>
          </div>

          <div className="flex flex-col gap-1">
            <label className="font-semibold text-on-surface">Loại phòng</label>
            <input
              type="text"
              value={type}
              onChange={(e) => setType(e.target.value)}
              className="bg-surface-container rounded-xl p-2.5 font-semibold text-on-surface focus:outline-none"
            />
          </div>

          <div className="flex flex-col gap-1">
            <label className="font-semibold text-on-surface">Giá niêm yết (VNĐ/tháng)</label>
            <input
              type="number"
              value={price}
              step={100000}
              onChange={(e) => setPrice(Number(e.target.value))}
              className="bg-surface-container rounded-xl p-2.5 font-bold text-secondary focus:outline-none tabular-nums"
            />
          </div>

          <div className="flex flex-col gap-1">
            <label className="font-semibold text-on-surface">Trạng thái ban đầu</label>
            <select
              value={status}
              onChange={(e) => setStatus(e.target.value as any)}
              className="bg-surface-container rounded-xl p-2.5 font-semibold text-on-surface focus:outline-none cursor-pointer"
            >
              <option value="available">Trống sẵn sàng</option>
              <option value="rented">Đang thuê</option>
            </select>
          </div>

          <div className="flex items-center justify-end gap-2 pt-3 border-t border-surface-container">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 rounded-xl bg-surface-container text-on-surface-variant font-semibold cursor-pointer"
            >
              Hủy
            </button>
            <button
              type="submit"
              className="px-5 py-2 rounded-xl bg-primary text-white font-bold hover:bg-primary-container transition-all cursor-pointer shadow-sm"
            >
              Lưu phòng
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
