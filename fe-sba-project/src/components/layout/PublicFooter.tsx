import React from 'react';
import { facilityInfo } from '../../data/mockData';

export const PublicFooter: React.FC = () => {
  return (
    <footer className="w-full bg-surface-container-lowest mt-16 border-t border-surface-container">
      <div className="max-w-7xl mx-auto px-6 py-12">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8 mb-8">
          <div className="space-y-3 md:col-span-1">
            <div className="flex items-center gap-2">
              <span className="font-headline text-2xl font-bold text-primary">TroPro</span>
            </div>
            <p className="text-xs text-on-surface-variant leading-relaxed">
              Nền tảng tìm kiếm và thuê nhà trọ minh bạch hàng đầu. Kết nối trực tiếp giữa người đi thuê và chính chủ dãy trọ trên toàn quốc.
            </p>
            <div className="pt-1">
              <span className="inline-block px-2.5 py-1 rounded bg-surface-container text-tertiary text-[11px] font-semibold">
                Cam kết 100% không phí môi giới
              </span>
            </div>
          </div>

          <div className="space-y-3">
            <h4 className="font-headline text-sm font-bold text-on-surface">Quy định minh bạch</h4>
            <ul className="space-y-2 text-xs text-on-surface-variant">
              <li>• Phí môi giới: Hoàn toàn 0đ cho khách thuê</li>
              <li>• Tiền cọc & Tiền phòng chuyển trực tiếp chủ trọ</li>
              <li>• Cam kết tin thật, phòng thật, giá niêm yết chuẩn</li>
              <li>• Chính sách bảo mật dữ liệu cá nhân khách thuê</li>
            </ul>
          </div>

          <div className="space-y-3">
            <h4 className="font-headline text-sm font-bold text-on-surface">Liên hệ & Hỗ trợ</h4>
            <ul className="space-y-2 text-xs text-on-surface-variant">
              <li>• Hotline 24/7: 1900 6868 (Miễn phí cuộc gọi)</li>
              <li>• Zalo Official Account: TroPro Việt Nam</li>
              <li>• Hỗ trợ xử lý vi phạm: support@tropro.vn</li>
              <li>• Thời gian trực tổng đài: 08:00 - 22:00 hàng ngày</li>
            </ul>
          </div>

          <div className="space-y-3">
            <h4 className="font-headline text-sm font-bold text-on-surface">Dành cho Chủ trọ</h4>
            <p className="text-xs text-on-surface-variant leading-relaxed">
              Quản lý lấp phòng trống, ghi chỉ số điện nước tự động, xuất hóa đơn Zalo với hệ sinh thái TroPro Manager.
            </p>
            <div className="pt-1">
              <span className="inline-flex items-center px-3 py-1.5 rounded-lg bg-surface-container text-primary text-xs font-semibold">
                {facilityInfo.address}
              </span>
            </div>
          </div>
        </div>

        <div className="pt-6 border-t border-surface-container flex flex-col sm:flex-row items-center justify-between gap-4 text-on-surface-variant text-xs">
          <p>© 2024 - 2026 TroPro. Nền tảng tìm trọ và quản lý nhà trọ công nghệ cao.</p>
          <div className="flex items-center gap-6 text-[11px]">
            <span className="hover:text-primary transition-colors cursor-pointer">Chính sách bảo mật</span>
            <span className="hover:text-primary transition-colors cursor-pointer">Điều khoản dịch vụ</span>
            <span className="hover:text-primary transition-colors cursor-pointer">Quy trình thuê an toàn</span>
          </div>
        </div>
      </div>
    </footer>
  );
};
