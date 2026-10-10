import React, { useState } from 'react';
import { MaintenanceTicket } from '../../types';

interface MaintenanceViewProps {
  tickets: MaintenanceTicket[];
  onClaimTicket: (ticketId: string) => void;
  onCreateTicket: () => void;
}

export const MaintenanceView: React.FC<MaintenanceViewProps> = ({
  tickets,
  onClaimTicket,
  onCreateTicket
}) => {
  const [activeTab, setActiveTab] = useState<'tickets' | 'assets'>('tickets');
  const [statusFilter, setStatusFilter] = useState<'all' | 'new' | 'in_progress' | 'completed'>('all');

  const filteredTickets = tickets.filter((t) => {
    if (statusFilter === 'new') return t.status === 'new';
    if (statusFilter === 'in_progress') return t.status === 'in_progress';
    if (statusFilter === 'completed') return t.status === 'completed';
    return true;
  });

  return (
    <div className="flex flex-col w-full p-6 lg:p-8 gap-6 max-w-[1600px] mx-auto">
      {/* Top Header */}
      <div className="flex flex-col gap-1">
        <div className="flex items-center gap-1.5 text-xs text-outline font-medium">
          <span>TroPro Sài Gòn</span>
          <span className="material-symbols-outlined text-xs">chevron_right</span>
          <span>Cơ sở Đinh Bộ Lĩnh</span>
          <span className="material-symbols-outlined text-xs">chevron_right</span>
          <span className="text-primary font-bold">CSVC & Sửa chữa</span>
        </div>

        <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4 mt-1">
          <div className="flex items-center gap-3 flex-wrap">
            <h1 className="font-headline text-2xl lg:text-3xl font-bold text-on-surface tracking-tight">
              Quản lý CSVC & Xử lý Yêu cầu Sửa chữa
            </h1>
            <span className="inline-flex items-center gap-1 px-3 py-1 rounded-full bg-primary/10 text-primary text-xs font-bold">
              <span className="material-symbols-outlined text-sm">verified_user</span>
              <span>Quy tắc BR-11: 1 Manager Claim / Ticket</span>
            </span>
          </div>

          <div className="flex items-center gap-2.5 flex-wrap">
            <button
              type="button"
              onClick={() => setActiveTab('assets')}
              className="inline-flex items-center gap-1.5 px-3.5 py-2 rounded-xl bg-surface-container-high text-on-surface text-xs font-semibold hover:bg-surface-container transition-all cursor-pointer"
            >
              <span className="material-symbols-outlined text-base text-tertiary">inventory_2</span>
              <span>Kiểm kê CSVC</span>
            </button>

            <button
              type="button"
              onClick={() => alert('Đang xuất sổ kiểm kê và chi phí sửa chữa ra Excel...')}
              className="inline-flex items-center gap-1.5 px-3.5 py-2 rounded-xl bg-surface-container-high text-on-surface text-xs font-semibold hover:bg-surface-container transition-all cursor-pointer"
            >
              <span className="material-symbols-outlined text-base text-tertiary">file_download</span>
              <span>Xuất báo cáo Excel</span>
            </button>

            <button
              type="button"
              onClick={onCreateTicket}
              className="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl bg-secondary-container text-white text-xs font-bold hover:bg-secondary transition-all shadow-md cursor-pointer active:scale-95"
            >
              <span className="material-symbols-outlined text-base">add_circle</span>
              <span>+ Tạo yêu cầu mới</span>
            </button>
          </div>
        </div>
      </div>

      {/* Tab Controller & Filter Ribbon */}
      <div className="bg-surface-container-lowest p-4 rounded-2xl shadow-xs border border-surface-container flex flex-col gap-4">
        <div className="flex items-center justify-between border-b border-surface-container pb-2">
          <div className="flex items-center gap-6">
            <button
              type="button"
              onClick={() => setActiveTab('tickets')}
              className={`flex items-center gap-2 pb-2 text-xs font-bold transition-all cursor-pointer ${
                activeTab === 'tickets'
                  ? 'text-primary border-b-2 border-primary'
                  : 'text-outline hover:text-on-surface'
              }`}
            >
              <span className="material-symbols-outlined text-base">report_problem</span>
              <span>Yêu cầu sửa chữa</span>
              <span className="px-2 py-0.5 rounded-full bg-secondary-container text-white text-[10px] font-bold">
                {tickets.length}
              </span>
            </button>

            <button
              type="button"
              onClick={() => setActiveTab('assets')}
              className={`flex items-center gap-2 pb-2 text-xs font-bold transition-all cursor-pointer ${
                activeTab === 'assets'
                  ? 'text-primary border-b-2 border-primary'
                  : 'text-outline hover:text-on-surface'
              }`}
            >
              <span className="material-symbols-outlined text-base">kitchen</span>
              <span>Kho & Danh mục CSVC (FR-MGR-11)</span>
              <span className="px-2 py-0.5 rounded-full bg-surface-container-high text-on-surface-variant text-[10px] font-bold">
                148
              </span>
            </button>
          </div>
        </div>

        {/* Quick Filters */}
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div className="flex flex-wrap items-center gap-2 text-xs">
            <button
              type="button"
              onClick={() => setStatusFilter('all')}
              className={`px-3 py-1.5 rounded-xl font-bold transition-all cursor-pointer ${
                statusFilter === 'all'
                  ? 'bg-primary text-white shadow-xs'
                  : 'bg-surface-container-high text-on-surface hover:bg-surface-container'
              }`}
            >
              Tất cả ({tickets.length})
            </button>

            <button
              type="button"
              onClick={() => setStatusFilter('new')}
              className={`px-3 py-1.5 rounded-xl font-bold transition-all cursor-pointer flex items-center gap-1.5 ${
                statusFilter === 'new'
                  ? 'bg-primary text-white shadow-xs'
                  : 'bg-surface-container-high text-on-surface hover:bg-surface-container'
              }`}
            >
              <span>Mới tiếp nhận</span>
              <span className="w-2 h-2 rounded-full bg-secondary-container"></span>
              <span className="text-secondary font-bold">3</span>
            </button>

            <button
              type="button"
              onClick={() => setStatusFilter('in_progress')}
              className={`px-3 py-1.5 rounded-xl font-bold transition-all cursor-pointer flex items-center gap-1.5 ${
                statusFilter === 'in_progress'
                  ? 'bg-primary text-white shadow-xs'
                  : 'bg-surface-container-high text-on-surface hover:bg-surface-container'
              }`}
            >
              <span>Đang xử lý</span>
              <span className="text-primary font-bold">4</span>
            </button>

            <button
              type="button"
              onClick={() => setStatusFilter('completed')}
              className={`px-3 py-1.5 rounded-xl font-bold transition-all cursor-pointer ${
                statusFilter === 'completed'
                  ? 'bg-primary text-white shadow-xs'
                  : 'bg-surface-container-high text-on-surface hover:bg-surface-container'
              }`}
            >
              Hoàn thành (18)
            </button>
          </div>

          <div className="flex items-center gap-2">
            <select className="bg-surface-container-low px-3 py-1.5 rounded-xl text-xs text-on-surface font-semibold focus:outline-none border border-surface-container cursor-pointer">
              <option>Tất cả tầng / phòng</option>
              <option>Tầng 1 (P.101 - P.106)</option>
              <option selected>Tầng 2 (P.201 - P.206)</option>
              <option>Tầng 3 (P.301 - P.306)</option>
              <option>Tầng 4 (P.401 - P.406)</option>
            </select>
          </div>
        </div>
      </div>

      {/* Main Content Area */}
      {activeTab === 'tickets' ? (
        <div className="flex flex-col gap-3">
          {filteredTickets.map((t) => (
            <div
              key={t.id}
              className={`bg-surface-container-lowest p-5 rounded-2xl shadow-xs border transition-all flex flex-col gap-3 ${
                t.status === 'new'
                  ? 'border-secondary-container/40 hover:border-secondary-container'
                  : t.urgency === 'high'
                  ? 'border-l-4 border-l-secondary-container border-surface-container'
                  : 'border-surface-container hover:shadow-md'
              }`}
            >
              <div className="flex items-start justify-between gap-3">
                <div className="flex items-center gap-2 flex-wrap">
                  <span className="text-xs font-bold font-mono text-primary">{t.ticketCode}</span>
                  <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold flex items-center gap-1 ${
                    t.urgency === 'high' ? 'bg-secondary/10 text-secondary' : 'bg-surface-container text-on-surface-variant'
                  }`}>
                    {t.urgency === 'high' && <span className="material-symbols-outlined text-xs">local_fire_department</span>}
                    {t.urgency === 'high' ? 'Khẩn cấp' : 'Bình thường'}
                  </span>
                  <span className={`px-2 py-0.5 rounded-full text-[10px] font-bold ${
                    t.status === 'in_progress'
                      ? 'bg-primary/10 text-primary'
                      : t.status === 'new'
                      ? 'bg-secondary-container/20 text-secondary'
                      : 'bg-surface-container-high text-outline'
                  }`}>
                    {t.statusLabel}
                  </span>
                </div>
                <span className="text-[11px] text-outline flex items-center gap-1">
                  <span className="material-symbols-outlined text-xs">schedule</span>
                  {t.createdAt}
                </span>
              </div>

              <div className="flex flex-col gap-1">
                <div className="flex items-center gap-2 font-headline text-sm font-bold text-on-surface">
                  <span className="px-2 py-0.5 rounded bg-surface-container text-primary text-xs font-bold">
                    {t.roomNumber} (Tầng {t.floor})
                  </span>
                  <span>{t.title}</span>
                </div>
                <p className="text-xs text-on-surface-variant leading-relaxed">
                  {t.description}
                </p>
              </div>

              <div className="mt-1 pt-3 border-t border-surface-container flex items-center justify-between flex-wrap gap-3">
                <div className="flex items-center gap-4 text-xs">
                  <div className="flex items-center gap-1.5">
                    <span className="material-symbols-outlined text-sm text-outline">person</span>
                    <span className="text-on-surface font-semibold">{t.tenantName}</span>
                    <span className="text-outline">({t.tenantPhone})</span>
                  </div>

                  {t.claimedBy && (
                    <div className="flex items-center gap-1 text-primary text-[11px] font-semibold bg-primary/5 px-2.5 py-0.5 rounded-md">
                      <span className="material-symbols-outlined text-xs">verified</span>
                      <span>Claimed: <strong>{t.claimedBy}</strong></span>
                    </div>
                  )}

                  {t.scheduledTime && (
                    <span className="text-tertiary bg-tertiary-fixed/30 px-2 py-0.5 rounded text-[11px] font-semibold">
                      Hẹn thợ: {t.scheduledTime}
                    </span>
                  )}
                </div>

                <div>
                  {t.status === 'new' ? (
                    <button
                      type="button"
                      onClick={() => onClaimTicket(t.id)}
                      className="inline-flex items-center gap-1 px-3.5 py-1.5 rounded-xl bg-secondary-container hover:bg-secondary text-white text-xs font-bold shadow-xs transition-all cursor-pointer active:scale-95"
                    >
                      <span className="material-symbols-outlined text-sm">pan_tool</span>
                      <span>Nhận xử lý ngay (Claim - BR-11)</span>
                    </button>
                  ) : (
                    <button
                      type="button"
                      onClick={() => alert(`Xem chi tiết tiến độ xử lý và hóa đơn thợ sự cố ${t.ticketCode}`)}
                      className="text-xs text-primary font-bold hover:underline cursor-pointer flex items-center gap-1"
                    >
                      <span>Xem chi tiết tiến độ</span>
                      <span className="material-symbols-outlined text-sm">arrow_forward</span>
                    </button>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      ) : (
        /* TAB 2: Asset Inventory Master */
        <div className="bg-surface-container-lowest p-6 rounded-2xl shadow-xs border border-surface-container flex flex-col gap-4">
          <div className="flex items-center justify-between">
            <div>
              <h3 className="font-headline text-base font-bold text-on-surface">
                Danh mục CSVC Đang Quản Lý (FR-MGR-11)
              </h3>
              <p className="text-xs text-on-surface-variant">Tổng 148 tài sản đã gắn mã QR TroPro Asset tag</p>
            </div>
            <button
              type="button"
              onClick={() => alert('Thao tác: Mở form thêm tài sản / thiết bị mới')}
              className="px-3.5 py-1.5 rounded-xl bg-primary text-white text-xs font-bold hover:bg-primary-container transition-colors cursor-pointer"
            >
              + Thêm tài sản thiết bị
            </button>
          </div>

          <div className="overflow-x-auto rounded-xl border border-surface-container">
            <table className="w-full text-left text-xs border-collapse">
              <thead>
                <tr className="bg-surface-container-low text-on-surface font-bold uppercase text-[11px] tracking-wider border-b border-surface-container">
                  <th className="p-3">Mã thiết bị</th>
                  <th className="p-3">Tên CSVC</th>
                  <th className="p-3">Vị trí phòng</th>
                  <th className="p-3">Tình trạng</th>
                  <th className="p-3">Bảo dưỡng gần nhất</th>
                  <th className="p-3 text-right">Thao tác</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-surface-container font-medium text-on-surface">
                <tr className="hover:bg-surface-container-low transition-colors">
                  <td className="p-3 font-mono font-bold text-primary">DK-99120</td>
                  <td className="p-3 font-bold">Máy lạnh Daikin 1.5HP</td>
                  <td className="p-3"><span className="px-2 py-0.5 rounded bg-surface-container text-on-surface font-bold">P.204</span></td>
                  <td className="p-3"><span className="px-2 py-0.5 rounded-full bg-secondary-container/20 text-secondary font-bold">Báo hỏng</span></td>
                  <td className="p-3 text-on-surface-variant">15/07/2024</td>
                  <td className="p-3 text-right"><button className="text-primary hover:underline font-bold cursor-pointer">Chi tiết</button></td>
                </tr>
                <tr className="hover:bg-surface-container-low transition-colors">
                  <td className="p-3 font-mono font-bold text-primary">TL-10492</td>
                  <td className="p-3 font-bold">Tủ lạnh Aqua 180L</td>
                  <td className="p-3"><span className="px-2 py-0.5 rounded bg-surface-container text-on-surface font-bold">P.204</span></td>
                  <td className="p-3"><span className="px-2 py-0.5 rounded-full bg-primary/10 text-primary font-bold">Tốt (98%)</span></td>
                  <td className="p-3 text-on-surface-variant">01/06/2024</td>
                  <td className="p-3 text-right"><button className="text-primary hover:underline font-bold cursor-pointer">Chi tiết</button></td>
                </tr>
                <tr className="hover:bg-surface-container-low transition-colors">
                  <td className="p-3 font-mono font-bold text-primary">MG-44018</td>
                  <td className="p-3 font-bold">Máy giặt Toshiba 8.5kg</td>
                  <td className="p-3"><span className="px-2 py-0.5 rounded bg-surface-container text-on-surface font-bold">P.102</span></td>
                  <td className="p-3"><span className="px-2 py-0.5 rounded-full bg-primary/10 text-primary font-bold">Tốt (95%)</span></td>
                  <td className="p-3 text-on-surface-variant">12/08/2024</td>
                  <td className="p-3 text-right"><button className="text-primary hover:underline font-bold cursor-pointer">Chi tiết</button></td>
                </tr>
                <tr className="hover:bg-surface-container-low transition-colors">
                  <td className="p-3 font-mono font-bold text-primary">NL-00912</td>
                  <td className="p-3 font-bold">Máy nước nóng Ariston 20L</td>
                  <td className="p-3"><span className="px-2 py-0.5 rounded bg-surface-container text-on-surface font-bold">P.305</span></td>
                  <td className="p-3"><span className="px-2 py-0.5 rounded-full bg-primary/10 text-primary font-bold">Tốt (92%)</span></td>
                  <td className="p-3 text-on-surface-variant">20/09/2024</td>
                  <td className="p-3 text-right"><button className="text-primary hover:underline font-bold cursor-pointer">Chi tiết</button></td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Audit Log Footer */}
      <div className="bg-surface-container-lowest p-4 rounded-2xl shadow-xs border border-surface-container flex items-center justify-between flex-wrap gap-4">
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-full bg-primary/10 flex items-center justify-center text-primary">
            <span className="material-symbols-outlined text-base">history</span>
          </div>
          <div className="flex flex-col">
            <span className="text-xs font-bold text-on-surface">Nhật ký Audit Log Quản trị Cơ sở</span>
            <span className="text-[11px] text-on-surface-variant">
              Lịch sử can thiệp, chuyển giao ticket và chi phí đều được đồng bộ thời gian thực theo tiêu chuẩn TroPro Enterprise.
            </span>
          </div>
        </div>

        <div className="flex items-center gap-4 text-xs text-on-surface-variant">
          <span>Phiên đăng nhập: <strong>Trần Minh Tuấn (QL-08)</strong></span>
          <span>•</span>
          <span>Trạng thái máy chủ: <span className="text-primary font-bold">Đồng bộ 100%</span></span>
        </div>
      </div>
    </div>
  );
};
