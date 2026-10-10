import React from 'react';
import { Invoice } from '../../types';
import { facilityInfo } from '../../data/mockData';

interface InvoiceDetailDrawerProps {
  invoice: Invoice | null;
  isOpen: boolean;
  onClose: () => void;
  onConfirmPayment: (invoiceId: string) => void;
  onSendReminder: (invoiceId: string) => void;
}

export const InvoiceDetailDrawer: React.FC<InvoiceDetailDrawerProps> = ({
  invoice,
  isOpen,
  onClose,
  onConfirmPayment,
  onSendReminder
}) => {
  if (!isOpen || !invoice) return null;

  return (
    <div className="fixed inset-0 z-50 overflow-hidden bg-black/40 backdrop-blur-xs flex justify-end">
      <div className="w-full max-w-lg bg-surface-container-lowest h-full shadow-2xl overflow-y-auto flex flex-col justify-between p-6 border-l border-surface-container animate-in slide-in-from-right duration-200">
        <div className="flex flex-col gap-6">
          {/* Drawer Top Header */}
          <div className="flex items-start justify-between pb-4 border-b border-surface-container">
            <div>
              <div className="flex items-center gap-2">
                <span className="font-headline text-xl font-bold text-on-surface">
                  Hóa đơn {invoice.roomNumber}
                </span>
                <span className="text-xs font-semibold px-2.5 py-0.5 rounded-full bg-surface-container text-primary">
                  Kỳ {invoice.month}
                </span>
              </div>
              <p className="text-xs text-on-surface-variant mt-0.5">
                Người đại diện: <strong className="text-on-surface">{invoice.tenantName}</strong>
              </p>
            </div>
            <button
              type="button"
              onClick={onClose}
              className="w-8 h-8 rounded-full bg-surface-container hover:bg-surface-container-high flex items-center justify-center text-on-surface-variant cursor-pointer"
            >
              <span className="material-symbols-outlined text-lg">close</span>
            </button>
          </div>

          {/* Status Badge & Due Date Alert */}
          <div className="p-4 rounded-xl bg-surface-container-low flex items-center justify-between">
            <div className="flex flex-col">
              <span className="text-[11px] text-on-surface-variant uppercase font-semibold">Trạng thái</span>
              <span className="font-headline text-base font-bold text-secondary mt-0.5">
                {invoice.statusLabel}
                {invoice.overdueDays ? ` (Trễ ${invoice.overdueDays} ngày)` : ''}
              </span>
            </div>
            <div className="flex flex-col items-end">
              <span className="text-[11px] text-on-surface-variant uppercase font-semibold">Hạn thanh toán</span>
              <span className="text-xs font-bold text-on-surface mt-0.5">{invoice.dueDate}</span>
            </div>
          </div>

          {/* Fee Itemization */}
          <div className="flex flex-col gap-3">
            <h4 className="text-xs font-bold text-on-surface uppercase tracking-wider flex items-center gap-1.5">
              <span className="material-symbols-outlined text-base text-primary">receipt_long</span>
              Chi tiết các khoản phí
            </h4>

            <div className="space-y-2 text-xs">
              {/* Rent */}
              <div className="p-3 rounded-lg bg-surface-container flex items-center justify-between">
                <div>
                  <span className="font-bold text-on-surface block">Tiền phòng cố định</span>
                  <span className="text-[11px] text-on-surface-variant">Tháng {invoice.month}</span>
                </div>
                <span className="font-bold text-sm text-on-surface tabular-nums">
                  {invoice.baseRent.toLocaleString()} đ
                </span>
              </div>

              {/* Electricity */}
              <div className="p-3 rounded-lg bg-surface-container flex items-center justify-between">
                <div>
                  <span className="font-bold text-on-surface block flex items-center gap-1">
                    <span className="material-symbols-outlined text-sm text-secondary">bolt</span>
                    Điện sinh hoạt (3.800 đ/kWh)
                  </span>
                  <span className="text-[11px] text-on-surface-variant">Tiêu thụ: {invoice.electricityKwh} kWh</span>
                </div>
                <span className="font-bold text-sm text-on-surface tabular-nums">
                  {invoice.electricityCost.toLocaleString()} đ
                </span>
              </div>

              {/* Water */}
              <div className="p-3 rounded-lg bg-surface-container flex items-center justify-between">
                <div>
                  <span className="font-bold text-on-surface block flex items-center gap-1">
                    <span className="material-symbols-outlined text-sm text-primary">water_drop</span>
                    Nước sinh hoạt (22.000 đ/m³)
                  </span>
                  <span className="text-[11px] text-on-surface-variant">Tiêu thụ: {invoice.waterM3} m³</span>
                </div>
                <span className="font-bold text-sm text-on-surface tabular-nums">
                  {invoice.waterCost.toLocaleString()} đ
                </span>
              </div>

              {/* Service */}
              <div className="p-3 rounded-lg bg-surface-container flex items-center justify-between">
                <div>
                  <span className="font-bold text-on-surface block">Wifi + Vệ sinh + Giữ xe</span>
                  <span className="text-[11px] text-on-surface-variant">Khoán cố định tháng</span>
                </div>
                <span className="font-bold text-sm text-on-surface tabular-nums">
                  {invoice.serviceCost.toLocaleString()} đ
                </span>
              </div>
            </div>
          </div>

          {/* Total & Remaining */}
          <div className="p-4 rounded-xl bg-primary/10 border border-primary/20 flex flex-col gap-2">
            <div className="flex items-center justify-between text-xs">
              <span className="text-on-surface-variant font-medium">Tổng hóa đơn:</span>
              <span className="font-bold text-on-surface tabular-nums">{invoice.total.toLocaleString()} đ</span>
            </div>
            <div className="flex items-center justify-between text-xs">
              <span className="text-on-surface-variant font-medium">Đã thanh toán:</span>
              <span className="font-bold text-primary tabular-nums">{invoice.paid.toLocaleString()} đ</span>
            </div>
            <div className="h-px bg-primary/20 my-1"></div>
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold text-on-surface uppercase">Còn lại phải thu:</span>
              <span className="font-headline text-lg font-bold text-secondary tabular-nums">
                {invoice.remaining.toLocaleString()} đ
              </span>
            </div>
          </div>

          {/* VietQR Bank Details */}
          <div className="p-3 rounded-xl bg-surface-container-low border border-surface-container flex items-center gap-3">
            <span className="material-symbols-outlined text-primary text-2xl">qr_code_scanner</span>
            <div className="text-xs">
              <span className="font-bold text-on-surface block">VietQR Động (MBBank)</span>
              <span className="text-on-surface-variant">
                STK: {facilityInfo.bankAccount.accountNumber} • {facilityInfo.bankAccount.accountHolder}
              </span>
            </div>
          </div>
        </div>

        {/* Action Controls */}
        <div className="pt-6 border-t border-surface-container flex flex-col gap-2">
          {invoice.remaining > 0 ? (
            <>
              <button
                type="button"
                onClick={() => onConfirmPayment(invoice.id)}
                className="w-full py-2.5 rounded-xl bg-primary hover:bg-primary-container text-white font-bold text-xs shadow-sm transition-all flex items-center justify-center gap-1.5 cursor-pointer"
              >
                <span className="material-symbols-outlined text-base">check_circle</span>
                <span>Xác nhận đã nhận đủ {invoice.remaining.toLocaleString()} đ</span>
              </button>

              <button
                type="button"
                onClick={() => onSendReminder(invoice.id)}
                className="w-full py-2 rounded-xl bg-surface-container hover:bg-surface-container-high text-secondary font-bold text-xs transition-colors flex items-center justify-center gap-1.5 cursor-pointer"
              >
                <span className="material-symbols-outlined text-base">send</span>
                <span>Gửi thông báo nhắc qua Zalo</span>
              </button>
            </>
          ) : (
            <div className="p-3 rounded-xl bg-primary/10 text-primary text-center font-bold text-xs flex items-center justify-center gap-1.5">
              <span className="material-symbols-outlined text-base">task_alt</span>
              <span>Hóa đơn đã được thanh quyết toán trọn vẹn</span>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
