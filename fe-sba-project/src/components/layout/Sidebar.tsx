import React from 'react';
import { ViewMode } from '../../types';
import { facilityInfo } from '../../data/mockData';

interface SidebarProps {
  currentView: ViewMode;
  onNavigate: (view: ViewMode) => void;
  onSwitchToPublic: () => void;
}

export const Sidebar: React.FC<SidebarProps> = ({ currentView, onNavigate, onSwitchToPublic }) => {
  const navItems: { id: ViewMode; label: string; icon: string }[] = [
    { id: 'overview', label: 'Tổng quan', icon: 'dashboard' },
    { id: 'rooms', label: 'Sơ đồ & Phòng', icon: 'meeting_room' },
    { id: 'billing', label: 'Hóa đơn & Thu phí', icon: 'receipt_long' },
    { id: 'threads-manager', label: 'Kênh tìm trọ', icon: 'forum' },
    { id: 'contracts', label: 'Hợp đồng điện tử', icon: 'history_edu' },
    { id: 'maintenance', label: 'CSVC & Sửa chữa', icon: 'build' },
    { id: 'utilities', label: 'Điện nước & Dịch vụ', icon: 'water_drop' },
    { id: 'settings', label: 'Cài đặt Cơ sở', icon: 'tune' },
  ];

  return (
    <aside className="fixed left-0 top-0 h-full w-72 bg-surface-container-low shadow-[0_1px_8px_rgba(0,0,0,0.04)] z-50 flex flex-col justify-between overflow-y-auto border-r border-outline-variant/30">
      <div className="flex flex-col">
        {/* Brand Logo Header */}
        <div className="h-16 px-6 flex items-center justify-between bg-surface-container-lowest border-b border-surface-container">
          <div className="flex items-center gap-2.5">
            <img 
              alt="TroPro Logo" 
              className="h-8 w-auto object-contain" 
              src={facilityInfo.logoUrl} 
            />
            <div className="flex flex-col">
              <span className="font-headline text-lg font-bold text-primary leading-none">TroPro</span>
              <span className="text-[11px] text-on-surface-variant tracking-wider uppercase font-semibold">Quản lý Nhà trọ</span>
            </div>
          </div>
        </div>

        {/* Facility Selector Card */}
        <div className="p-4">
          <div className="p-3 bg-surface-container rounded-xl flex items-center justify-between cursor-pointer hover:bg-surface-container-high transition-colors">
            <div className="flex flex-col min-w-0 pr-1">
              <span className="text-[11px] text-on-surface-variant truncate font-medium">Cơ sở vận hành</span>
              <span className="text-sm text-on-surface truncate font-bold">{facilityInfo.shortName}</span>
            </div>
            <span className="material-symbols-outlined text-on-surface-variant flex-shrink-0 text-lg">unfold_more</span>
          </div>
        </div>

        {/* Navigation Links */}
        <nav className="flex flex-col gap-1 px-3">
          {navItems.map((item) => {
            const isActive = currentView === item.id || 
              (item.id === 'rooms' && currentView === 'room-detail') ||
              (item.id === 'contracts' && currentView === 'create-contract');

            return (
              <button
                key={item.id}
                type="button"
                onClick={() => onNavigate(item.id)}
                className={`flex items-center gap-3 px-4 py-2.5 rounded-lg text-sm transition-all text-left cursor-pointer ${
                  isActive
                    ? 'bg-primary-container text-on-primary-container font-semibold shadow-sm'
                    : 'text-on-surface-variant hover:bg-surface-container hover:text-on-surface font-medium'
                }`}
              >
                <span className={`material-symbols-outlined text-xl ${isActive ? 'text-white' : ''}`}>
                  {item.icon}
                </span>
                <span className="truncate">{item.label}</span>
              </button>
            );
          })}
        </nav>

        {/* Public Portal Quick Access Card */}
        <div className="px-4 pt-4">
          <div className="p-3 rounded-xl bg-gradient-to-r from-secondary-container/10 to-primary-container/10 border border-secondary-container/20 flex flex-col gap-2">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold text-secondary uppercase tracking-wider flex items-center gap-1">
                <span className="material-symbols-outlined text-sm">travel_explore</span>
                Cổng Khách Thuê
              </span>
              <span className="px-1.5 py-0.5 rounded text-[10px] bg-secondary-container text-white font-bold">Public</span>
            </div>
            <p className="text-xs text-on-surface-variant">Tra cứu phòng trống & Kênh cộng đồng như người thuê</p>
            <button
              type="button"
              onClick={onSwitchToPublic}
              className="w-full py-1.5 px-3 rounded-lg bg-surface-container-lowest text-primary hover:bg-primary hover:text-white border border-primary/20 text-xs font-semibold transition-all flex items-center justify-center gap-1.5 shadow-xs"
            >
              <span>Xem Cổng Tìm Trọ</span>
              <span className="material-symbols-outlined text-sm">open_in_new</span>
            </button>
          </div>
        </div>
      </div>

      {/* Support hotline footer */}
      <div className="p-4">
        <div className="p-3.5 rounded-xl bg-surface-container-high flex flex-col gap-1 border border-outline-variant/30">
          <div className="flex items-center gap-1.5 text-primary">
            <span className="material-symbols-outlined text-base">support_agent</span>
            <span className="text-xs font-bold">Hỗ trợ Kỹ thuật</span>
          </div>
          <span className="text-xs text-on-surface-variant font-medium">Hotline TroPro: {facilityInfo.hotline}</span>
        </div>
      </div>
    </aside>
  );
};
