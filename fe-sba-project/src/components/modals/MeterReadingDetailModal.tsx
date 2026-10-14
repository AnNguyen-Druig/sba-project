import React from 'react';
import { MeterReadingRecord } from '../../types';

interface MeterReadingDetailModalProps {
  isOpen: boolean;
  onClose: () => void;
  record: MeterReadingRecord | null;
  onFinalize?: (id: string) => void;
  onSendAlert?: (record: MeterReadingRecord) => void;
}

export const MeterReadingDetailModal: React.FC<MeterReadingDetailModalProps> = ({
  isOpen,
  onClose,
  record,
  onFinalize,
  onSendAlert
}) => {
  if (!isOpen || !record) return null;

  return (
    <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="bg-surface-container-lowest w-full max-w-2xl rounded-2xl p-6 shadow-2xl border border-surface-container flex flex-col gap-5 animate-in fade-in zoom-in-95 max-h-[90vh] overflow-y-auto">
        {/* Header */}
        <div className="flex items-start justify-between border-b border-surface-container pb-4">
          <div className="flex items-center gap-3">
            <span className="w-10 h-10 rounded-xl bg-primary/10 text-primary flex items-center justify-center">
              <span className="material-symbols-outlined text-2xl">electric_meter</span>
            </span>
            <div>
              <div className="flex items-center gap-2">
                <h3 className="font-headline text-lg font-bold text-on-surface">
                  Chi tiết Chốt số & Đối soát Điện Nước · {record.roomNumber}
                </h3>
                <span
                  className={`px-2 py-0.5 rounded-full text-[11px] font-bold ${
                    record.status === 'finalized'
                      ? 'bg-primary/10 text-primary'
                      : record.status === 'abnormal'
                      ? 'bg-error-container text-on-error-container'
                      : 'bg-secondary-fixed text-on-secondary-fixed'
                  }`}
                >
                  {record.statusLabel}
                </span>
              </div>
              <p className="text-xs text-on-surface-variant">
                Kỳ tính: <strong>{record.billingMonth}</strong> · Đại diện: {record.tenantName} · Tầng {record.floor}
              </p>
            </div>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="w-8 h-8 rounded-full bg-surface-container flex items-center justify-center text-on-surface-variant hover:bg-surface-container-high transition-colors cursor-pointer"
          >
            <span className="material-symbols-outlined text-sm">close</span>
          </button>
        </div>

        {/* Abnormal Alert Banner (P2-21) */}
        {record.isAbnormal && (
          <div className="p-4 rounded-xl bg-error-container/40 border border-error/30 flex items-start gap-3">
            <span className="material-symbols-outlined text-error text-xl shrink-0 mt-0.5">
              warning
            </span>
            <div className="flex flex-col gap-1">
              <span className="font-bold text-xs text-error">
                Cảnh báo số liệu tăng đột biến (P2-21 - Alert System)
              </span>
              <p className="text-xs text-on-surface">
                {record.abnormalReason ||
                  'Chỉ số tiêu thụ tăng vượt quá 50% so với trung bình 3 tháng gần nhất. Cần đối soát công tơ hoặc cử thợ kiểm tra rò rỉ.'}
              </p>
              {onSendAlert && (
                <button
                  type="button"
                  onClick={() => onSendAlert(record)}
                  className="mt-1 w-fit px-3 py-1 bg-error text-white font-bold text-[11px] rounded-lg hover:bg-error/90 transition-colors flex items-center gap-1 cursor-pointer"
                >
                  <span className="material-symbols-outlined text-sm">notifications</span>
                  <span>Gửi thông báo nhắc kiểm tra cho khách thuê</span>
                </button>
              )}
            </div>
          </div>
        )}

        {/* Detailed Breakdown Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {/* 1. Electricity Box */}
          <div className="p-4 rounded-xl bg-surface-container-low border border-surface-container flex flex-col gap-3">
            <div className="flex items-center justify-between font-bold text-xs text-on-surface">
              <span className="flex items-center gap-1.5 text-secondary">
                <span className="material-symbols-outlined text-base">bolt</span>
                Điện sinh hoạt ({record.electricityRate.toLocaleString()} đ/kWh)
              </span>
              <span className="text-sm font-headline text-secondary">
                {record.electricityTotal.toLocaleString()} đ
              </span>
            </div>

            <div className="grid grid-cols-3 gap-2 p-2.5 rounded-lg bg-surface-container-lowest text-xs text-center">
              <div>
                <span className="text-[10px] text-on-surface-variant block">Chỉ số cũ</span>
                <span className="font-bold font-mono text-on-surface">{record.previousElectricity}</span>
              </div>
              <div>
                <span className="text-[10px] text-on-surface-variant block">Chỉ số mới</span>
                <span className="font-bold font-mono text-primary">{record.currentElectricity}</span>
              </div>
              <div>
                <span className="text-[10px] text-on-surface-variant block">Tiêu thụ</span>
                <span className="font-bold font-mono text-secondary">
                  {record.electricityUsage} kWh
                </span>
              </div>
            </div>

            <div className="text-[11px] text-on-surface-variant flex items-center justify-between">
              <span>Phương thức: Theo công tơ (METERED)</span>
              <span>Lý do: Segment REGULAR</span>
            </div>
          </div>

          {/* 2. Water Box */}
          <div className="p-4 rounded-xl bg-surface-container-low border border-surface-container flex flex-col gap-3">
            <div className="flex items-center justify-between font-bold text-xs text-on-surface">
              <span className="flex items-center gap-1.5 text-primary">
                <span className="material-symbols-outlined text-base">water_drop</span>
                Nước máy ({record.waterRate.toLocaleString()} đ/m³)
              </span>
              <span className="text-sm font-headline text-primary">
                {record.waterTotal.toLocaleString()} đ
              </span>
            </div>

            <div className="grid grid-cols-3 gap-2 p-2.5 rounded-lg bg-surface-container-lowest text-xs text-center">
              <div>
                <span className="text-[10px] text-on-surface-variant block">Chỉ số cũ</span>
                <span className="font-bold font-mono text-on-surface">{record.previousWater}</span>
              </div>
              <div>
                <span className="text-[10px] text-on-surface-variant block">Chỉ số mới</span>
                <span className="font-bold font-mono text-primary">{record.currentWater}</span>
              </div>
              <div>
                <span className="text-[10px] text-on-surface-variant block">Tiêu thụ</span>
                <span className="font-bold font-mono text-primary">{record.waterUsage} m³</span>
              </div>
            </div>

            <div className="text-[11px] text-on-surface-variant flex items-center justify-between">
              <span>Phương thức: Đồng hồ Sawaco (METERED)</span>
              <span>Lý do: Segment REGULAR</span>
            </div>
          </div>
        </div>

        {/* Evidence Photos & Verification (BE-04.2 evidence_refs) */}
        <div className="p-4 rounded-xl bg-surface-container-lowest border border-surface-container flex flex-col gap-3">
          <div className="flex items-center justify-between">
            <span className="font-bold text-xs text-on-surface flex items-center gap-1.5">
              <span className="material-symbols-outlined text-primary text-base">camera_alt</span>
              Ảnh chụp minh chứng đồng hồ (Evidence Refs)
            </span>
            <span className="text-[11px] text-primary font-semibold cursor-pointer hover:underline">
              + Tải ảnh mới
            </span>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div className="relative h-28 rounded-lg overflow-hidden border border-surface-container bg-surface-container-low group">
              <img
                src={
                  record.meterPhotoUrl ||
                  'https://images.unsplash.com/photo-1558494949-ef010cbdcc31?w=400'
                }
                alt="Công tơ điện"
                className="w-full h-full object-cover group-hover:scale-105 transition-transform"
              />
              <div className="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent flex items-end p-2">
                <span className="text-[10px] font-bold text-white flex items-center gap-1">
                  <span className="material-symbols-outlined text-xs">bolt</span>
                  Công tơ điện: {record.currentElectricity} kWh
                </span>
              </div>
            </div>

            <div className="relative h-28 rounded-lg overflow-hidden border border-surface-container bg-surface-container-low group">
              <img
                src={
                  record.waterPhotoUrl ||
                  'https://images.unsplash.com/photo-1544717305-2782549b5136?w=400'
                }
                alt="Đồng hồ nước"
                className="w-full h-full object-cover group-hover:scale-105 transition-transform"
              />
              <div className="absolute inset-0 bg-gradient-to-t from-black/60 to-transparent flex items-end p-2">
                <span className="text-[10px] font-bold text-white flex items-center gap-1">
                  <span className="material-symbols-outlined text-xs">water_drop</span>
                  Đồng hồ nước: {record.currentWater} m³
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* Total Summary Ribbon */}
        <div className="p-4 rounded-xl bg-surface-container flex items-center justify-between">
          <div className="flex flex-col">
            <span className="text-[11px] text-on-surface-variant font-medium">
              Tổng tiền dịch vụ tháng {record.billingMonth}:
            </span>
            <span className="text-[11px] text-on-surface-variant">
              (Điện: {record.electricityTotal.toLocaleString()} đ + Nước: {record.waterTotal.toLocaleString()} đ + Dịch vụ cố định: {record.fixedServicesTotal.toLocaleString()} đ)
            </span>
          </div>
          <span className="font-headline text-xl font-bold text-primary tabular-nums">
            {record.totalAmount.toLocaleString()} đ
          </span>
        </div>

        {/* Footer Actions */}
        <div className="flex items-center justify-between pt-2 border-t border-surface-container">
          <button
            type="button"
            onClick={() => window.print()}
            className="px-3 py-2 rounded-xl bg-surface-container text-on-surface text-xs font-semibold hover:bg-surface-container-high transition-colors flex items-center gap-1 cursor-pointer"
          >
            <span className="material-symbols-outlined text-base">print</span>
            <span>In phiếu đối soát</span>
          </button>

          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 rounded-xl bg-surface-container text-on-surface-variant text-xs font-semibold hover:bg-surface-container-high transition-colors cursor-pointer"
            >
              Đóng
            </button>
            {record.status !== 'finalized' && onFinalize && (
              <button
                type="button"
                onClick={() => {
                  onFinalize(record.id);
                  onClose();
                }}
                className="px-4 py-2 rounded-xl bg-primary text-white text-xs font-bold hover:bg-primary-container transition-all cursor-pointer shadow-sm flex items-center gap-1"
              >
                <span className="material-symbols-outlined text-base">lock</span>
                <span>Chốt số kỳ này (Finalize)</span>
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
