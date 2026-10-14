import React, { useState } from 'react';
import { CostProposal } from '../../types';

interface CostProposalModalProps {
  isOpen: boolean;
  onClose: () => void;
  proposal: CostProposal | null;
  onApprove: (proposalId: string, note?: string) => void;
  onReject: (proposalId: string, reason: string) => void;
}

export const CostProposalModal: React.FC<CostProposalModalProps> = ({
  isOpen,
  onClose,
  proposal,
  onApprove,
  onReject
}) => {
  const [rejectReason, setRejectReason] = useState('');
  const [isRejecting, setIsRejecting] = useState(false);

  if (!isOpen || !proposal) return null;

  const totalAllocated = proposal.members.reduce((acc, m) => acc + m.allocatedAmount, 0);
  const isMatch = Math.abs(totalAllocated - proposal.totalAmount) <= 1; // Tolerance 1 VND for rounding

  return (
    <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="bg-surface-container-lowest w-full max-w-xl rounded-2xl p-6 shadow-2xl border border-surface-container flex flex-col gap-5 animate-in fade-in zoom-in-95 max-h-[90vh] overflow-y-auto">
        {/* Header */}
        <div className="flex items-start justify-between border-b border-surface-container pb-4">
          <div className="flex items-center gap-3">
            <span className="w-10 h-10 rounded-xl bg-primary/10 text-primary flex items-center justify-center">
              <span className="material-symbols-outlined text-2xl">call_split</span>
            </span>
            <div>
              <div className="flex items-center gap-2">
                <h3 className="font-headline text-base font-bold text-on-surface">
                  Đề xuất Chia phí Cư dân · {proposal.roomNumber}
                </h3>
                <span
                  className={`px-2 py-0.5 rounded-full text-[11px] font-bold ${
                    proposal.status === 'APPROVED'
                      ? 'bg-primary/10 text-primary'
                      : proposal.status === 'REJECTED'
                      ? 'bg-error-container text-on-error-container'
                      : 'bg-secondary-fixed text-on-secondary-fixed'
                  }`}
                >
                  {proposal.statusLabel}
                </span>
              </div>
              <p className="text-xs text-on-surface-variant">
                Người đề xuất: <strong>{proposal.proposedBy}</strong> · {proposal.createdAt} (BE-04.3)
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

        {/* Reason / Explanation Card */}
        <div className="p-4 rounded-xl bg-surface-container-low border border-surface-container flex flex-col gap-2">
          <div className="flex items-center justify-between text-xs">
            <span className="font-bold text-on-surface flex items-center gap-1.5">
              <span className="material-symbols-outlined text-primary text-base">notes</span>
              Lý do & Thỏa thuận phòng ghép
            </span>
            <span className="text-[11px] text-primary font-semibold">
              Phương thức:{' '}
              {proposal.method === 'RESIDENT_DAYS'
                ? 'Số ngày cư trú thực tế (Hare-Niemeyer)'
                : 'Thỏa thuận nội bộ tùy chỉnh'}
            </span>
          </div>
          <p className="text-xs text-on-surface italic leading-relaxed">
            "{proposal.reason}"
          </p>
        </div>

        {/* Members Allocation Breakdown Table */}
        <div className="flex flex-col gap-2">
          <div className="flex items-center justify-between text-xs font-bold text-on-surface">
            <span>Chi tiết phân bổ cho từng cư dân</span>
            <span className="text-primary">
              Tổng tiền phòng: {proposal.totalAmount.toLocaleString()} đ
            </span>
          </div>

          <div className="overflow-x-auto rounded-xl border border-surface-container bg-surface-container-lowest">
            <table className="w-full text-left text-xs border-collapse">
              <thead>
                <tr className="bg-surface-container-low text-on-surface-variant font-bold border-b border-surface-container text-[11px]">
                  <th className="py-2.5 px-3">Cư dân</th>
                  <th className="py-2.5 px-3 text-center">Số ngày ở</th>
                  <th className="py-2.5 px-3 text-center">Tỷ lệ (%)</th>
                  <th className="py-2.5 px-3 text-right">Khoản phải trả</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-surface-container font-medium text-on-surface">
                {proposal.members.map((m, idx) => (
                  <tr key={idx} className="hover:bg-surface-container-low/50">
                    <td className="py-3 px-3 flex items-center gap-2.5">
                      <img
                        src={m.avatar}
                        alt={m.name}
                        className="w-7 h-7 rounded-full object-cover ring-1 ring-primary/20"
                      />
                      <div className="flex flex-col">
                        <span className="font-bold text-on-surface">{m.name}</span>
                        {m.note && (
                          <span className="text-[10px] text-on-surface-variant">{m.note}</span>
                        )}
                      </div>
                    </td>
                    <td className="py-3 px-3 text-center font-semibold">
                      {m.residentDays} ngày
                    </td>
                    <td className="py-3 px-3 text-center font-bold text-primary">
                      {m.percentage.toFixed(1)}%
                    </td>
                    <td className="py-3 px-3 text-right font-headline font-bold text-secondary tabular-nums">
                      {m.allocatedAmount.toLocaleString()} đ
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* Validation Status Ribbon */}
        <div
          className={`p-3.5 rounded-xl border flex items-center justify-between text-xs font-bold ${
            isMatch
              ? 'bg-primary/10 border-primary/20 text-primary'
              : 'bg-error-container/40 border-error/30 text-error'
          }`}
        >
          <div className="flex items-center gap-2">
            <span className="material-symbols-outlined text-base">
              {isMatch ? 'verified' : 'error'}
            </span>
            <span>
              {isMatch
                ? 'Tổng phân bổ khớp 100% hóa đơn gốc (Thuật toán Largest Remainder VND)'
                : 'Cảnh báo: Tổng phân bổ bị lệch so với hóa đơn gốc!'}
            </span>
          </div>
          <span className="font-headline tabular-nums">
            {totalAllocated.toLocaleString()} / {proposal.totalAmount.toLocaleString()} đ
          </span>
        </div>

        {/* Reject reason input */}
        {isRejecting && (
          <div className="flex flex-col gap-2 p-3.5 rounded-xl bg-error-container/20 border border-error/20">
            <label className="text-xs font-bold text-error">
              Lý do từ chối đề xuất (Sẽ gửi thông báo cho khách thuê) *
            </label>
            <textarea
              value={rejectReason}
              onChange={(e) => setRejectReason(e.target.value)}
              placeholder="VD: Ngày công tác chưa khớp với thông báo vắng mặt trên hệ thống..."
              rows={2}
              className="p-2 text-xs rounded-lg border border-surface-container bg-surface-container-lowest focus:outline-none focus:ring-1 focus:ring-error"
            />
          </div>
        )}

        {/* Footer Actions */}
        <div className="flex items-center justify-between pt-2 border-t border-surface-container">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 rounded-xl bg-surface-container text-on-surface-variant text-xs font-semibold hover:bg-surface-container-high transition-colors cursor-pointer"
          >
            Đóng
          </button>

          {proposal.status === 'PENDING' && (
            <div className="flex items-center gap-2">
              {!isRejecting ? (
                <>
                  <button
                    type="button"
                    onClick={() => setIsRejecting(true)}
                    className="px-4 py-2 rounded-xl bg-surface-container text-error text-xs font-bold hover:bg-error-container hover:text-on-error-container transition-colors cursor-pointer flex items-center gap-1"
                  >
                    <span className="material-symbols-outlined text-base">cancel</span>
                    <span>Từ chối</span>
                  </button>
                  <button
                    type="button"
                    onClick={() => {
                      onApprove(proposal.id);
                      onClose();
                    }}
                    className="px-5 py-2 rounded-xl bg-primary text-white text-xs font-bold hover:bg-primary-container transition-all cursor-pointer shadow-sm flex items-center gap-1.5"
                  >
                    <span className="material-symbols-outlined text-base">check_circle</span>
                    <span>Duyệt phương án (Approve)</span>
                  </button>
                </>
              ) : (
                <>
                  <button
                    type="button"
                    onClick={() => setIsRejecting(false)}
                    className="px-3 py-2 rounded-xl bg-surface-container text-on-surface-variant text-xs font-semibold"
                  >
                    Quay lại
                  </button>
                  <button
                    type="button"
                    onClick={() => {
                      if (!rejectReason.trim()) {
                        alert('Vui lòng nhập lý do từ chối!');
                        return;
                      }
                      onReject(proposal.id, rejectReason);
                      onClose();
                    }}
                    className="px-4 py-2 rounded-xl bg-error text-white text-xs font-bold hover:bg-error/90 transition-all cursor-pointer shadow-sm"
                  >
                    Xác nhận từ chối
                  </button>
                </>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
