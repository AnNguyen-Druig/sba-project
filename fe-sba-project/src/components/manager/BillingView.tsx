import React, { useState } from 'react';
import { Invoice } from '../../types';
import { InvoiceDetailDrawer } from '../modals/InvoiceDetailDrawer';

interface BillingViewProps {
  invoices: Invoice[];
  onConfirmInvoicePayment: (invoiceId: string) => void;
  onSendInvoiceReminder: (invoiceId: string) => void;
  onConfirmAllPending: () => void;
  onRemindAllOverdue: () => void;
}

export const BillingView: React.FC<BillingViewProps> = ({
  invoices,
  onConfirmInvoicePayment,
  onSendInvoiceReminder,
  onConfirmAllPending,
  onRemindAllOverdue
}) => {
  const [selectedInvoice, setSelectedInvoice] = useState<Invoice | null>(null);
  const [activeTab, setActiveTab] = useState<'all' | 'pending' | 'overdue' | 'due' | 'paid'>('all');

  const filteredInvoices = invoices.filter((inv) => {
    if (activeTab === 'pending') return inv.status === 'pending_verification';
    if (activeTab === 'overdue') return inv.status === 'overdue';
    if (activeTab === 'due') return inv.status === 'viewed';
    if (activeTab === 'paid') return inv.status === 'paid';
    return true;
  });

  return (
    <div className="flex flex-col w-full p-6 lg:p-8 gap-6 max-w-[1440px] mx-auto">
      {/* Header Context & Quick Actions */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-1">
        <div className="flex flex-col gap-1">
          <h1 className="font-headline text-2xl lg:text-3xl font-bold text-on-surface tracking-tight">
            Thu phí & Đối soát dòng tiền
          </h1>
          <p className="text-xs text-on-surface-variant font-medium">Hạn thanh toán 05/11 · 30 phòng</p>
        </div>

        <div className="flex items-center gap-2.5 flex-wrap">
          <button
            type="button"
            onClick={() => alert('Đang xuất sổ thu phí tháng 10/2024 ra file Excel (.xlsx)...')}
            className="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl bg-surface-container-lowest border border-surface-container hover:bg-surface-container text-on-surface text-xs font-semibold transition-colors shadow-xs cursor-pointer"
          >
            <span className="material-symbols-outlined text-base text-outline">download</span>
            <span>Xuất Excel</span>
          </button>

          <button
            type="button"
            onClick={onRemindAllOverdue}
            className="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl bg-secondary-fixed text-on-secondary-fixed text-xs font-bold hover:bg-secondary-fixed/80 transition-colors shadow-sm cursor-pointer active:scale-95"
          >
            <span className="material-symbols-outlined text-base text-secondary">notification_important</span>
            <span>Nhắc 4 phòng quá hạn</span>
          </button>
        </div>
      </div>

      {/* Summary Metrics Ribbon */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 bg-surface-container-lowest p-6 rounded-2xl shadow-xs border border-surface-container">
        {/* Phải thu */}
        <div className="flex flex-col gap-1">
          <span className="text-xs text-on-surface-variant font-semibold">Phải thu</span>
          <span className="font-headline text-2xl lg:text-3xl font-bold text-on-surface tracking-tight tabular-nums">
            168,45 tr
          </span>
        </div>

        {/* Đã thu */}
        <div className="flex flex-col gap-1">
          <span className="text-xs text-on-surface-variant font-semibold">Đã thu</span>
          <div className="flex items-baseline gap-1">
            <span className="font-headline text-2xl lg:text-3xl font-bold text-primary tracking-tight tabular-nums">
              112,30 tr
            </span>
          </div>
          <div className="flex items-center gap-2 mt-1">
            <div className="w-28 sm:w-36 h-2 rounded-full bg-surface-container overflow-hidden">
              <div className="h-full bg-primary rounded-full" style={{ width: '67%' }}></div>
            </div>
            <span className="text-[11px] text-on-surface-variant font-medium">67% · 20/30 phòng</span>
          </div>
        </div>

        {/* Còn phải thu */}
        <div className="flex flex-col gap-1">
          <span className="text-xs text-on-surface-variant font-semibold">Còn phải thu</span>
          <span className="font-headline text-2xl lg:text-3xl font-bold text-on-surface tracking-tight tabular-nums">
            56,15 tr
          </span>
        </div>

        {/* Quá hạn */}
        <div className="flex flex-col gap-1">
          <span className="text-xs text-on-surface-variant font-semibold">Quá hạn</span>
          <span className="font-headline text-2xl lg:text-3xl font-bold text-secondary tracking-tight">
            4 phòng
          </span>
          <span className="text-[11px] text-on-surface-variant font-medium">23,10 tr</span>
        </div>
      </div>

      {/* Stepper Status Trail */}
      <div className="flex items-center gap-2 flex-wrap text-xs">
        <div className="inline-flex items-center gap-1 px-3.5 py-1.5 rounded-full bg-primary/10 text-primary border border-primary/20 font-bold">
          <span>✓</span>
          <span>Chốt chỉ số 30/30</span>
        </div>
        <span className="text-outline font-semibold">›</span>
        <div className="inline-flex items-center gap-1 px-3.5 py-1.5 rounded-full bg-primary/10 text-primary border border-primary/20 font-bold">
          <span>✓</span>
          <span>Phát hành 30/30</span>
        </div>
        <span className="text-outline font-semibold">›</span>
        <div className="inline-flex items-center gap-1 px-3.5 py-1.5 rounded-full bg-primary-container text-white font-bold shadow-xs">
          <span>Đang thu tiền 20/30</span>
        </div>
        <span className="text-outline font-semibold">›</span>
        <div className="inline-flex items-center gap-1 px-3.5 py-1.5 rounded-full bg-surface-container-high text-on-surface-variant font-medium">
          <span>Khóa sổ tháng</span>
        </div>
      </div>

      {/* Section: Cần xử lý */}
      <div className="flex flex-col gap-3">
        <h2 className="font-headline text-lg font-bold text-on-surface">Cần xử lý ngay</h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {/* Card 1: Tiền đã về, chờ xác nhận */}
          <div className="bg-surface-container-lowest p-5 rounded-2xl border border-surface-container shadow-xs flex flex-col justify-between gap-4 hover:shadow-md transition-shadow">
            <div className="flex flex-col gap-1.5">
              <div className="flex items-center justify-between">
                <span className="font-headline text-sm font-bold text-on-surface">Tiền đã về, chờ xác nhận</span>
                <span className="w-6 h-6 rounded-full bg-secondary-container text-white font-bold text-xs flex items-center justify-center">
                  3
                </span>
              </div>
              <span className="text-xs text-on-surface-variant font-medium">P.204, P.305, P.108 · 17,9 tr</span>
            </div>
            <button
              type="button"
              onClick={onConfirmAllPending}
              className="w-full py-2 px-3 rounded-xl bg-surface-container-low hover:bg-surface-container text-on-surface text-xs font-bold border border-outline-variant/30 transition-colors text-center cursor-pointer"
            >
              Xác nhận cả 3
            </button>
          </div>

          {/* Card 2: Quá hạn */}
          <div className="bg-surface-container-lowest p-5 rounded-2xl border border-surface-container shadow-xs flex flex-col justify-between gap-4 hover:shadow-md transition-shadow">
            <div className="flex flex-col gap-1.5">
              <div className="flex items-center justify-between">
                <span className="font-headline text-sm font-bold text-on-surface">Quá hạn</span>
                <span className="w-6 h-6 rounded-full bg-error text-white font-bold text-xs flex items-center justify-center">
                  4
                </span>
              </div>
              <span className="text-xs text-on-surface-variant font-medium">Lâu nhất: P.201 trễ 6 ngày</span>
            </div>
            <button
              type="button"
              onClick={onRemindAllOverdue}
              className="w-full py-2 px-3 rounded-xl bg-surface-container-low hover:bg-surface-container text-secondary text-xs font-bold border border-outline-variant/30 transition-colors text-center cursor-pointer"
            >
              Gửi nhắc Zalo
            </button>
          </div>

          {/* Card 3: Thanh toán thiếu */}
          <div className="bg-surface-container-lowest p-5 rounded-2xl border border-surface-container shadow-xs flex flex-col justify-between gap-4 hover:shadow-md transition-shadow">
            <div className="flex flex-col gap-1.5">
              <div className="flex items-center justify-between">
                <span className="font-headline text-sm font-bold text-on-surface">Thanh toán thiếu</span>
                <span className="w-6 h-6 rounded-full bg-surface-container-high text-on-surface font-bold text-xs flex items-center justify-center">
                  1
                </span>
              </div>
              <span className="text-xs text-on-surface-variant font-medium">P.102 còn thiếu 573.000 đ</span>
            </div>
            <button
              type="button"
              onClick={() => {
                const inv102 = invoices.find((i) => i.roomNumber === 'P.102');
                if (inv102) setSelectedInvoice(inv102);
              }}
              className="w-full py-2 px-3 rounded-xl bg-surface-container-low hover:bg-surface-container text-on-surface text-xs font-bold border border-outline-variant/30 transition-colors text-center cursor-pointer"
            >
              Xem chi tiết
            </button>
          </div>
        </div>
      </div>

      {/* Filter Tabs & Invoices Table */}
      <div className="flex flex-col gap-3 bg-surface-container-lowest rounded-2xl shadow-xs border border-surface-container p-6">
        {/* Filter Bar */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 border-b border-surface-container pb-4">
          <div className="flex items-center gap-2 flex-wrap text-xs font-medium">
            <button
              type="button"
              onClick={() => setActiveTab('all')}
              className={`px-3.5 py-1.5 rounded-full font-bold transition-all cursor-pointer ${
                activeTab === 'all'
                  ? 'bg-surface-container-highest text-on-surface shadow-xs'
                  : 'text-on-surface-variant hover:text-on-surface hover:bg-surface-container'
              }`}
            >
              Tất cả 30
            </button>

            <button
              type="button"
              onClick={() => setActiveTab('pending')}
              className={`px-3.5 py-1.5 rounded-full font-bold transition-all cursor-pointer ${
                activeTab === 'pending'
                  ? 'bg-surface-container-highest text-on-surface shadow-xs'
                  : 'text-on-surface-variant hover:text-on-surface hover:bg-surface-container'
              }`}
            >
              Chờ xác nhận 3
            </button>

            <button
              type="button"
              onClick={() => setActiveTab('overdue')}
              className={`px-3.5 py-1.5 rounded-full font-bold transition-all cursor-pointer ${
                activeTab === 'overdue'
                  ? 'bg-surface-container-highest text-on-surface shadow-xs'
                  : 'text-on-surface-variant hover:text-on-surface hover:bg-surface-container'
              }`}
            >
              Quá hạn 4
            </button>

            <button
              type="button"
              onClick={() => setActiveTab('due')}
              className={`px-3.5 py-1.5 rounded-full font-bold transition-all cursor-pointer ${
                activeTab === 'due'
                  ? 'bg-surface-container-highest text-on-surface shadow-xs'
                  : 'text-on-surface-variant hover:text-on-surface hover:bg-surface-container'
              }`}
            >
              Chưa đến hạn 3
            </button>

            <button
              type="button"
              onClick={() => setActiveTab('paid')}
              className={`px-3.5 py-1.5 rounded-full font-bold transition-all cursor-pointer ${
                activeTab === 'paid'
                  ? 'bg-surface-container-highest text-on-surface shadow-xs'
                  : 'text-on-surface-variant hover:text-on-surface hover:bg-surface-container'
              }`}
            >
              Đã thu 20
            </button>
          </div>

          <div className="flex items-center gap-1 text-on-surface-variant text-xs cursor-pointer hover:text-on-surface">
            <span>Sắp xếp: mức độ gấp</span>
            <span className="material-symbols-outlined text-base">expand_more</span>
          </div>
        </div>

        {/* Table */}
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse text-xs">
            <thead>
              <tr className="text-on-surface-variant font-bold border-b border-surface-container uppercase text-[11px] tracking-wider">
                <th className="py-3 px-3">Phòng</th>
                <th className="py-3 px-3">Người đại diện</th>
                <th className="py-3 px-3 text-right">Tổng (VNĐ)</th>
                <th className="py-3 px-3 text-right">Còn lại</th>
                <th className="py-3 px-3 text-center">Trạng thái</th>
                <th className="py-3 px-3 text-left">Hạn</th>
                <th className="py-3 px-3 text-right">Thao tác</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-surface-container font-medium text-on-surface">
              {filteredInvoices.map((inv) => (
                <tr
                  key={inv.id}
                  onClick={() => setSelectedInvoice(inv)}
                  className="hover:bg-surface-container-low/60 transition-colors cursor-pointer group"
                >
                  <td className="py-3 px-3 font-headline text-sm font-bold text-primary">
                    {inv.roomNumber}
                  </td>
                  <td className="py-3 px-3 text-on-surface font-semibold">
                    {inv.tenantName}
                  </td>
                  <td className="py-3 px-3 text-right font-bold tabular-nums">
                    {inv.total.toLocaleString()}
                  </td>
                  <td className={`py-3 px-3 text-right font-bold tabular-nums ${
                    inv.remaining > 0 ? 'text-secondary' : 'text-primary'
                  }`}>
                    {inv.remaining.toLocaleString()}
                  </td>
                  <td className="py-3 px-3 text-center">
                    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-[11px] font-bold ${
                      inv.status === 'overdue'
                        ? 'bg-error-container text-on-error-container'
                        : inv.status === 'pending_verification'
                        ? 'bg-secondary-fixed text-on-secondary-fixed'
                        : inv.status === 'partial'
                        ? 'bg-secondary-container/20 text-secondary'
                        : inv.status === 'viewed'
                        ? 'bg-surface-container text-primary'
                        : 'bg-primary/10 text-primary'
                    }`}>
                      {inv.statusLabel}
                    </span>
                  </td>
                  <td className={`py-3 px-3 font-semibold ${
                    inv.overdueDays ? 'text-secondary font-bold' : 'text-on-surface-variant'
                  }`}>
                    {inv.overdueDays ? `Trễ ${inv.overdueDays} ngày` : inv.dueDate}
                  </td>
                  <td className="py-3 px-3 text-right">
                    <button
                      type="button"
                      onClick={(e) => {
                        e.stopPropagation();
                        setSelectedInvoice(inv);
                      }}
                      className="px-2.5 py-1 rounded bg-surface-container text-primary font-bold text-[11px] hover:bg-primary hover:text-white transition-colors"
                    >
                      Chi tiết
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Drawer Hint Footer */}
        <div className="pt-3 pb-1 text-center border-t border-surface-container">
          <p className="text-[11px] text-on-surface-variant italic">
            Bấm vào một dòng để mở drawer chi tiết khoản, lịch sử thanh toán, dòng thời gian nhắc nợ VietQR
          </p>
        </div>
      </div>

      {/* Invoice Detail Drawer */}
      <InvoiceDetailDrawer
        invoice={selectedInvoice}
        isOpen={!!selectedInvoice}
        onClose={() => setSelectedInvoice(null)}
        onConfirmPayment={(id) => {
          onConfirmInvoicePayment(id);
          setSelectedInvoice(null);
        }}
        onSendReminder={(id) => {
          onSendInvoiceReminder(id);
          setSelectedInvoice(null);
        }}
      />
    </div>
  );
};
