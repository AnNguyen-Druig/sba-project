import React from 'react';
import { ViewMode } from '../../types';
import { facilityInfo } from '../../data/mockData';

interface PublicHeaderProps {
  currentView: ViewMode;
  onNavigate: (view: ViewMode) => void;
  onOpenCreateThread: () => void;
  onSwitchToManager: () => void;
}

export const PublicHeader: React.FC<PublicHeaderProps> = ({
  currentView,
  onNavigate,
  onOpenCreateThread,
  onSwitchToManager
}) => {
  return (
    <header className="fixed top-0 left-0 right-0 w-full z-50 bg-surface-container-lowest/95 backdrop-blur-md shadow-[0_1px_8px_rgba(0,0,0,0.04)] border-b border-surface-container">
      <div className="h-20 max-w-7xl mx-auto px-6 flex items-center justify-between gap-4">
        {/* Brand Logo */}
        <div className="flex items-center gap-6 shrink-0">
          <button 
            type="button" 
            onClick={() => onNavigate('public-search')}
            className="flex items-center gap-2 cursor-pointer"
          >
            <img 
              alt="TroPro Logo" 
              className="h-8 w-auto object-contain" 
              src={facilityInfo.logoUrl} 
            />
            <div className="flex flex-col text-left">
              <span className="font-headline text-lg font-bold text-primary leading-none">TroPro</span>
              <span className="text-[10px] text-on-surface-variant tracking-wider uppercase font-semibold">Cổng Tìm Trọ Chính Chủ</span>
            </div>
          </button>
        </div>

        {/* Navigation Links */}
        <nav className="hidden lg:flex items-center gap-1.5">
          <button
            type="button"
            onClick={() => onNavigate('public-search')}
            className={`px-4 py-2 rounded-lg text-sm transition-all cursor-pointer font-medium ${
              currentView === 'public-search'
                ? 'bg-primary-container text-on-primary-container font-semibold shadow-sm'
                : 'text-on-surface-variant hover:text-on-surface hover:bg-surface-container'
            }`}
          >
            Tìm trọ quanh đây
          </button>

          <button
            type="button"
            onClick={() => onNavigate('public-threads')}
            className={`px-4 py-2 rounded-lg text-sm transition-all cursor-pointer font-medium flex items-center gap-1.5 ${
              currentView === 'public-threads'
                ? 'bg-primary-container text-on-primary-container font-semibold shadow-sm'
                : 'text-on-surface-variant hover:text-on-surface hover:bg-surface-container'
            }`}
          >
            <span className="material-symbols-outlined text-lg">forum</span>
            <span>Kênh Thread cộng đồng</span>
          </button>

          <button
            type="button"
            onClick={() => {
              const el = document.getElementById('safety-guide');
              if (el) el.scrollIntoView({ behavior: 'smooth' });
            }}
            className="px-4 py-2 rounded-lg text-on-surface-variant hover:text-on-surface hover:bg-surface-container transition-colors text-sm font-medium cursor-pointer"
          >
            Giới thiệu TroPro
          </button>
        </nav>

        {/* Right Action Cluster */}
        <div className="flex items-center gap-4 shrink-0">
          {/* Hotline */}
          <div className="hidden sm:flex flex-col items-end">
            <span className="text-[11px] text-on-surface-variant font-medium">Hỗ trợ Zalo/Hotline</span>
            <a href="tel:19006868" className="font-headline text-sm font-bold text-primary hover:text-primary-container transition-colors">
              1900 6868
            </a>
          </div>

          {/* Post Thread CTA Button */}
          <button
            type="button"
            onClick={onOpenCreateThread}
            className="hidden md:inline-flex items-center gap-1 px-3.5 py-2 rounded-lg bg-secondary text-white hover:bg-secondary-container transition-all text-xs font-semibold shadow-sm cursor-pointer active:scale-95"
          >
            <span className="material-symbols-outlined text-base">add</span>
            <span>Đăng tin tìm trọ</span>
          </button>

          {/* Switch back to Manager Back-office */}
          <button
            type="button"
            onClick={onSwitchToManager}
            className="inline-flex items-center gap-1.5 px-3.5 py-2 rounded-lg bg-primary text-white hover:bg-primary-container transition-all text-xs font-semibold shadow-sm cursor-pointer"
            title="Truy cập bảng điều khiển Chủ trọ & Quản lý cơ sở"
          >
            <span className="material-symbols-outlined text-base">dashboard</span>
            <span>Vào Quản lý Trọ</span>
          </button>
        </div>
      </div>
    </header>
  );
};
