import React, { useState } from 'react';
import { SeekingThread } from '../../types';

interface CreateThreadModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (thread: Partial<SeekingThread>) => void;
}

export const CreateThreadModal: React.FC<CreateThreadModalProps> = ({ isOpen, onClose, onSubmit }) => {
  const [area, setArea] = useState('binh-thanh');
  const [budget, setBudget] = useState('5.000.000 đ');
  const [minArea, setMinArea] = useState('22');
  const [occupants, setOccupants] = useState('2 người (Ở đôi / Bạn bè)');
  const [moveIn, setMoveIn] = useState('Đầu tháng sau');
  const [amenities, setAmenities] = useState<string[]>(['Máy lạnh', 'Ban công thoáng']);
  const [note, setNote] = useState('');

  if (!isOpen) return null;

  const toggleAmenity = (name: string) => {
    if (amenities.includes(name)) {
      setAmenities(amenities.filter((a) => a !== name));
    } else {
      setAmenities([...amenities, name]);
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const numericBudget = parseInt(budget.replace(/\D/g, ''), 10) || 5000000;
    
    onSubmit({
      authorName: 'Khách tìm trọ',
      authorRole: 'Người thuê mới',
      authorAvatar: 'https://lh3.googleusercontent.com/aida-public/AB6AXuCpUIjYtTTVWYpQcwlUaF1Es5Nn60C1fvlE8PKJUqYTy1oxB0vgCwDJyJjz2pj2__2teT5CGxfbJ7Vx4mrSqWIJAHMnY5XFkjjLRHDALIxJUzcIscJLA765nNgL_BwbG00DvN1pLWGQ_qdgudu-pi_RmQy49Gku7tlFWe2-lO0xYB7iJZ66cTzH9aghhnWnu_pU2Dl-5ckvyD9FAYXJjuH6-CqHKJf_IuBReBWjESaa-fDonG8FlJYx',
      isVerified: true,
      timeAgo: 'Vừa xong',
      title: `Tìm phòng ${amenities.join(', ')} quanh Bình Thạnh`,
      content: note || 'Cần tìm phòng trọ sạch sẽ, an ninh, đúng tiêu chí và dọn vào sớm. Mong gặp chính chủ không môi giới.',
      location: area === 'binh-thanh' ? 'Quận Bình Thạnh (P.25, P.26)' : 'TP. Hồ Chí Minh',
      maxBudget: numericBudget,
      minArea: parseInt(minArea, 10) || 20,
      moveInDate: moveIn,
      occupants,
      amenities,
      status: 'matched_100',
      matchPercent: 100,
      matchedRoom: {
        roomNumber: 'Phòng 401',
        area: 28,
        price: 5000000,
        floor: 4,
        facilityName: 'Cơ sở Đinh Bộ Lĩnh',
        image: 'https://lh3.googleusercontent.com/aida-public/AB6AXuDy1HvTyRVa99LcDyFv03QXj5dS1EP-3Ne6VfJp4__HbIKdIxIFCNlEIsArALxlQ7Q--z-3E78-KQft1zPmimobXnSqK3KX-0VUk1tS47-qQBseP8yzHUJos8RB5D1rcjwHPbw6AE7NyO-zaQX5jhErxrqiO53X-yl6AL7cxa4VfWKYbfybA-uJ8O4wqR7MBLq2zAK1s6bkmo7WGUhzoeP5JHJ_Vh8Lqjw3MqTg8Yq3N8TbBk2HTT0t'
      },
      proposalsCount: 1,
      viewsCount: 1,
      commentsCount: 0
    });
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 overflow-y-auto bg-black/60 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="relative bg-surface-container-lowest w-full max-w-2xl rounded-2xl shadow-2xl border border-surface-container overflow-hidden my-8 animate-in fade-in zoom-in-95 duration-200">
        {/* Header */}
        <div className="p-6 bg-surface-container-low flex items-start justify-between border-b border-surface-container">
          <div>
            <div className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full bg-primary-container text-on-primary-container text-xs font-semibold mb-1.5">
              <span className="material-symbols-outlined text-[14px]">auto_awesome</span>
              <span>Tính năng Thông Minh (FR-THR-02)</span>
            </div>
            <h3 className="font-headline text-xl font-bold text-on-surface">
              Đăng Thread Tìm Trọ Theo Tiêu Chí Của Bạn
            </h3>
            <p className="text-xs text-on-surface-variant mt-0.5">
              Hệ thống tự động so khớp 100% với phòng trống thực tế của Quản lý cơ sở & Chủ trọ.
            </p>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="w-8 h-8 rounded-full bg-surface-container hover:bg-surface-container-high flex items-center justify-center text-on-surface-variant hover:text-on-surface transition-colors cursor-pointer"
          >
            <span className="material-symbols-outlined text-lg">close</span>
          </button>
        </div>

        {/* Form Body */}
        <form onSubmit={handleSubmit} className="p-6 flex flex-col gap-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="flex flex-col gap-1">
              <label className="text-xs text-tertiary flex items-center gap-1 font-semibold">
                <span className="material-symbols-outlined text-base">location_on</span>
                Khu vực mong muốn *
              </label>
              <select
                value={area}
                onChange={(e) => setArea(e.target.value)}
                className="w-full bg-surface-container rounded-lg py-2 px-3 text-on-surface text-sm focus:outline-none focus:ring-2 focus:ring-primary border-0 cursor-pointer"
              >
                <option value="binh-thanh">Quận Bình Thạnh (P.25, P.26, Đinh Bộ Lĩnh)</option>
                <option value="quan-1">Quận 1 (Đa Kao, Tân Định, Bến Nghé)</option>
                <option value="phu-nhuan">Quận Phú Nhuận (Phan Xích Long, P.7)</option>
                <option value="quan-3">Quận 3 (Lê Văn Sỹ, Nam Kỳ Khởi Nghĩa)</option>
                <option value="thu-duc">TP. Thủ Đức (Linh Trung, ĐH Quốc Gia)</option>
              </select>
            </div>

            <div className="flex flex-col gap-1">
              <label className="text-xs text-tertiary flex items-center gap-1 font-semibold">
                <span className="material-symbols-outlined text-base">payments</span>
                Ngân sách tối đa (VNĐ/tháng) *
              </label>
              <input
                type="text"
                value={budget}
                onChange={(e) => setBudget(e.target.value)}
                placeholder="Ví dụ: 4.500.000 đ"
                required
                className="w-full bg-surface-container rounded-lg py-2 px-3 text-on-surface text-sm focus:outline-none focus:ring-2 focus:ring-primary border-0"
              />
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="flex flex-col gap-1">
              <label className="text-xs text-tertiary flex items-center gap-1 font-semibold">
                <span className="material-symbols-outlined text-base">aspect_ratio</span>
                Diện tích tối thiểu
              </label>
              <select
                value={minArea}
                onChange={(e) => setMinArea(e.target.value)}
                className="w-full bg-surface-container rounded-lg py-2 px-3 text-on-surface text-sm focus:outline-none focus:ring-2 focus:ring-primary border-0 cursor-pointer"
              >
                <option value="20">Từ 20 m² trở lên</option>
                <option value="22">Từ 22 m² trở lên</option>
                <option value="25">Từ 25 m² trở lên</option>
                <option value="30">Từ 30 m² trở lên (Studio)</option>
              </select>
            </div>

            <div className="flex flex-col gap-1">
              <label className="text-xs text-tertiary flex items-center gap-1 font-semibold">
                <span className="material-symbols-outlined text-base">group</span>
                Số lượng người ở
              </label>
              <select
                value={occupants}
                onChange={(e) => setOccupants(e.target.value)}
                className="w-full bg-surface-container rounded-lg py-2 px-3 text-on-surface text-sm focus:outline-none focus:ring-2 focus:ring-primary border-0 cursor-pointer"
              >
                <option value="1 người">1 người (Phòng đơn tiêu chuẩn)</option>
                <option value="2 người (Ở đôi / Bạn bè)">2 người (Ở đôi / Bạn bè)</option>
                <option value="3 người trở lên (Nhóm sinh viên)">3 người trở lên (Nhóm sinh viên)</option>
              </select>
            </div>
          </div>

          {/* Move-in schedule */}
          <div className="flex flex-col gap-1">
            <label className="text-xs text-tertiary flex items-center gap-1 font-semibold">
              <span className="material-symbols-outlined text-base">schedule</span>
              Thời gian dự kiến dọn vào
            </label>
            <div className="grid grid-cols-3 gap-2">
              {['Ngay lập tức', 'Trong tuần này', 'Đầu tháng sau'].map((time) => (
                <label
                  key={time}
                  className={`flex items-center gap-2 p-2.5 rounded-lg cursor-pointer transition-colors text-xs font-medium border ${
                    moveIn === time
                      ? 'bg-primary/10 border-primary text-primary font-bold'
                      : 'bg-surface-container border-transparent text-on-surface hover:bg-surface-container-high'
                  }`}
                >
                  <input
                    type="radio"
                    name="move_in"
                    checked={moveIn === time}
                    onChange={() => setMoveIn(time)}
                    className="accent-primary"
                  />
                  <span>{time}</span>
                </label>
              ))}
            </div>
          </div>

          {/* Mandatory Amenities */}
          <div className="flex flex-col gap-1">
            <label className="text-xs text-tertiary flex items-center gap-1 font-semibold">
              <span className="material-symbols-outlined text-base">checklist</span>
              Tiện ích bắt buộc phải có
            </label>
            <div className="flex flex-wrap gap-2 pt-1">
              {[
                'Máy lạnh',
                'Ban công thoáng',
                'Tủ lạnh sẵn',
                'Giờ giấc tự do',
                'Thang máy',
                'Cho nuôi thú cưng',
                'Chỗ để xe máy an toàn'
              ].map((item) => {
                const selected = amenities.includes(item);
                return (
                  <button
                    key={item}
                    type="button"
                    onClick={() => toggleAmenity(item)}
                    className={`inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-semibold cursor-pointer transition-all ${
                      selected
                        ? 'bg-primary-container text-white shadow-xs'
                        : 'bg-surface-container text-on-surface-variant hover:bg-surface-container-high'
                    }`}
                  >
                    <span className="material-symbols-outlined text-sm">
                      {selected ? 'check' : 'add'}
                    </span>
                    <span>{item}</span>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Notes */}
          <div className="flex flex-col gap-1">
            <label className="text-xs text-tertiary flex items-center gap-1 font-semibold">
              <span className="material-symbols-outlined text-base">edit_note</span>
              Ghi chú thêm về yêu cầu của bạn
            </label>
            <textarea
              value={note}
              onChange={(e) => setNote(e.target.value)}
              placeholder="Ví dụ: Ưu tiên phòng yên tĩnh, tầng 2-4, có khóa vân tay, không chung chủ..."
              rows={2}
              className="w-full bg-surface-container rounded-lg p-3 text-on-surface text-xs focus:outline-none focus:ring-2 focus:ring-primary border-0"
            />
          </div>

          {/* Actions */}
          <div className="pt-2 flex items-center justify-end gap-3 border-t border-surface-container">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 rounded-lg bg-surface-container text-on-surface-variant hover:bg-surface-container-high text-xs font-semibold cursor-pointer"
            >
              Đóng lại
            </button>
            <button
              type="submit"
              className="px-5 py-2.5 rounded-lg bg-secondary-container hover:bg-secondary text-white text-xs font-bold shadow-md transition-all flex items-center gap-1.5 cursor-pointer"
            >
              <span className="material-symbols-outlined text-base">send</span>
              <span>Đăng Thread & Tìm Phòng Khớp</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
