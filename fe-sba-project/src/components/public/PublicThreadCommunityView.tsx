import React, { useState } from 'react';
import { SeekingThread } from '../../types';

interface PublicThreadCommunityViewProps {
  threads: SeekingThread[];
  onOpenCreateThread: () => void;
  onSwitchToManager: () => void;
}

export const PublicThreadCommunityView: React.FC<PublicThreadCommunityViewProps> = ({
  threads,
  onOpenCreateThread,
  onSwitchToManager
}) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedDistrict, setSelectedDistrict] = useState('binh-thanh');
  const [selectedBudget, setSelectedBudget] = useState('all');
  const [statusFilter, setStatusFilter] = useState<'all' | 'urgent' | 'matched' | 'closed'>('all');
  const [expandedProposalId, setExpandedProposalId] = useState<string | null>('thr-1');

  const filteredThreads = threads.filter((t) => {
    if (searchTerm.trim()) {
      const matchTitle = t.title.toLowerCase().includes(searchTerm.toLowerCase());
      const matchContent = t.content.toLowerCase().includes(searchTerm.toLowerCase());
      const matchLoc = t.location.toLowerCase().includes(searchTerm.toLowerCase());
      if (!matchTitle && !matchContent && !matchLoc) return false;
    }
    if (statusFilter === 'matched' && t.matchPercent !== 100) return false;
    return true;
  });

  return (
    <div className="flex flex-col w-full pb-16">
      {/* Hero Header Section */}
      <section className="relative w-full overflow-hidden bg-surface-container-low px-6 py-12 lg:py-16 border-b border-surface-container">
        <div className="max-w-7xl mx-auto flex flex-col lg:flex-row lg:items-center lg:justify-between gap-8 relative z-10">
          <div className="max-w-3xl space-y-3">
            <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-primary-container text-white text-xs font-bold shadow-xs">
              <span className="material-symbols-outlined text-[15px]">verified</span>
              <span>Cơ chế kiểm duyệt đề xuất (BR-07 & SRS D2)</span>
            </div>

            <h1 className="font-headline text-3xl lg:text-4xl font-bold text-primary tracking-tight">
              Kênh Thread Tìm Trọ Cộng Đồng & So Khớp Tự Động
            </h1>

            <p className="text-sm text-on-surface-variant leading-relaxed">
              Người tìm trọ đăng tiêu chí cụ thể (khu vực, ngân sách, tiện ích mong muốn). Hệ thống TroPro tự động đối soát và chỉ cho phép Chủ trọ / Quản lý gửi đề xuất phòng khi <strong className="text-primary font-bold">TRỐNG và KHỚP 100%</strong> tiêu chí. Cam kết minh bạch, hoàn toàn không trung gian môi giới lãng phí thời gian.
            </p>

            <div className="flex flex-wrap items-center gap-6 pt-2 text-on-surface-variant text-xs font-semibold">
              <div className="flex items-center gap-1.5">
                <span className="material-symbols-outlined text-base text-primary">verified_user</span>
                <span>Không số điện thoại rác</span>
              </div>
              <div className="flex items-center gap-1.5">
                <span className="material-symbols-outlined text-base text-primary">rule</span>
                <span>Chỉ gửi đề xuất khi phòng trống thật</span>
              </div>
              <div className="flex items-center gap-1.5">
                <span className="material-symbols-outlined text-base text-primary">speed</span>
                <span>Phản hồi trung bình 2.4 giờ</span>
              </div>
            </div>
          </div>

          {/* Action CTAs */}
          <div className="flex flex-col sm:flex-row lg:flex-col gap-3 shrink-0">
            <button
              type="button"
              onClick={onOpenCreateThread}
              className="px-6 py-3.5 rounded-2xl bg-secondary-container hover:bg-secondary text-white font-headline text-sm font-bold shadow-md hover:shadow-lg transition-all flex items-center justify-center gap-2 cursor-pointer active:scale-95"
            >
              <span className="material-symbols-outlined text-xl">add_circle</span>
              <span>+ Đăng Thread tìm trọ mới</span>
            </button>

            <a
              href="#rules-widget"
              className="px-4 py-2.5 rounded-xl bg-surface-container-lowest text-tertiary text-xs font-bold shadow-xs hover:bg-surface-container transition-colors flex items-center justify-center gap-1.5 border border-surface-container"
            >
              <span className="material-symbols-outlined text-base text-primary">policy</span>
              <span>Xem quy tắc đề xuất & bảo mật</span>
            </a>
          </div>
        </div>
      </section>

      {/* Filter & Search Bar Section */}
      <section className="max-w-7xl mx-auto w-full px-6 mt-6">
        <div className="bg-surface-container-lowest p-4 rounded-2xl shadow-xs border border-surface-container flex flex-col gap-4">
          <div className="grid grid-cols-1 md:grid-cols-12 gap-3 items-center">
            {/* Search input */}
            <div className="md:col-span-5 relative">
              <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline text-lg">
                search
              </span>
              <input
                type="text"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                placeholder="Tìm theo khu vực, trường ĐH (Hutech, UEF, Ngoại Thương, Bách Khoa...)"
                className="w-full bg-surface-container pl-9 pr-3 py-2 rounded-xl text-xs text-on-surface focus:outline-none focus:ring-2 focus:ring-primary border-0"
              />
            </div>

            {/* Area */}
            <div className="md:col-span-3 relative">
              <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline text-base">
                location_on
              </span>
              <select
                value={selectedDistrict}
                onChange={(e) => setSelectedDistrict(e.target.value)}
                className="w-full bg-surface-container pl-9 pr-7 py-2 rounded-xl text-xs text-on-surface font-semibold focus:outline-none focus:ring-2 focus:ring-primary border-0 appearance-none cursor-pointer"
              >
                <option value="binh-thanh">Bình Thạnh (P.25, 26, D2)</option>
                <option value="quan-1">Quận 1 (Trung tâm)</option>
                <option value="quan-3">Quận 3 & Phú Nhuận</option>
                <option value="thu-duc">TP. Thủ Đức (Linh Trung)</option>
              </select>
              <span className="material-symbols-outlined absolute right-2.5 top-1/2 -translate-y-1/2 text-outline text-base pointer-events-none">
                expand_more
              </span>
            </div>

            {/* Budget */}
            <div className="md:col-span-2 relative">
              <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline text-base">
                payments
              </span>
              <select
                value={selectedBudget}
                onChange={(e) => setSelectedBudget(e.target.value)}
                className="w-full bg-surface-container pl-9 pr-7 py-2 rounded-xl text-xs text-on-surface font-semibold focus:outline-none focus:ring-2 focus:ring-primary border-0 appearance-none cursor-pointer"
              >
                <option value="all">Ngân sách max</option>
                <option value="3.5">Dưới 3.5 Triệu</option>
                <option value="5.5">Dưới 5.5 Triệu</option>
                <option value="7.5">Dưới 7.5 Triệu</option>
              </select>
              <span className="material-symbols-outlined absolute right-2.5 top-1/2 -translate-y-1/2 text-outline text-base pointer-events-none">
                expand_more
              </span>
            </div>

            {/* Search CTA */}
            <div className="md:col-span-2">
              <button
                type="button"
                className="w-full py-2 px-3 rounded-xl bg-primary text-white text-xs font-bold shadow-xs hover:bg-primary-container transition-colors flex items-center justify-center gap-1 cursor-pointer"
              >
                <span className="material-symbols-outlined text-base">filter_alt</span>
                <span>Lọc kết quả</span>
              </button>
            </div>
          </div>

          {/* Quick Status Tabs */}
          <div className="flex items-center gap-2 overflow-x-auto pt-1">
            <span className="text-xs text-on-surface-variant font-semibold shrink-0 mr-1 flex items-center gap-1">
              <span className="material-symbols-outlined text-sm">tune</span>
              <span>Trạng thái:</span>
            </span>

            <button
              type="button"
              onClick={() => setStatusFilter('all')}
              className={`px-3 py-1 rounded-full text-xs font-bold transition-all shrink-0 cursor-pointer ${
                statusFilter === 'all'
                  ? 'bg-primary text-white shadow-xs'
                  : 'bg-surface-container text-on-surface-variant hover:bg-surface-container-high'
              }`}
            >
              Tất cả Thread (42)
            </button>

            <button
              type="button"
              onClick={() => setStatusFilter('urgent')}
              className={`px-3 py-1 rounded-full text-xs font-bold transition-all shrink-0 cursor-pointer flex items-center gap-1.5 ${
                statusFilter === 'urgent'
                  ? 'bg-primary text-white shadow-xs'
                  : 'bg-surface-container text-on-surface-variant hover:bg-surface-container-high'
              }`}
            >
              <span className="w-2 h-2 rounded-full bg-secondary"></span>
              <span>Đang mở & Cần phòng gấp (16)</span>
            </button>

            <button
              type="button"
              onClick={() => setStatusFilter('matched')}
              className={`px-3 py-1 rounded-full text-xs font-bold transition-all shrink-0 cursor-pointer flex items-center gap-1.5 ${
                statusFilter === 'matched'
                  ? 'bg-primary text-white shadow-xs'
                  : 'bg-surface-container text-on-surface-variant hover:bg-surface-container-high'
              }`}
            >
              <span className="w-2 h-2 rounded-full bg-primary"></span>
              <span>Đã nhận đề xuất khớp 100% (19)</span>
            </button>
          </div>
        </div>
      </section>

      {/* 2-Column Content Layout (8 cols left / 4 cols right) */}
      <section className="max-w-7xl mx-auto w-full px-6 mt-6">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
          {/* LEFT COLUMN: THREAD LIST (8 cols) */}
          <div className="lg:col-span-8 flex flex-col gap-4">
            <div className="flex items-center justify-between px-1">
              <div className="flex items-center gap-2">
                <h2 className="font-headline text-lg font-bold text-on-surface">
                  Thread tìm trọ đang thảo luận sôi nổi
                </h2>
                <span className="px-2 py-0.5 rounded-full bg-surface-container text-tertiary text-xs font-bold">
                  Live Feed
                </span>
              </div>
              <span className="text-xs text-on-surface-variant">Cập nhật 2 phút trước</span>
            </div>

            {filteredThreads.map((thread) => (
              <article
                key={thread.id}
                className="bg-surface-container-lowest rounded-2xl p-5 shadow-xs border border-surface-container hover:shadow-md transition-all flex flex-col gap-4 relative overflow-hidden"
              >
                <div className="absolute top-0 left-0 w-1.5 h-full bg-primary"></div>

                {/* Header */}
                <div className="flex items-start justify-between gap-3 pl-2">
                  <div className="flex items-center gap-3">
                    <img
                      src={thread.authorAvatar}
                      alt={thread.authorName}
                      className="w-12 h-12 rounded-full object-cover shadow-sm ring-2 ring-primary/20"
                    />
                    <div>
                      <div className="flex items-center gap-2">
                        <h3 className="font-headline text-sm font-bold text-on-surface">
                          {thread.authorName}
                        </h3>
                        {thread.isVerified && (
                          <span className="material-symbols-outlined text-primary text-base" title="CCCD đã xác thực">
                            verified
                          </span>
                        )}
                        <span className="px-2 py-0.5 rounded bg-secondary-container/10 text-secondary text-[11px] font-bold">
                          {thread.authorRole}
                        </span>
                      </div>
                      <div className="flex items-center gap-2 text-xs text-on-surface-variant mt-0.5">
                        <span>{thread.timeAgo}</span>
                        <span>•</span>
                        <span className="flex items-center gap-1 text-primary font-medium">
                          <span className="material-symbols-outlined text-sm">pin_drop</span>
                          <span>{thread.location}</span>
                        </span>
                      </div>
                    </div>
                  </div>

                  {thread.matchPercent === 100 && (
                    <div className="shrink-0 flex items-center gap-1.5 px-3 py-1 rounded-full bg-primary/10 text-primary text-xs font-bold">
                      <span className="material-symbols-outlined text-base">verified</span>
                      <span>1 Đề xuất khớp 100%</span>
                    </div>
                  )}
                </div>

                {/* Content */}
                <p className="text-xs text-on-surface pl-2 leading-relaxed">
                  "{thread.content}"
                </p>

                {/* Criteria Tags Grid */}
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 pl-2">
                  <div className="p-2.5 rounded-xl bg-surface-container flex flex-col">
                    <span className="text-[10px] text-tertiary flex items-center gap-1 font-semibold">
                      <span className="material-symbols-outlined text-xs">payments</span>
                      <span>Ngân sách tối đa</span>
                    </span>
                    <span className="font-headline text-xs font-bold text-secondary mt-0.5 tabular-nums">
                      {thread.maxBudget.toLocaleString()} đ
                    </span>
                  </div>

                  <div className="p-2.5 rounded-xl bg-surface-container flex flex-col">
                    <span className="text-[10px] text-tertiary flex items-center gap-1 font-semibold">
                      <span className="material-symbols-outlined text-xs">square_foot</span>
                      <span>Diện tích tối thiểu</span>
                    </span>
                    <span className="font-headline text-xs font-bold text-on-surface mt-0.5">
                      ≥ {thread.minArea} m²
                    </span>
                  </div>

                  <div className="p-2.5 rounded-xl bg-surface-container flex flex-col">
                    <span className="text-[10px] text-tertiary flex items-center gap-1 font-semibold">
                      <span className="material-symbols-outlined text-xs">calendar_today</span>
                      <span>Ngày dọn vào</span>
                    </span>
                    <span className="font-headline text-xs font-bold text-on-surface mt-0.5">
                      {thread.moveInDate}
                    </span>
                  </div>

                  <div className="p-2.5 rounded-xl bg-surface-container flex flex-col">
                    <span className="text-[10px] text-tertiary flex items-center gap-1 font-semibold">
                      <span className="material-symbols-outlined text-xs">person</span>
                      <span>Số người ở</span>
                    </span>
                    <span className="font-headline text-xs font-bold text-on-surface mt-0.5 truncate">
                      {thread.occupants}
                    </span>
                  </div>
                </div>

                {/* Mandatory Amenities */}
                <div className="flex flex-wrap items-center gap-1.5 pl-2">
                  <span className="text-[11px] text-on-surface-variant mr-1 font-medium">Tiện ích bắt buộc:</span>
                  {thread.amenities.map((am) => (
                    <span
                      key={am}
                      className="px-2 py-0.5 rounded-md bg-primary-container text-white text-[11px] font-semibold inline-flex items-center gap-1"
                    >
                      <span className="material-symbols-outlined text-xs">check</span>
                      <span>{am}</span>
                    </span>
                  ))}
                </div>

                {/* Verified Proposal Card if matched (BR-07) */}
                {thread.matchedRoom && (
                  <div className="ml-2 p-3 rounded-2xl bg-surface-container-low flex flex-col sm:flex-row items-center justify-between gap-3 border border-surface-container">
                    <div className="flex items-center gap-3">
                      <img
                        src={thread.matchedRoom.image}
                        alt={thread.matchedRoom.roomNumber}
                        className="w-20 h-16 rounded-xl object-cover shrink-0 shadow-xs"
                      />
                      <div className="space-y-0.5">
                        <div className="flex items-center gap-1.5">
                          <span className="px-1.5 py-0.2 rounded bg-primary text-white text-[9px] font-bold tracking-wide uppercase">
                            Phòng Trống Xác Thực
                          </span>
                          <span className="text-xs font-bold text-on-surface">
                            Căn Hộ Studio P.26 Ban Công Lộng Gió
                          </span>
                        </div>
                        <p className="text-[11px] text-on-surface-variant">
                          Chủ cơ sở: <strong className="text-tertiary">An Lộc House</strong> • Mã phòng: <span className="font-mono text-primary font-bold">AL-302</span>
                        </p>
                        <div className="flex items-center gap-3 text-xs text-primary font-bold">
                          <span>Giá: {thread.matchedRoom.price.toLocaleString()} đ/tháng</span>
                          <span>•</span>
                          <span>Diện tích: {thread.matchedRoom.area} m²</span>
                        </div>
                      </div>
                    </div>

                    <button
                      type="button"
                      onClick={() => setExpandedProposalId(expandedProposalId === thread.id ? null : thread.id)}
                      className="px-3.5 py-1.5 rounded-xl bg-primary text-white text-xs font-bold hover:bg-primary-container transition-colors flex items-center gap-1 shrink-0 cursor-pointer"
                    >
                      <span>Xem thẻ đề xuất</span>
                      <span className="material-symbols-outlined text-base">
                        {expandedProposalId === thread.id ? 'expand_less' : 'chevron_right'}
                      </span>
                    </button>
                  </div>
                )}

                {/* Collapsible Proposal Spec */}
                {expandedProposalId === thread.id && (
                  <div className="ml-2 p-3.5 rounded-xl bg-surface-container text-xs flex flex-col gap-2 animate-in fade-in">
                    <div className="flex items-center justify-between font-bold text-primary">
                      <span className="flex items-center gap-1">
                        <span className="material-symbols-outlined text-sm">check_circle</span>
                        Hệ thống TroPro đã đối soát tự động: Khớp 5/5 tiêu chí của {thread.authorName}
                      </span>
                      <span className="text-on-surface-variant font-normal">Hợp đồng điện tử sẵn sàng</span>
                    </div>
                    <p className="text-on-surface leading-relaxed text-[11px]">
                      Chủ trọ cam kết: Tiền cọc chỉ 1 tháng (5.0tr), điện 3.500đ/kWh, nước 25k/m3, wifi miễn phí. Có cổng vân tay và camera an ninh 24/7. Có thể hẹn qua xem phòng trực tiếp ngay hôm nay!
                    </p>
                  </div>
                )}

                {/* Bottom Row */}
                <div className="flex items-center justify-between pt-1 pl-2 text-xs text-on-surface-variant font-semibold">
                  <div className="flex items-center gap-4">
                    <span className="flex items-center gap-1">
                      <span className="material-symbols-outlined text-sm">forum</span>
                      <span>{thread.commentsCount} Bình luận</span>
                    </span>
                    <span className="flex items-center gap-1">
                      <span className="material-symbols-outlined text-sm">visibility</span>
                      <span>{thread.viewsCount} Lượt xem</span>
                    </span>
                    <span className="hidden sm:flex items-center gap-1 text-primary">
                      <span className="material-symbols-outlined text-sm">lock</span>
                      <span>Chỉ quản lý được đề xuất</span>
                    </span>
                  </div>

                  <button
                    type="button"
                    onClick={() => alert(`Xem trọn vẹn thảo luận thread của ${thread.authorName}`)}
                    className="text-primary hover:text-primary-container font-bold flex items-center gap-0.5 cursor-pointer"
                  >
                    <span>Xem toàn bộ thread</span>
                    <span className="material-symbols-outlined text-base">arrow_forward</span>
                  </button>
                </div>
              </article>
            ))}
          </div>

          {/* RIGHT COLUMN: SIDEBAR WIDGETS (4 cols) */}
          <aside className="lg:col-span-4 flex flex-col gap-6" id="rules-widget">
            {/* Widget 1: Strict Rules (BR-07, BR-08, BR-09) */}
            <div className="bg-surface-container-lowest rounded-2xl p-5 shadow-xs border-l-4 border-l-primary border-y border-r border-surface-container flex flex-col gap-3">
              <div className="flex items-center gap-2">
                <div className="w-8 h-8 rounded-full bg-primary/10 flex items-center justify-center text-primary">
                  <span className="material-symbols-outlined text-lg">shield</span>
                </div>
                <div>
                  <h3 className="font-headline text-sm font-bold text-on-surface">Quy tắc đề xuất nghiêm ngặt</h3>
                  <span className="text-[11px] text-primary font-bold">SRS Business Rule: BR-07 & Quyết định D2</span>
                </div>
              </div>

              <p className="text-xs text-on-surface-variant leading-relaxed">
                TroPro bảo vệ tuyệt đối sự yên tĩnh và trải nghiệm tìm phòng của bạn với 3 nguyên tắc bất khả xâm phạm:
              </p>

              <ul className="space-y-2.5 text-xs text-on-surface">
                <li className="flex items-start gap-2">
                  <span className="material-symbols-outlined text-primary text-base shrink-0 mt-0.5">check_circle</span>
                  <span>
                    <strong>Chỉ gửi khi phòng TRỐNG thật:</strong> Hệ thống Back-office tự khóa tính năng gửi nếu phòng chưa có trạng thái "Trống" hoặc đang có hợp đồng hiệu lực.
                  </span>
                </li>

                <li className="flex items-start gap-2">
                  <span className="material-symbols-outlined text-primary text-base shrink-0 mt-0.5">check_circle</span>
                  <span>
                    <strong>Thuật toán so khớp 100% (FR-THR-02):</strong> Ngân sách phòng đề xuất không được vượt quá ngân sách max; đầy đủ 100% tiện ích bắt buộc người thuê yêu cầu.
                  </span>
                </li>

                <li className="flex items-start gap-2">
                  <span className="material-symbols-outlined text-primary text-base shrink-0 mt-0.5">check_circle</span>
                  <span>
                    <strong>Triệt tiêu môi giới (0đ phí):</strong> Chủ nhà và khách tự kết nối trực tiếp, không phí hoa hồng, không chào mời spam số điện thoại.
                  </span>
                </li>
              </ul>
            </div>

            {/* Widget 2: Stats & Highlights */}
            <div className="bg-surface-container-lowest rounded-2xl p-5 shadow-xs border border-surface-container flex flex-col gap-4">
              <div className="flex items-center justify-between">
                <h3 className="font-headline text-sm font-bold text-on-surface flex items-center gap-1.5">
                  <span className="material-symbols-outlined text-secondary text-base">local_fire_department</span>
                  <span>Xu hướng tìm trọ hôm nay</span>
                </h3>
                <span className="text-[11px] text-primary font-bold">TP. Hồ Chí Minh</span>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div className="p-3 rounded-xl bg-surface-container text-center">
                  <span className="font-headline text-2xl font-bold text-primary">18</span>
                  <p className="text-[11px] text-on-surface-variant mt-0.5">Người đăng tìm trọ tại Bình Thạnh hôm nay</p>
                </div>
                <div className="p-3 rounded-xl bg-surface-container text-center">
                  <span className="font-headline text-2xl font-bold text-secondary">95%</span>
                  <p className="text-[11px] text-on-surface-variant mt-0.5">Nhận được đề xuất khớp trong 24h</p>
                </div>
              </div>

              <div className="space-y-1">
                <div className="flex justify-between text-xs text-on-surface-variant font-medium">
                  <span>Mức giá được tìm nhiều nhất: 3.5tr - 5.5tr</span>
                  <span className="font-bold text-primary">68% nhu cầu</span>
                </div>
                <div className="w-full h-2 rounded-full bg-surface-container overflow-hidden">
                  <div className="h-full bg-primary rounded-full" style={{ width: '68%' }}></div>
                </div>
              </div>
            </div>

            {/* Widget 3: For Landlords */}
            <div className="bg-gradient-to-br from-primary to-tertiary rounded-2xl p-5 text-white shadow-md flex flex-col gap-3 relative overflow-hidden">
              <div className="flex items-center gap-2">
                <span className="material-symbols-outlined text-2xl">real_estate_agent</span>
                <h3 className="font-headline text-sm font-bold">Dành cho Chủ trọ & Quản lý</h3>
              </div>

              <p className="text-xs text-white/90 leading-relaxed">
                Dãy trọ của bạn đang có phòng chuẩn bị trả hoặc trống? Hãy đăng nhập hệ thống Quản lý TroPro Manager để gửi đề xuất lấp kín phòng trong vòng 48h.
              </p>

              <button
                type="button"
                onClick={onSwitchToManager}
                className="w-full py-2.5 px-4 rounded-xl bg-white text-primary text-xs font-bold shadow-xs hover:bg-surface-container transition-all flex items-center justify-center gap-1.5 cursor-pointer mt-1"
              >
                <span className="material-symbols-outlined text-base">dashboard</span>
                <span>Vào Back-office Gửi Đề Xuất</span>
              </button>
            </div>
          </aside>
        </div>
      </section>
    </div>
  );
};
