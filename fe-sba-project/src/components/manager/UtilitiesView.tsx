import React, { useState } from 'react';
import {
  ServiceItem,
  MeterReadingRecord,
  CostProposal,
  AppNotification
} from '../../types';
import {
  initialServices,
  initialMeterReadings,
  initialCostProposals,
  initialNotifications,
  facilityInfo
} from '../../data/mockData';
import { AddServiceModal } from '../modals/AddServiceModal';
import { MeterReadingDetailModal } from '../modals/MeterReadingDetailModal';
import { CostProposalModal } from '../modals/CostProposalModal';
import { SendNotificationModal } from '../modals/SendNotificationModal';

export const UtilitiesView: React.FC = () => {
  // Navigation Tabs
  const [activeTab, setActiveTab] = useState<
    'meter-readings' | 'cost-allocation' | 'services-catalog' | 'notifications' | 'preferences'
  >('meter-readings');

  // Domain State
  const [services, setServices] = useState<ServiceItem[]>(initialServices as ServiceItem[]);
  const [readings, setReadings] = useState<MeterReadingRecord[]>(initialMeterReadings);
  const [proposals, setProposals] = useState<CostProposal[]>(initialCostProposals as CostProposal[]);
  const [notifications, setNotifications] = useState<AppNotification[]>(
    initialNotifications as AppNotification[]
  );

  // Filters
  const [selectedFloor, setSelectedFloor] = useState<number | 'all'>('all');
  const [readingStatusFilter, setReadingStatusFilter] = useState<'all' | 'finalized' | 'recorded' | 'abnormal'>('all');
  const [notifCategoryFilter, setNotifCategoryFilter] = useState<'all' | 'unread' | 'utilities' | 'financial'>('all');
  const [searchQuery, setSearchQuery] = useState('');

  // Modals state
  const [isAddServiceOpen, setIsAddServiceOpen] = useState(false);
  const [editingService, setEditingService] = useState<ServiceItem | null>(null);
  const [selectedReading, setSelectedReading] = useState<MeterReadingRecord | null>(null);
  const [selectedProposal, setSelectedProposal] = useState<CostProposal | null>(null);
  const [isSendNotificationOpen, setIsSendNotificationOpen] = useState(false);
  const [toastMessage, setToastMessage] = useState<string | null>(null);

  // Cost Allocation Interactive Simulator State (BE-04.3)
  const [simRoom, setSimRoom] = useState<'P.204' | 'P.305'>('P.204');
  const [simTotalCharge, setSimTotalCharge] = useState<number>(1053000);
  const [simMembers, setSimMembers] = useState([
    { name: 'Nguyễn Hoàng Long', days: 30 },
    { name: 'Lê Văn Bảo', days: 16 }
  ]);

  const showToast = (msg: string) => {
    setToastMessage(msg);
    setTimeout(() => setToastMessage(null), 3500);
  };

  // ================= METRICS COMPUTATION =================
  const totalServiceCharges = readings.reduce((acc, r) => acc + r.totalAmount, 0);
  const finalizedCount = readings.filter((r) => r.status === 'finalized').length;
  const abnormalCount = readings.filter((r) => r.isAbnormal).length;
  const pendingProposalsCount = proposals.filter((p) => p.status === 'PENDING').length;
  const unreadNotifCount = notifications.filter((n) => !n.read).length;

  // Filtered readings
  const filteredReadings = readings.filter((r) => {
    if (selectedFloor !== 'all' && r.floor !== selectedFloor) return false;
    if (readingStatusFilter === 'finalized' && r.status !== 'finalized') return false;
    if (readingStatusFilter === 'recorded' && r.status !== 'recorded') return false;
    if (readingStatusFilter === 'abnormal' && !r.isAbnormal) return false;
    if (searchQuery && !r.roomNumber.toLowerCase().includes(searchQuery.toLowerCase()) && !r.tenantName.toLowerCase().includes(searchQuery.toLowerCase())) {
      return false;
    }
    return true;
  });

  // Filtered notifications
  const filteredNotifications = notifications.filter((n) => {
    if (notifCategoryFilter === 'unread') return !n.read;
    if (notifCategoryFilter === 'utilities') return n.category === 'utilities';
    if (notifCategoryFilter === 'financial') return n.category === 'financial';
    return true;
  });

  // ================= ACTIONS =================
  const handleUpdateReading = (id: string, field: 'currentElectricity' | 'currentWater', value: number) => {
    setReadings((prev) =>
      prev.map((r) => {
        if (r.id !== id) return r;
        const newElec = field === 'currentElectricity' ? value : r.currentElectricity;
        const newWater = field === 'currentWater' ? value : r.currentWater;
        const elecUsage = Math.max(0, newElec - r.previousElectricity);
        const waterUsage = Math.max(0, newWater - r.previousWater);
        const elecTotal = elecUsage * r.electricityRate;
        const waterTotal = waterUsage * r.waterRate;
        const total = elecTotal + waterTotal + r.fixedServicesTotal;
        const isAbnormal = elecUsage > 250; // trigger alert if > 250 kWh
        return {
          ...r,
          [field]: value,
          electricityUsage: elecUsage,
          waterUsage: waterUsage,
          electricityTotal: elecTotal,
          waterTotal: waterTotal,
          totalAmount: total,
          isAbnormal,
          status: isAbnormal ? 'abnormal' : 'recorded',
          statusLabel: isAbnormal ? 'Bất thường' : 'Đã ghi số'
        };
      })
    );
  };

  const handleFinalizeReading = (id: string) => {
    setReadings((prev) =>
      prev.map((r) =>
        r.id === id ? { ...r, status: 'finalized', statusLabel: 'Đã chốt', isAbnormal: false } : r
      )
    );
    showToast('Đã chốt chỉ số và khóa số liệu cho phòng thành công (P1-77)');
  };

  const handleFinalizeAll = () => {
    setReadings((prev) =>
      prev.map((r) => ({ ...r, status: 'finalized', statusLabel: 'Đã chốt' }))
    );
    showToast('Đã chốt toàn bộ chỉ số điện nước cho 36 phòng!');
  };

  const handleSaveService = (serviceData: Partial<ServiceItem>) => {
    if (editingService) {
      setServices((prev) =>
        prev.map((s) => (s.id === editingService.id ? ({ ...s, ...serviceData } as ServiceItem) : s))
      );
      showToast(`Đã cập nhật dịch vụ ${serviceData.name} thành công!`);
    } else {
      setServices((prev) => [serviceData as ServiceItem, ...prev]);
      showToast(`Đã thêm dịch vụ ${serviceData.name} vào danh mục cơ sở!`);
    }
    setEditingService(null);
  };

  const handleToggleServiceActive = (id: string) => {
    setServices((prev) =>
      prev.map((s) => (s.id === id ? { ...s, active: !s.active } : s))
    );
  };

  const handleApproveProposal = (proposalId: string) => {
    setProposals((prev) =>
      prev.map((p) =>
        p.id === proposalId ? { ...p, status: 'APPROVED', statusLabel: 'Đã duyệt' } : p
      )
    );
    showToast('Đã phê duyệt đề xuất phân bổ chi phí và khóa nghĩa vụ thanh toán (P1-81, P1-83)');
  };

  const handleRejectProposal = (proposalId: string, reason: string) => {
    setProposals((prev) =>
      prev.map((p) =>
        p.id === proposalId ? { ...p, status: 'REJECTED', statusLabel: 'Đã từ chối' } : p
      )
    );
    showToast(`Đã từ chối đề xuất chia phí với lý do: ${reason}`);
  };

  const handleSendNotification = (data: any) => {
    const newNotif: AppNotification = {
      id: `notif-${Date.now()}`,
      recipientUserId: 'all-users',
      recipientName: data.targetScope === 'ALL' ? 'Toàn bộ cư dân' : `Cư dân ${data.targetScope}`,
      type: data.type || 'SERVICE_CHARGE_FINALIZED',
      title: data.title,
      message: data.message,
      read: false,
      createdAt: 'Vừa xong',
      channel: 'IN_APP',
      deliveryStatus: 'SENT',
      category: 'utilities'
    };
    setNotifications([newNotif, ...notifications]);
    showToast(`Đã phát thông báo thành công tới ${newNotif.recipientName}!`);
  };

  const handleMarkNotifRead = (id: string) => {
    setNotifications((prev) =>
      prev.map((n) => (n.id === id ? { ...n, read: true, readAt: 'Vừa xong' } : n))
    );
  };

  const handleMarkAllNotifsRead = () => {
    setNotifications((prev) => prev.map((n) => ({ ...n, read: true, readAt: 'Vừa xong' })));
    showToast('Đã đánh dấu tất cả thông báo là đã đọc!');
  };

  // ================= SIMULATOR CALCULATION (HARE-NIEMEYER) =================
  const totalDays = simMembers.reduce((acc, m) => acc + m.days, 0);
  const calculatedSplits = simMembers.map((m) => {
    const rawShare = totalDays > 0 ? (m.days / totalDays) * simTotalCharge : 0;
    const integerPart = Math.floor(rawShare);
    const remainder = rawShare - integerPart;
    return {
      ...m,
      percentage: totalDays > 0 ? (m.days / totalDays) * 100 : 0,
      integerPart,
      remainder,
      finalAmount: integerPart
    };
  });
  // Distribute remaining VND using Largest Remainder (Hare-Niemeyer)
  const sumIntegers = calculatedSplits.reduce((acc, s) => acc + s.integerPart, 0);
  let leftoverVnd = simTotalCharge - sumIntegers;
  // Sort descending by remainder
  const sortedIndices = calculatedSplits
    .map((s, idx) => ({ idx, remainder: s.remainder }))
    .sort((a, b) => b.remainder - a.remainder);
  for (let i = 0; i < leftoverVnd && i < sortedIndices.length; i++) {
    calculatedSplits[sortedIndices[i].idx].finalAmount += 1;
  }

  return (
    <div className="flex flex-col w-full pb-16 animate-in fade-in duration-300">
      {/* Toast Notification */}
      {toastMessage && (
        <div className="fixed top-20 right-6 z-50 bg-primary text-white px-5 py-3 rounded-2xl shadow-xl text-xs font-bold flex items-center gap-2 animate-in fade-in slide-in-from-top-4">
          <span className="material-symbols-outlined text-base">check_circle</span>
          <span>{toastMessage}</span>
        </div>
      )}

      {/* Sub-Header & Operation Context Ribbon (Matches TroPRO Style from Screenshots) */}
      <div className="px-6 lg:px-8 py-4 bg-surface-container-lowest/90 backdrop-blur-md sticky top-16 z-30 flex flex-col gap-3 shadow-xs border-b border-surface-container">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2 text-xs text-on-surface-variant font-medium">
            <span className="inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full bg-primary/10 text-primary font-bold text-[11px]">
              <span className="w-1.5 h-1.5 rounded-full bg-primary animate-pulse"></span>
              Đang vận hành thời gian thực
            </span>
            <span className="text-outline">|</span>
            <span>Cơ sở: <strong>{facilityInfo.name}</strong></span>
          </div>

          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={() => alert('Đang xuất sổ điện nước & dịch vụ ra file Excel (.xlsx)...')}
              className="px-3 py-1.5 rounded-lg bg-surface-container text-on-surface text-xs font-semibold hover:bg-surface-container-high transition-colors flex items-center gap-1.5 cursor-pointer"
            >
              <span className="material-symbols-outlined text-base text-outline">download</span>
              <span>Xuất Bảng kê Excel</span>
            </button>
            <button
              type="button"
              onClick={handleFinalizeAll}
              className="px-4 py-1.5 rounded-lg bg-primary text-white text-xs font-bold hover:bg-primary-container transition-all cursor-pointer shadow-xs flex items-center gap-1.5"
            >
              <span className="material-symbols-outlined text-base">task_alt</span>
              <span>Chốt toàn bộ cơ sở</span>
            </button>
          </div>
        </div>

        {/* Title and Top Level Actions */}
        <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4 pt-1">
          <div className="flex flex-col gap-0.5">
            <h1 className="font-headline text-2xl lg:text-3xl font-bold text-on-surface tracking-tight">
              Điện nước & Dịch vụ — {facilityInfo.shortName}
            </h1>
            <p className="text-xs text-on-surface-variant font-medium">
              Chốt chỉ số đồng hồ · Phân bổ chi phí phòng ghép (Hare-Niemeyer) · Phát thông báo cư dân
            </p>
          </div>

          <div className="flex items-center gap-2.5 flex-wrap">
            <div className="flex items-center gap-1.5 bg-surface-container px-3 py-1.5 rounded-xl text-xs font-bold text-on-surface">
              <span className="material-symbols-outlined text-base text-primary">calendar_month</span>
              <span>Kỳ tính: Tháng 10/2024</span>
              <span className="material-symbols-outlined text-sm text-outline">expand_more</span>
            </div>

            <button
              type="button"
              onClick={() => {
                setEditingService(null);
                setIsAddServiceOpen(true);
              }}
              className="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl bg-secondary-fixed text-on-secondary-fixed font-bold text-xs hover:bg-secondary-fixed-dim transition-colors shadow-xs cursor-pointer"
            >
              <span className="material-symbols-outlined text-base">add_circle</span>
              <span>+ Thêm dịch vụ mới</span>
            </button>

            <button
              type="button"
              onClick={() => setIsSendNotificationOpen(true)}
              className="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl bg-tertiary text-white font-bold text-xs hover:bg-tertiary-container transition-colors shadow-xs cursor-pointer"
            >
              <span className="material-symbols-outlined text-base">campaign</span>
              <span>+ Phát thông báo</span>
            </button>
          </div>
        </div>
      </div>

      <div className="p-6 lg:p-8 flex flex-col gap-6 max-w-[1600px] mx-auto w-full">
        {/* Summary Metrics Ribbon (Matches BillingView and Overview Screenshots) */}
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 bg-surface-container-lowest p-6 rounded-2xl shadow-xs border border-surface-container">
          {/* Tổng tiền dịch vụ */}
          <div className="flex flex-col gap-1">
            <span className="text-xs text-on-surface-variant font-semibold flex items-center gap-1">
              <span className="material-symbols-outlined text-sm text-primary">payments</span>
              Tổng tiền dịch vụ kỳ 10/2024
            </span>
            <span className="font-headline text-2xl lg:text-3xl font-bold text-primary tracking-tight tabular-nums">
              {(totalServiceCharges / 1000000).toFixed(2).replace('.', ',')} tr
            </span>
            <span className="text-[11px] text-on-surface-variant">
              Bao gồm điện, nước, internet & tiện ích
            </span>
          </div>

          {/* Tiến độ chốt số */}
          <div className="flex flex-col gap-1">
            <span className="text-xs text-on-surface-variant font-semibold flex items-center gap-1">
              <span className="material-symbols-outlined text-sm text-primary">electric_meter</span>
              Tiến độ chốt số đồng hồ
            </span>
            <div className="flex items-baseline gap-1">
              <span className="font-headline text-2xl lg:text-3xl font-bold text-on-surface tracking-tight tabular-nums">
                {finalizedCount}/36 phòng
              </span>
              <span className="text-xs text-primary font-bold">
                ({Math.round((finalizedCount / 36) * 100)}%)
              </span>
            </div>
            <div className="w-full h-2 rounded-full bg-surface-container overflow-hidden mt-1">
              <div
                className="h-full bg-primary rounded-full transition-all duration-500"
                style={{ width: `${(finalizedCount / 36) * 100}%` }}
              ></div>
            </div>
          </div>

          {/* Đề xuất chia tiền phòng ghép */}
          <div className="flex flex-col gap-1">
            <span className="text-xs text-on-surface-variant font-semibold flex items-center gap-1">
              <span className="material-symbols-outlined text-sm text-secondary">call_split</span>
              Đề xuất chia tiền chờ duyệt
            </span>
            <div className="flex items-center gap-2">
              <span className="font-headline text-2xl lg:text-3xl font-bold text-secondary tracking-tight">
                {pendingProposalsCount} đề xuất
              </span>
              {pendingProposalsCount > 0 && (
                <span className="px-2 py-0.5 rounded-full bg-secondary-container/20 text-secondary text-[10px] font-bold">
                  Cần xử lý
                </span>
              )}
            </div>
            <span className="text-[11px] text-on-surface-variant">
              P.204 (Bảo) & P.305 (Hưng)
            </span>
          </div>

          {/* Cảnh báo tiêu thụ bất thường */}
          <div className="flex flex-col gap-1">
            <span className="text-xs text-on-surface-variant font-semibold flex items-center gap-1">
              <span className="material-symbols-outlined text-sm text-error">warning</span>
              Cảnh báo tiêu thụ bất thường
            </span>
            <span className="font-headline text-2xl lg:text-3xl font-bold text-error tracking-tight">
              {abnormalCount} phòng
            </span>
            <span className="text-[11px] text-error font-medium">
              P.404 tăng vọt 340 kWh (+95%)
            </span>
          </div>
        </div>

        {/* Main Tabs Navigation Bar (Rounded TroPRO Pills Style) */}
        <div className="flex items-center gap-2 overflow-x-auto pb-1 border-b border-surface-container">
          <button
            type="button"
            onClick={() => setActiveTab('meter-readings')}
            className={`px-4 py-2.5 rounded-xl text-xs font-bold transition-all flex items-center gap-2 cursor-pointer shrink-0 ${
              activeTab === 'meter-readings'
                ? 'bg-primary text-white shadow-xs'
                : 'bg-surface-container text-on-surface-variant hover:bg-surface-container-high hover:text-on-surface'
            }`}
          >
            <span className="material-symbols-outlined text-base">water_drop</span>
            <span>Chốt điện nước ({readings.length} phòng)</span>
            {abnormalCount > 0 && (
              <span className="w-2 h-2 rounded-full bg-error ring-2 ring-white"></span>
            )}
          </button>

          <button
            type="button"
            onClick={() => setActiveTab('cost-allocation')}
            className={`px-4 py-2.5 rounded-xl text-xs font-bold transition-all flex items-center gap-2 cursor-pointer shrink-0 ${
              activeTab === 'cost-allocation'
                ? 'bg-primary text-white shadow-xs'
                : 'bg-surface-container text-on-surface-variant hover:bg-surface-container-high hover:text-on-surface'
            }`}
          >
            <span className="material-symbols-outlined text-base">call_split</span>
            <span>Phân bổ & Chia tiền phòng ghép</span>
            {pendingProposalsCount > 0 && (
              <span className="px-1.5 py-0.2 rounded-full bg-secondary-container text-white text-[10px] font-bold">
                {pendingProposalsCount}
              </span>
            )}
          </button>

          <button
            type="button"
            onClick={() => setActiveTab('services-catalog')}
            className={`px-4 py-2.5 rounded-xl text-xs font-bold transition-all flex items-center gap-2 cursor-pointer shrink-0 ${
              activeTab === 'services-catalog'
                ? 'bg-primary text-white shadow-xs'
                : 'bg-surface-container text-on-surface-variant hover:bg-surface-container-high hover:text-on-surface'
            }`}
          >
            <span className="material-symbols-outlined text-base">room_service</span>
            <span>Danh mục Dịch vụ & Đơn giá ({services.length})</span>
          </button>

          <button
            type="button"
            onClick={() => setActiveTab('notifications')}
            className={`px-4 py-2.5 rounded-xl text-xs font-bold transition-all flex items-center gap-2 cursor-pointer shrink-0 ${
              activeTab === 'notifications'
                ? 'bg-primary text-white shadow-xs'
                : 'bg-surface-container text-on-surface-variant hover:bg-surface-container-high hover:text-on-surface'
            }`}
          >
            <span className="material-symbols-outlined text-base">notifications</span>
            <span>Trung tâm Thông báo</span>
            {unreadNotifCount > 0 && (
              <span className="px-1.5 py-0.2 rounded-full bg-error text-white text-[10px] font-bold">
                {unreadNotifCount}
              </span>
            )}
          </button>

          <button
            type="button"
            onClick={() => setActiveTab('preferences')}
            className={`px-4 py-2.5 rounded-xl text-xs font-bold transition-all flex items-center gap-2 cursor-pointer shrink-0 ${
              activeTab === 'preferences'
                ? 'bg-primary text-white shadow-xs'
                : 'bg-surface-container text-on-surface-variant hover:bg-surface-container-high hover:text-on-surface'
            }`}
          >
            <span className="material-symbols-outlined text-base">tune</span>
            <span>Cài đặt Định mức & Cảnh báo</span>
          </button>
        </div>

        {/* ================= TAB 1: METER READINGS ================= */}
        {activeTab === 'meter-readings' && (
          <div className="flex flex-col gap-6">
            {/* Filter Ribbons */}
            <div className="bg-surface-container-lowest rounded-2xl p-5 border border-surface-container shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-4">
              {/* Floor Selector Pills */}
              <div className="flex items-center gap-1.5 flex-wrap text-xs">
                <span className="text-on-surface-variant font-bold mr-1">Tầng:</span>
                {[
                  { id: 'all', label: 'Tất cả 36 phòng' },
                  { id: 4, label: 'Tầng 4' },
                  { id: 3, label: 'Tầng 3' },
                  { id: 2, label: 'Tầng 2' },
                  { id: 1, label: 'Tầng 1' }
                ].map((fl) => (
                  <button
                    key={fl.id}
                    type="button"
                    onClick={() => setSelectedFloor(fl.id as any)}
                    className={`px-3 py-1.5 rounded-lg font-bold transition-colors cursor-pointer ${
                      selectedFloor === fl.id
                        ? 'bg-surface-container-highest text-on-surface shadow-xs'
                        : 'text-on-surface-variant hover:bg-surface-container'
                    }`}
                  >
                    {fl.label}
                  </button>
                ))}
              </div>

              {/* Status Filter Pills & Search */}
              <div className="flex items-center gap-3">
                <div className="flex items-center gap-1.5 text-xs">
                  <button
                    type="button"
                    onClick={() => setReadingStatusFilter('all')}
                    className={`px-3 py-1 rounded-md text-[11px] font-bold ${
                      readingStatusFilter === 'all'
                        ? 'bg-primary text-white'
                        : 'bg-surface-container text-on-surface-variant hover:bg-surface-container-high'
                    }`}
                  >
                    Tất cả
                  </button>
                  <button
                    type="button"
                    onClick={() => setReadingStatusFilter('recorded')}
                    className={`px-3 py-1 rounded-md text-[11px] font-bold ${
                      readingStatusFilter === 'recorded'
                        ? 'bg-secondary text-white'
                        : 'bg-surface-container text-on-surface-variant hover:bg-surface-container-high'
                    }`}
                  >
                    Đang ghi
                  </button>
                  <button
                    type="button"
                    onClick={() => setReadingStatusFilter('finalized')}
                    className={`px-3 py-1 rounded-md text-[11px] font-bold ${
                      readingStatusFilter === 'finalized'
                        ? 'bg-primary text-white'
                        : 'bg-surface-container text-on-surface-variant hover:bg-surface-container-high'
                    }`}
                  >
                    Đã chốt
                  </button>
                  <button
                    type="button"
                    onClick={() => setReadingStatusFilter('abnormal')}
                    className={`px-3 py-1 rounded-md text-[11px] font-bold ${
                      readingStatusFilter === 'abnormal'
                        ? 'bg-error text-white'
                        : 'bg-surface-container text-error hover:bg-error/10'
                    }`}
                  >
                    Bất thường ({abnormalCount})
                  </button>
                </div>

                <div className="relative">
                  <span className="material-symbols-outlined absolute left-2.5 top-1/2 -translate-y-1/2 text-outline text-sm">
                    search
                  </span>
                  <input
                    type="text"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    placeholder="Tìm phòng..."
                    className="pl-8 pr-3 py-1.5 text-xs rounded-lg bg-surface-container-low border border-surface-container w-32 focus:w-44 transition-all focus:outline-none focus:ring-1 focus:ring-primary"
                  />
                </div>
              </div>
            </div>

            {/* Meter Readings Table */}
            <div className="bg-surface-container-lowest rounded-2xl border border-surface-container shadow-xs overflow-hidden">
              <div className="overflow-x-auto">
                <table className="w-full text-left text-xs border-collapse">
                  <thead>
                    <tr className="bg-surface-container-low text-on-surface-variant font-bold border-b border-surface-container uppercase text-[11px] tracking-wider">
                      <th className="py-3 px-4">Phòng</th>
                      <th className="py-3 px-4">Đại diện thuê</th>
                      <th className="py-3 px-3 text-center bg-secondary/5 text-secondary">
                        Điện cũ (kWh)
                      </th>
                      <th className="py-3 px-3 text-center bg-secondary/5 text-secondary">
                        Điện mới
                      </th>
                      <th className="py-3 px-3 text-center bg-secondary/5 text-secondary">
                        Tiêu thụ (3.8k)
                      </th>
                      <th className="py-3 px-3 text-center bg-primary/5 text-primary">
                        Nước cũ (m³)
                      </th>
                      <th className="py-3 px-3 text-center bg-primary/5 text-primary">
                        Nước mới
                      </th>
                      <th className="py-3 px-3 text-center bg-primary/5 text-primary">
                        Tiêu thụ (22k)
                      </th>
                      <th className="py-3 px-3 text-right">Dịch vụ cố định</th>
                      <th className="py-3 px-4 text-right">Tổng cộng (VNĐ)</th>
                      <th className="py-3 px-3 text-center">Trạng thái</th>
                      <th className="py-3 px-4 text-right">Thao tác</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-surface-container font-medium text-on-surface">
                    {filteredReadings.map((r) => (
                      <tr
                        key={r.id}
                        className={`hover:bg-surface-container-low/60 transition-colors ${
                          r.isAbnormal ? 'bg-error-container/10' : ''
                        }`}
                      >
                        {/* Room */}
                        <td className="py-3 px-4 font-headline text-sm font-bold text-primary">
                          <div className="flex items-center gap-1.5">
                            <span>{r.roomNumber}</span>
                            {r.meterPhotoUrl && (
                              <span
                                className="material-symbols-outlined text-xs text-primary cursor-pointer"
                                title="Đã có ảnh công tơ"
                              >
                                photo_camera
                              </span>
                            )}
                          </div>
                        </td>

                        {/* Tenant */}
                        <td className="py-3 px-4 text-on-surface font-semibold max-w-[140px] truncate">
                          {r.tenantName}
                        </td>

                        {/* Elec Old */}
                        <td className="py-3 px-3 text-center font-mono text-on-surface-variant bg-secondary/5">
                          {r.previousElectricity}
                        </td>

                        {/* Elec New Input */}
                        <td className="py-3 px-3 text-center bg-secondary/5">
                          {r.status === 'finalized' ? (
                            <span className="font-mono font-bold text-on-surface">
                              {r.currentElectricity}
                            </span>
                          ) : (
                            <input
                              type="number"
                              value={r.currentElectricity}
                              onChange={(e) =>
                                handleUpdateReading(
                                  r.id,
                                  'currentElectricity',
                                  Number(e.target.value)
                                )
                              }
                              className="w-16 px-1.5 py-1 text-center font-mono font-bold text-secondary bg-surface-container-lowest border border-surface-container rounded focus:outline-none focus:ring-1 focus:ring-secondary"
                            />
                          )}
                        </td>

                        {/* Elec Usage */}
                        <td className="py-3 px-3 text-center bg-secondary/5">
                          <span
                            className={`font-mono font-bold ${
                              r.electricityUsage > 250 ? 'text-error animate-pulse' : 'text-secondary'
                            }`}
                          >
                            {r.electricityUsage} kWh
                          </span>
                        </td>

                        {/* Water Old */}
                        <td className="py-3 px-3 text-center font-mono text-on-surface-variant bg-primary/5">
                          {r.previousWater}
                        </td>

                        {/* Water New Input */}
                        <td className="py-3 px-3 text-center bg-primary/5">
                          {r.status === 'finalized' ? (
                            <span className="font-mono font-bold text-on-surface">
                              {r.currentWater}
                            </span>
                          ) : (
                            <input
                              type="number"
                              value={r.currentWater}
                              onChange={(e) =>
                                handleUpdateReading(
                                  r.id,
                                  'currentWater',
                                  Number(e.target.value)
                                )
                              }
                              className="w-16 px-1.5 py-1 text-center font-mono font-bold text-primary bg-surface-container-lowest border border-surface-container rounded focus:outline-none focus:ring-1 focus:ring-primary"
                            />
                          )}
                        </td>

                        {/* Water Usage */}
                        <td className="py-3 px-3 text-center bg-primary/5">
                          <span className="font-mono font-bold text-primary">
                            {r.waterUsage} m³
                          </span>
                        </td>

                        {/* Fixed Services */}
                        <td className="py-3 px-3 text-right text-on-surface-variant font-mono">
                          {r.fixedServicesTotal.toLocaleString()} đ
                        </td>

                        {/* Total Bill */}
                        <td className="py-3 px-4 text-right font-headline text-sm font-bold text-on-surface tabular-nums">
                          {r.totalAmount.toLocaleString()} đ
                        </td>

                        {/* Status */}
                        <td className="py-3 px-3 text-center">
                          <span
                            className={`inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-bold ${
                              r.status === 'finalized'
                                ? 'bg-primary/10 text-primary'
                                : r.isAbnormal
                                ? 'bg-error-container text-on-error-container'
                                : 'bg-secondary-fixed text-on-secondary-fixed'
                            }`}
                          >
                            {r.statusLabel}
                          </span>
                        </td>

                        {/* Actions */}
                        <td className="py-3 px-4 text-right">
                          <div className="flex items-center justify-end gap-1.5">
                            <button
                              type="button"
                              onClick={() => setSelectedReading(r)}
                              className="p-1 rounded bg-surface-container text-primary hover:bg-primary hover:text-white transition-colors cursor-pointer"
                              title="Xem chi tiết & ảnh công tơ"
                            >
                              <span className="material-symbols-outlined text-sm">visibility</span>
                            </button>
                            {r.status !== 'finalized' && (
                              <button
                                type="button"
                                onClick={() => handleFinalizeReading(r.id)}
                                className="px-2 py-1 rounded bg-primary/10 text-primary hover:bg-primary hover:text-white font-bold text-[11px] transition-colors cursor-pointer"
                              >
                                Chốt
                              </button>
                            )}
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              {/* Table Footer */}
              <div className="p-4 bg-surface-container-low/50 border-t border-surface-container flex flex-col sm:flex-row sm:items-center justify-between text-xs text-on-surface-variant gap-2">
                <span className="flex items-center gap-1.5">
                  <span className="material-symbols-outlined text-sm text-primary">info</span>
                  Đơn giá niêm yết: Điện <strong>3.800 đ/kWh</strong> · Nước <strong>22.000 đ/m³</strong>. Số liệu chốt sẽ tự động đồng bộ sang Hóa đơn (Student 5).
                </span>
                <span className="font-bold text-on-surface">
                  Hiển thị {filteredReadings.length}/36 phòng
                </span>
              </div>
            </div>
          </div>
        )}

        {/* ================= TAB 2: COST ALLOCATION (BE-04.3) ================= */}
        {activeTab === 'cost-allocation' && (
          <div className="flex flex-col gap-8">
            {/* 1. Pending Tenant Proposals Section */}
            <div className="flex flex-col gap-4">
              <div className="flex items-center justify-between">
                <div>
                  <h2 className="font-headline text-lg font-bold text-on-surface flex items-center gap-2">
                    <span>Đề xuất chia tiền phòng ghép chờ duyệt</span>
                    {pendingProposalsCount > 0 && (
                      <span className="px-2 py-0.5 rounded-full bg-secondary text-white text-xs font-bold">
                        {pendingProposalsCount}
                      </span>
                    )}
                  </h2>
                  <p className="text-xs text-on-surface-variant">
                    Khách thuê gửi yêu cầu chia lại tiền điện nước theo ngày ở thực tế hoặc thỏa thuận riêng (BE-04.3 / [P1-80], [P1-81])
                  </p>
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {proposals.map((prop) => (
                  <div
                    key={prop.id}
                    className="p-6 rounded-2xl bg-surface-container-lowest border border-surface-container shadow-xs flex flex-col justify-between gap-5 hover:shadow-md transition-shadow"
                  >
                    <div className="flex flex-col gap-3">
                      {/* Top Proposer Ribbon */}
                      <div className="flex items-start justify-between">
                        <div className="flex items-center gap-3">
                          <img
                            src={prop.proposedByAvatar}
                            alt={prop.proposedBy}
                            className="w-11 h-11 rounded-full object-cover ring-2 ring-primary/20"
                          />
                          <div>
                            <div className="flex items-center gap-2">
                              <span className="font-bold text-sm text-on-surface">
                                {prop.proposedBy}
                              </span>
                              <span className="px-2 py-0.5 rounded-md bg-primary/10 text-primary font-bold text-xs">
                                {prop.roomNumber}
                              </span>
                            </div>
                            <span className="text-[11px] text-on-surface-variant">
                              {prop.createdAt} · Phương thức: <strong>{prop.method}</strong>
                            </span>
                          </div>
                        </div>

                        <span
                          className={`px-2.5 py-1 rounded-full text-xs font-bold ${
                            prop.status === 'APPROVED'
                              ? 'bg-primary/10 text-primary'
                              : prop.status === 'REJECTED'
                              ? 'bg-error-container text-on-error-container'
                              : 'bg-secondary-fixed text-on-secondary-fixed'
                          }`}
                        >
                          {prop.statusLabel}
                        </span>
                      </div>

                      {/* Reason */}
                      <div className="p-3 rounded-xl bg-surface-container-low text-xs italic text-on-surface border border-surface-container">
                        "{prop.reason}"
                      </div>

                      {/* Members Splitting Breakdown */}
                      <div className="flex flex-col gap-1.5 pt-1">
                        <span className="text-[11px] font-bold text-on-surface-variant uppercase tracking-wider">
                          Phân bổ chi tiết (Tổng: {prop.totalAmount.toLocaleString()} đ)
                        </span>
                        <div className="divide-y divide-surface-container rounded-xl border border-surface-container bg-surface-container-lowest overflow-hidden">
                          {prop.members.map((m, idx) => (
                            <div key={idx} className="p-2.5 flex items-center justify-between text-xs">
                              <div className="flex items-center gap-2">
                                <span className="font-bold text-on-surface">{m.name}</span>
                                <span className="text-[11px] text-on-surface-variant">
                                  ({m.residentDays} ngày · {m.percentage.toFixed(1)}%)
                                </span>
                              </div>
                              <span className="font-headline font-bold text-secondary tabular-nums">
                                {m.allocatedAmount.toLocaleString()} đ
                              </span>
                            </div>
                          ))}
                        </div>
                      </div>
                    </div>

                    {/* Actions */}
                    <div className="flex items-center justify-between pt-3 border-t border-surface-container">
                      <button
                        type="button"
                        onClick={() => setSelectedProposal(prop)}
                        className="text-xs text-primary font-bold hover:underline flex items-center gap-1 cursor-pointer"
                      >
                        <span className="material-symbols-outlined text-sm">visibility</span>
                        <span>Xem chi tiết đối soát</span>
                      </button>

                      {prop.status === 'PENDING' && (
                        <div className="flex items-center gap-2">
                          <button
                            type="button"
                            onClick={() => handleRejectProposal(prop.id, 'Thông tin ngày ở chưa khớp')}
                            className="px-3 py-1.5 rounded-lg bg-surface-container text-error hover:bg-error-container text-xs font-bold transition-colors cursor-pointer"
                          >
                            Từ chối
                          </button>
                          <button
                            type="button"
                            onClick={() => handleApproveProposal(prop.id)}
                            className="px-4 py-1.5 rounded-lg bg-primary text-white hover:bg-primary-container text-xs font-bold transition-all shadow-xs cursor-pointer flex items-center gap-1"
                          >
                            <span className="material-symbols-outlined text-sm">check</span>
                            <span>Duyệt</span>
                          </button>
                        </div>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* 2. Interactive Resident Days Simulator Tool (BE-04.3 Hare-Niemeyer) */}
            <div className="bg-surface-container-lowest rounded-2xl p-6 border border-surface-container shadow-xs flex flex-col gap-6">
              <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-surface-container pb-4">
                <div>
                  <h3 className="font-headline text-base font-bold text-on-surface flex items-center gap-2">
                    <span className="material-symbols-outlined text-primary">calculate</span>
                    <span>Mô phỏng Phân bổ Chi phí theo Ngày cư trú (Largest Remainder)</span>
                  </h3>
                  <p className="text-xs text-on-surface-variant">
                    Thuật toán Hare-Niemeyer phân bổ chính xác từng đồng lẻ VND, hòa thì so UUID ([P1-79])
                  </p>
                </div>

                <div className="flex items-center gap-2">
                  <span className="text-xs font-bold text-on-surface-variant">Chọn phòng:</span>
                  <select
                    value={simRoom}
                    onChange={(e) => {
                      const r = e.target.value as any;
                      setSimRoom(r);
                      if (r === 'P.204') {
                        setSimTotalCharge(1053000);
                        setSimMembers([
                          { name: 'Nguyễn Hoàng Long', days: 30 },
                          { name: 'Lê Văn Bảo', days: 16 }
                        ]);
                      } else {
                        setSimTotalCharge(1495000);
                        setSimMembers([
                          { name: 'Chu Gia Hưng', days: 30 },
                          { name: 'Trần Văn Thái', days: 22 },
                          { name: 'Nguyễn Tiến Dũng', days: 22 }
                        ]);
                      }
                    }}
                    className="p-2 rounded-xl bg-surface-container text-xs font-bold text-primary focus:outline-none cursor-pointer"
                  >
                    <option value="P.204">P.204 (2 người · 1.053.000 đ)</option>
                    <option value="P.305">P.305 (3 người · 1.495.000 đ)</option>
                  </select>
                </div>
              </div>

              {/* Controls */}
              <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                <div className="flex flex-col gap-1">
                  <label className="text-xs font-bold text-on-surface">Tổng tiền dịch vụ cần chia (VNĐ)</label>
                  <input
                    type="number"
                    value={simTotalCharge}
                    onChange={(e) => setSimTotalCharge(Number(e.target.value))}
                    step={1000}
                    className="p-2.5 rounded-xl bg-surface-container-low border border-surface-container font-headline font-bold text-primary text-base focus:outline-none"
                  />
                  <span className="text-[11px] text-on-surface-variant">
                    Lấy từ tổng tiền điện, nước và dịch vụ chốt của phòng
                  </span>
                </div>

                <div className="col-span-2 flex flex-col gap-3">
                  <span className="text-xs font-bold text-on-surface">
                    Điều chỉnh số ngày cư trú thực tế trong tháng:
                  </span>
                  <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
                    {simMembers.map((m, idx) => (
                      <div key={idx} className="p-3 rounded-xl bg-surface-container-low border border-surface-container flex flex-col gap-2">
                        <span className="font-bold text-xs text-on-surface truncate">{m.name}</span>
                        <div className="flex items-center justify-between">
                          <span className="text-[11px] text-on-surface-variant">Ngày ở:</span>
                          <input
                            type="number"
                            min={1}
                            max={31}
                            value={m.days}
                            onChange={(e) => {
                              const val = Math.max(1, Math.min(31, Number(e.target.value)));
                              setSimMembers((prev) =>
                                prev.map((item, i) => (i === idx ? { ...item, days: val } : item))
                              );
                            }}
                            className="w-16 px-2 py-1 text-center font-bold text-primary bg-surface-container-lowest border border-surface-container rounded-lg text-xs"
                          />
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              </div>

              {/* Computed Splitting Result Table */}
              <div className="overflow-x-auto rounded-xl border border-surface-container bg-surface-container-lowest">
                <table className="w-full text-left text-xs border-collapse">
                  <thead>
                    <tr className="bg-surface-container-low text-on-surface-variant font-bold border-b border-surface-container uppercase text-[11px]">
                      <th className="py-3 px-4">Thành viên</th>
                      <th className="py-3 px-4 text-center">Số ngày ở</th>
                      <th className="py-3 px-4 text-center">Tỷ trọng (%)</th>
                      <th className="py-3 px-4 text-right">Tiền cơ bản</th>
                      <th className="py-3 px-4 text-right">Phần dư phân bổ</th>
                      <th className="py-3 px-4 text-right font-bold text-primary">Thực thu chốt (VNĐ)</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-surface-container font-medium text-on-surface">
                    {calculatedSplits.map((item, idx) => (
                      <tr key={idx} className="hover:bg-surface-container-low/50">
                        <td className="py-3 px-4 font-bold text-on-surface">{item.name}</td>
                        <td className="py-3 px-4 text-center font-semibold">{item.days} / 30 ngày</td>
                        <td className="py-3 px-4 text-center font-bold text-primary">
                          {item.percentage.toFixed(2)}%
                        </td>
                        <td className="py-3 px-4 text-right tabular-nums">
                          {item.integerPart.toLocaleString()} đ
                        </td>
                        <td className="py-3 px-4 text-right text-secondary tabular-nums">
                          +{item.finalAmount - item.integerPart} đ (Dư: {item.remainder.toFixed(3)})
                        </td>
                        <td className="py-3 px-4 text-right font-headline text-sm font-bold text-primary tabular-nums">
                          {item.finalAmount.toLocaleString()} đ
                        </td>
                      </tr>
                    ))}
                  </tbody>
                  <tfoot>
                    <tr className="bg-surface-container font-bold text-xs border-t border-surface-container">
                      <td colSpan={2} className="py-3 px-4">Tổng cộng:</td>
                      <td className="py-3 px-4 text-center text-primary">100.00%</td>
                      <td colSpan={2} className="py-3 px-4 text-right text-on-surface-variant">
                        Khớp 100% không lệch 1 đồng:
                      </td>
                      <td className="py-3 px-4 text-right text-base text-primary tabular-nums">
                        {calculatedSplits.reduce((acc, i) => acc + i.finalAmount, 0).toLocaleString()} đ
                      </td>
                    </tr>
                  </tfoot>
                </table>
              </div>

              <div className="flex items-center justify-between text-xs text-on-surface-variant">
                <span>
                  Công thức áp dụng: <code>(Số ngày ở / Tổng ngày ở) × Tổng tiền</code>, làm tròn VND bằng thuật toán Largest Remainder.
                </span>
                <button
                  type="button"
                  onClick={() => showToast(`Đã áp dụng phương án chia tiền cho ${simRoom}!`)}
                  className="px-4 py-2 rounded-xl bg-primary text-white font-bold hover:bg-primary-container transition-all cursor-pointer shadow-xs"
                >
                  Áp dụng phương án này vào Hóa đơn
                </button>
              </div>
            </div>
          </div>
        )}

        {/* ================= TAB 3: SERVICES CATALOG (BE-04.1) ================= */}
        {activeTab === 'services-catalog' && (
          <div className="flex flex-col gap-6">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
              <div>
                <h2 className="font-headline text-lg font-bold text-on-surface">
                  Danh mục Dịch vụ Cơ sở & Đơn giá áp dụng
                </h2>
                <p className="text-xs text-on-surface-variant">
                  Quản lý dịch vụ bắt buộc (Điện, Nước) và dịch vụ tùy chọn đăng ký/hủy (BE-04.1 / [P1-68], [P1-70])
                </p>
              </div>

              <button
                type="button"
                onClick={() => {
                  setEditingService(null);
                  setIsAddServiceOpen(true);
                }}
                className="px-4 py-2 rounded-xl bg-primary text-white font-bold text-xs hover:bg-primary-container transition-all cursor-pointer shadow-xs flex items-center gap-1.5 self-start"
              >
                <span className="material-symbols-outlined text-base">add_circle</span>
                <span>+ Thêm dịch vụ mới</span>
              </button>
            </div>

            {/* Services Grid Cards */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
              {services.map((srv) => (
                <div
                  key={srv.id}
                  className={`p-5 rounded-2xl bg-surface-container-lowest border transition-all flex flex-col justify-between gap-4 shadow-xs ${
                    srv.active
                      ? 'border-surface-container hover:shadow-md'
                      : 'border-outline-variant/30 opacity-60 bg-surface-container-low/40'
                  }`}
                >
                  <div className="flex flex-col gap-3">
                    <div className="flex items-start justify-between">
                      <div className="flex items-center gap-3">
                        <span
                          className={`w-11 h-11 rounded-xl flex items-center justify-center ${
                            srv.serviceType === 'ELECTRICITY'
                              ? 'bg-secondary/10 text-secondary'
                              : srv.serviceType === 'WATER'
                              ? 'bg-primary/10 text-primary'
                              : 'bg-surface-container text-tertiary'
                          }`}
                        >
                          <span className="material-symbols-outlined text-2xl">
                            {srv.icon || 'room_service'}
                          </span>
                        </span>

                        <div className="flex flex-col">
                          <h3 className="font-headline font-bold text-sm text-on-surface">
                            {srv.name}
                          </h3>
                          <span className="text-[11px] font-mono font-medium text-on-surface-variant">
                            {srv.code} · {srv.billingMethod}
                          </span>
                        </div>
                      </div>

                      <div className="flex items-center gap-1">
                        {srv.mandatory && (
                          <span className="px-2 py-0.5 rounded-md bg-secondary/10 text-secondary font-bold text-[10px]">
                            Bắt buộc
                          </span>
                        )}
                        <span
                          className={`px-2 py-0.5 rounded-md text-[10px] font-bold ${
                            srv.active
                              ? 'bg-primary/10 text-primary'
                              : 'bg-surface-container text-on-surface-variant'
                          }`}
                        >
                          {srv.active ? 'Hoạt động' : 'Tạm dừng'}
                        </span>
                      </div>
                    </div>

                    <p className="text-xs text-on-surface-variant min-h-[36px] line-clamp-2">
                      {srv.description || 'Dịch vụ phân bổ tại cơ sở'}
                    </p>

                    {/* Price Ribbon */}
                    <div className="p-3 rounded-xl bg-surface-container-low flex items-center justify-between">
                      <span className="text-[11px] text-on-surface-variant font-medium">Đơn giá áp dụng:</span>
                      <div className="flex items-baseline gap-1">
                        <span className="font-headline text-base font-bold text-primary tabular-nums">
                          {srv.referencePrice > 0
                            ? `${srv.referencePrice.toLocaleString()} đ`
                            : 'Miễn phí'}
                        </span>
                        {srv.referencePrice > 0 && (
                          <span className="text-[11px] text-on-surface-variant font-semibold">
                            / {srv.unit}
                          </span>
                        )}
                      </div>
                    </div>
                  </div>

                  {/* Actions */}
                  <div className="flex items-center justify-between pt-2 border-t border-surface-container text-xs">
                    <button
                      type="button"
                      onClick={() => handleToggleServiceActive(srv.id)}
                      className={`font-semibold cursor-pointer ${
                        srv.active ? 'text-on-surface-variant hover:text-error' : 'text-primary'
                      }`}
                    >
                      {srv.active ? 'Tạm ngưng' : 'Kích hoạt lại'}
                    </button>

                    <button
                      type="button"
                      onClick={() => {
                        setEditingService(srv);
                        setIsAddServiceOpen(true);
                      }}
                      className="px-3 py-1.5 rounded-lg bg-surface-container text-primary font-bold hover:bg-primary hover:text-white transition-colors cursor-pointer"
                    >
                      Chỉnh sửa
                    </button>
                  </div>
                </div>
              ))}
            </div>

            {/* Per-Room Service Configuration Note */}
            <div className="p-5 rounded-2xl bg-surface-container-lowest border border-surface-container shadow-xs flex items-center justify-between">
              <div className="flex items-center gap-3">
                <span className="material-symbols-outlined text-primary text-2xl">tune</span>
                <div>
                  <h4 className="font-headline text-sm font-bold text-on-surface">
                    Cấu hình dịch vụ riêng từng phòng (Room Service Override - [P1-69])
                  </h4>
                  <p className="text-xs text-on-surface-variant">
                    Hỗ trợ đơn giá đặc thù cho các căn hộ có ban công rộng, gác lửng hoặc đăng ký thêm xe máy.
                  </p>
                </div>
              </div>

              <button
                type="button"
                onClick={() => alert('Mở bảng cấu hình giá riêng theo từng phòng!')}
                className="px-4 py-2 rounded-xl bg-surface-container text-on-surface font-bold text-xs hover:bg-surface-container-high transition-colors cursor-pointer"
              >
                Cấu hình theo phòng
              </button>
            </div>
          </div>
        )}

        {/* ================= TAB 4: NOTIFICATIONS CENTER (BE-04.4) ================= */}
        {activeTab === 'notifications' && (
          <div className="flex flex-col gap-6">
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
              <div>
                <h2 className="font-headline text-lg font-bold text-on-surface flex items-center gap-2">
                  <span>Trung tâm Thông báo & Tin tức Cư dân</span>
                  {unreadNotifCount > 0 && (
                    <span className="px-2 py-0.5 rounded-full bg-error text-white text-xs font-bold">
                      {unreadNotifCount} chưa đọc
                    </span>
                  )}
                </h2>
                <p className="text-xs text-on-surface-variant">
                  Hệ thống sự kiện thông báo đa kênh In-App, Zalo OA, SMS (BE-04.4 / [P1-123], [P1-125])
                </p>
              </div>

              <div className="flex items-center gap-2 flex-wrap">
                <button
                  type="button"
                  onClick={handleMarkAllNotifsRead}
                  className="px-3 py-1.5 rounded-xl bg-surface-container text-on-surface text-xs font-bold hover:bg-surface-container-high transition-colors cursor-pointer"
                >
                  Đánh dấu tất cả đã đọc
                </button>

                <button
                  type="button"
                  onClick={() => setIsSendNotificationOpen(true)}
                  className="px-4 py-1.5 rounded-xl bg-primary text-white text-xs font-bold hover:bg-primary-container transition-all cursor-pointer shadow-xs flex items-center gap-1.5"
                >
                  <span className="material-symbols-outlined text-base">campaign</span>
                  <span>+ Phát thông báo mới</span>
                </button>
              </div>
            </div>

            {/* Filter Pills */}
            <div className="flex items-center gap-2 text-xs">
              <button
                type="button"
                onClick={() => setNotifCategoryFilter('all')}
                className={`px-3.5 py-1.5 rounded-full font-bold transition-colors cursor-pointer ${
                  notifCategoryFilter === 'all'
                    ? 'bg-primary text-white shadow-xs'
                    : 'bg-surface-container text-on-surface-variant hover:bg-surface-container-high'
                }`}
              >
                Tất cả ({notifications.length})
              </button>
              <button
                type="button"
                onClick={() => setNotifCategoryFilter('unread')}
                className={`px-3.5 py-1.5 rounded-full font-bold transition-colors cursor-pointer ${
                  notifCategoryFilter === 'unread'
                    ? 'bg-primary text-white shadow-xs'
                    : 'bg-surface-container text-on-surface-variant hover:bg-surface-container-high'
                }`}
              >
                Chưa đọc ({unreadNotifCount})
              </button>
              <button
                type="button"
                onClick={() => setNotifCategoryFilter('utilities')}
                className={`px-3.5 py-1.5 rounded-full font-bold transition-colors cursor-pointer ${
                  notifCategoryFilter === 'utilities'
                    ? 'bg-primary text-white shadow-xs'
                    : 'bg-surface-container text-on-surface-variant hover:bg-surface-container-high'
                }`}
              >
                ⚡ Điện nước & Phí
              </button>
              <button
                type="button"
                onClick={() => setNotifCategoryFilter('financial')}
                className={`px-3.5 py-1.5 rounded-full font-bold transition-colors cursor-pointer ${
                  notifCategoryFilter === 'financial'
                    ? 'bg-primary text-white shadow-xs'
                    : 'bg-surface-container text-on-surface-variant hover:bg-surface-container-high'
                }`}
              >
                💰 Hóa đơn & Thu tiền
              </button>
            </div>

            {/* Notifications Feed */}
            <div className="flex flex-col gap-3">
              {filteredNotifications.map((n) => (
                <div
                  key={n.id}
                  onClick={() => handleMarkNotifRead(n.id)}
                  className={`p-4 rounded-2xl border transition-all flex items-start gap-4 cursor-pointer ${
                    !n.read
                      ? 'bg-surface-container-lowest border-primary/30 shadow-xs ring-1 ring-primary/10'
                      : 'bg-surface-container-low/40 border-surface-container text-on-surface-variant'
                  }`}
                >
                  <span
                    className={`w-10 h-10 rounded-xl flex items-center justify-center shrink-0 ${
                      n.type === 'METER_READING_ABNORMAL'
                        ? 'bg-error-container text-error'
                        : n.type === 'COST_ALLOCATION_PROPOSED'
                        ? 'bg-secondary/10 text-secondary'
                        : n.type === 'SERVICE_CHARGE_FINALIZED'
                        ? 'bg-primary/10 text-primary'
                        : 'bg-surface-container text-tertiary'
                    }`}
                  >
                    <span className="material-symbols-outlined text-xl">
                      {n.type === 'METER_READING_ABNORMAL'
                        ? 'warning'
                        : n.type === 'COST_ALLOCATION_PROPOSED'
                        ? 'call_split'
                        : n.type === 'SERVICE_CHARGE_FINALIZED'
                        ? 'electric_meter'
                        : 'notifications'}
                    </span>
                  </span>

                  <div className="flex flex-col min-w-0 flex-1 gap-1">
                    <div className="flex items-center justify-between gap-2">
                      <div className="flex items-center gap-2">
                        <h4 className={`text-xs font-bold ${!n.read ? 'text-on-surface' : 'text-on-surface-variant'}`}>
                          {n.title}
                        </h4>
                        {!n.read && (
                          <span className="w-2 h-2 rounded-full bg-primary animate-pulse"></span>
                        )}
                      </div>
                      <span className="text-[11px] text-outline font-medium shrink-0">
                        {n.createdAt}
                      </span>
                    </div>

                    <p className="text-xs text-on-surface-variant leading-relaxed">
                      {n.message}
                    </p>

                    <div className="flex items-center gap-3 pt-1 text-[11px] text-outline">
                      <span className="flex items-center gap-1">
                        <span className="material-symbols-outlined text-xs">send</span>
                        <span>Kênh: {n.channel}</span>
                      </span>
                      <span>•</span>
                      <span>Gửi tới: {n.recipientName}</span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* ================= TAB 5: PREFERENCES & THRESHOLDS (BE-04.6 / P2-33) ================= */}
        {activeTab === 'preferences' && (
          <div className="bg-surface-container-lowest rounded-2xl p-6 border border-surface-container shadow-xs flex flex-col gap-6 max-w-3xl">
            <div className="border-b border-surface-container pb-4">
              <h2 className="font-headline text-lg font-bold text-on-surface">
                Cài đặt Định mức Cảnh báo & Tùy chọn Thông báo
              </h2>
              <p className="text-xs text-on-surface-variant">
                Cấu hình phát hiện bất thường điện nước và kênh phát thông báo mặc định (P2-21, P2-33)
              </p>
            </div>

            <div className="flex flex-col gap-5 text-xs">
              <div className="flex flex-col gap-1.5">
                <label className="font-bold text-on-surface">
                  Ngưỡng cảnh báo điện nước tăng đột biến (P2-21)
                </label>
                <div className="flex items-center gap-3">
                  <input
                    type="range"
                    min={20}
                    max={100}
                    defaultValue={50}
                    className="w-64 accent-primary"
                  />
                  <span className="font-bold text-primary font-mono text-sm">+50%</span>
                </div>
                <span className="text-[11px] text-on-surface-variant">
                  Hệ thống sẽ gắn cờ cảnh báo đỏ nếu tiêu thụ tháng này vượt quá 50% so với tháng trước.
                </span>
              </div>

              <div className="grid grid-cols-2 gap-4 pt-2 border-t border-surface-container">
                <div className="flex flex-col gap-1">
                  <label className="font-bold text-on-surface">Ngày chốt số hàng tháng</label>
                  <select defaultValue="25" className="p-2.5 rounded-xl bg-surface-container text-xs font-bold text-on-surface">
                    <option value="20">Ngày 20 hàng tháng</option>
                    <option value="25">Ngày 25 hàng tháng (Mặc định)</option>
                    <option value="28">Ngày 28 hàng tháng</option>
                    <option value="30">Ngày cuối cùng của tháng</option>
                  </select>
                </div>

                <div className="flex flex-col gap-1">
                  <label className="font-bold text-on-surface">Hạn chót đóng tiền dịch vụ</label>
                  <select defaultValue="05" className="p-2.5 rounded-xl bg-surface-container text-xs font-bold text-on-surface">
                    <option value="02">Ngày 02 tháng kế tiếp</option>
                    <option value="05">Ngày 05 tháng kế tiếp (Mặc định)</option>
                    <option value="10">Ngày 10 tháng kế tiếp</option>
                  </select>
                </div>
              </div>

              <div className="flex flex-col gap-2 pt-2 border-t border-surface-container">
                <span className="font-bold text-on-surface">Tùy chọn nhận thông báo (Notification Preferences):</span>
                <label className="flex items-center gap-2 cursor-pointer font-medium">
                  <input type="checkbox" defaultChecked className="w-4 h-4 rounded text-primary accent-primary" />
                  <span>Tự động phát tin nhắn Zalo OA cho cư dân ngay khi chốt số điện nước</span>
                </label>
                <label className="flex items-center gap-2 cursor-pointer font-medium">
                  <input type="checkbox" defaultChecked className="w-4 h-4 rounded text-primary accent-primary" />
                  <span>Gửi thông báo đẩy In-App khi có khách thuê đề xuất chia tiền phòng ghép</span>
                </label>
                <label className="flex items-center gap-2 cursor-pointer font-medium">
                  <input type="checkbox" defaultChecked className="w-4 h-4 rounded text-primary accent-primary" />
                  <span>Bật chuông thông báo đỏ trên thanh tiêu đề khi phát hiện chỉ số bất thường</span>
                </label>
              </div>

              <div className="pt-4 border-t border-surface-container flex justify-end">
                <button
                  type="button"
                  onClick={() => showToast('Đã lưu cấu hình định mức & thông báo thành công!')}
                  className="px-5 py-2.5 rounded-xl bg-primary text-white font-bold text-xs hover:bg-primary-container transition-all cursor-pointer shadow-sm"
                >
                  Lưu cấu hình
                </button>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Modals */}
      <AddServiceModal
        isOpen={isAddServiceOpen}
        onClose={() => setIsAddServiceOpen(false)}
        onSave={handleSaveService}
        initialService={editingService}
      />

      <MeterReadingDetailModal
        isOpen={!!selectedReading}
        onClose={() => setSelectedReading(null)}
        record={selectedReading}
        onFinalize={(id) => {
          handleFinalizeReading(id);
          setSelectedReading(null);
        }}
        onSendAlert={(r) => {
          setIsSendNotificationOpen(true);
        }}
      />

      <CostProposalModal
        isOpen={!!selectedProposal}
        onClose={() => setSelectedProposal(null)}
        proposal={selectedProposal}
        onApprove={(id) => {
          handleApproveProposal(id);
          setSelectedProposal(null);
        }}
        onReject={(id, reason) => {
          handleRejectProposal(id, reason);
          setSelectedProposal(null);
        }}
      />

      <SendNotificationModal
        isOpen={isSendNotificationOpen}
        onClose={() => setIsSendNotificationOpen(false)}
        onSend={handleSendNotification}
      />
    </div>
  );
};
