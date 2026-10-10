import React, { useState } from 'react';
import { facilityInfo } from '../../data/mockData';

interface HeaderProps {
  onSwitchToPublic: () => void;
  searchQuery: string;
  onSearchChange: (q: string) => void;
}

export const Header: React.FC<HeaderProps> = ({ onSwitchToPublic, searchQuery, onSearchChange }) => {
  const [showNotificationMenu, setShowNotificationMenu] = useState(false);
  const [notifications] = useState([
    { id: 1, title: 'Hóa đơn P.201 đã quá hạn 6 ngày', time: '10 phút trước', unread: true },
    { id: 2, title: 'Minh Trang vừa đăng thread tìm trọ khớp P.401', time: '42 phút trước', unread: true },
    { id: 3, title: 'Yêu cầu sửa máy lạnh P.204 đã được tiếp nhận', time: '2 giờ trước', unread: false },
  ]);

  return (
    <header className="fixed top-0 left-72 right-0 h-16 bg-surface-container-lowest/95 backdrop-blur-xl shadow-[0_1px_8px_rgba(0,0,0,0.04)] z-40 flex items-center justify-between px-6 border-b border-surface-container">
      {/* Global Search Bar */}
      <div className="flex items-center gap-4 w-96 max-w-md">
        <div className="relative w-full">
          <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline text-lg">
            search
          </span>
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => onSearchChange(e.target.value)}
            placeholder="Tìm phòng, người thuê, số điện thoại, hóa đơn..."
            className="w-full pl-10 pr-4 py-2 rounded-lg bg-surface-container-low text-xs text-on-surface placeholder:text-outline focus:outline-none focus:bg-surface-container-lowest focus:ring-1 focus:ring-primary transition-colors border border-transparent focus:border-primary/30"
          />
          {searchQuery && (
            <button
              onClick={() => onSearchChange('')}
              className="absolute right-2.5 top-1/2 -translate-y-1/2 text-outline hover:text-on-surface text-xs"
            >
              <span className="material-symbols-outlined text-sm">close</span>
            </button>
          )}
        </div>
      </div>

      {/* Right Controls */}
      <div className="flex items-center gap-4">
        {/* Switch to Public View Button */}
        <button
          type="button"
          onClick={onSwitchToPublic}
          className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-secondary-fixed/40 text-on-secondary-fixed-variant hover:bg-secondary-fixed/70 transition-colors text-xs font-semibold"
          title="Xem giao diện Cổng Tìm Trọ cho khách vãng lai"
        >
          <span className="material-symbols-outlined text-secondary text-base">public</span>
          <span>Xem Cổng Tìm Trọ</span>
        </button>

        {/* Zalo Group Link */}
        <a
          href="https://zalo.me"
          target="_blank"
          rel="noopener noreferrer"
          className="hidden sm:inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-surface-container-low text-on-surface hover:bg-surface-container transition-colors text-xs font-medium border border-outline-variant/30"
        >
          <span className="material-symbols-outlined text-primary text-base">chat</span>
          <span>Zalo Group Cơ sở</span>
        </a>

        {/* Notification Bell */}
        <div className="relative">
          <button
            type="button"
            onClick={() => setShowNotificationMenu(!showNotificationMenu)}
            className="relative p-2 rounded-lg text-on-surface-variant hover:bg-surface-container hover:text-on-surface transition-colors cursor-pointer"
            aria-label="Thông báo"
          >
            <span className="material-symbols-outlined text-xl">notifications</span>
            <span className="absolute top-1.5 right-1.5 w-2.5 h-2.5 bg-secondary-container rounded-full ring-2 ring-white"></span>
          </button>

          {showNotificationMenu && (
            <div className="absolute right-0 mt-2 w-80 bg-surface-container-lowest rounded-xl shadow-xl border border-surface-container-high py-2 z-50">
              <div className="px-4 py-2 border-b border-surface-container flex items-center justify-between">
                <span className="font-bold text-xs text-on-surface">Thông báo vận hành</span>
                <span className="text-[11px] text-primary font-semibold cursor-pointer hover:underline">Đánh dấu đã đọc</span>
              </div>
              <div className="divide-y divide-surface-container max-h-64 overflow-y-auto">
                {notifications.map((n) => (
                  <div key={n.id} className="p-3 hover:bg-surface-container-low transition-colors cursor-pointer">
                    <p className="text-xs text-on-surface font-medium">{n.title}</p>
                    <span className="text-[10px] text-outline mt-0.5 block">{n.time}</span>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>

        <div className="h-7 w-px bg-outline-variant/40"></div>

        {/* User Profile */}
        <div className="flex items-center gap-3">
          <img
            alt={facilityInfo.managerName}
            src={facilityInfo.managerAvatar}
            className="w-9 h-9 rounded-full object-cover shadow-sm ring-1 ring-primary/20"
          />
          <div className="hidden lg:flex flex-col text-left">
            <span className="text-xs font-bold text-on-surface leading-tight">
              {facilityInfo.managerName}
            </span>
            <span className="text-[11px] text-on-surface-variant leading-tight">
              {facilityInfo.managerRole}
            </span>
          </div>
        </div>
      </div>
    </header>
  );
};
