import React, { useState } from 'react';
import { AppNotification } from '../../types';

interface SendNotificationModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSend: (notification: Partial<AppNotification> & { targetScope: string }) => void;
}

export const SendNotificationModal: React.FC<SendNotificationModalProps> = ({
  isOpen,
  onClose,
  onSend
}) => {
  const [targetScope, setTargetScope] = useState<'ALL' | 'FLOOR_4' | 'FLOOR_3' | 'FLOOR_2' | 'FLOOR_1' | 'SPECIFIC'>('ALL');
  const [specificRoom, setSpecificRoom] = useState('P.204');
  const [type, setType] = useState('SERVICE_CHARGE_FINALIZED');
  const [title, setTitle] = useState('Thông báo chốt số điện nước & Kỳ thanh toán tháng 10/2024');
  const [message, setMessage] = useState(
    'Kính gửi Quý cư dân, chỉ số điện nước tháng 10 đã được chốt. Quý cư dân vui lòng kiểm tra công tơ đối chiếu trong ứng dụng TroPro và hoàn tất thanh toán trước ngày 05/11.'
  );
  const [channels, setChannels] = useState({
    inApp: true,
    zalo: true,
    sms: false,
    email: false
  });

  if (!isOpen) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSend({
      type,
      title,
      message,
      targetScope: targetScope === 'SPECIFIC' ? specificRoom : targetScope,
      category: 'utilities',
      channel: channels.zalo ? 'IN_APP' : 'IN_APP',
      deliveryStatus: 'SENT'
    });
    onClose();
  };

  const handleTemplateChange = (tpl: string) => {
    if (tpl === 'meter') {
      setType('SERVICE_CHARGE_FINALIZED');
      setTitle('Thông báo chốt số điện nước tháng 10/2024');
      setMessage(
        'Chỉ số điện nước tháng 10 đã được cập nhật. Quý cư dân vui lòng đối soát ảnh công tơ trong app trước khi thanh toán hóa đơn.'
      );
    } else if (tpl === 'overdue') {
      setType('INVOICE_ISSUED');
      setTitle('Nhắc nhở: Hạn thanh toán tiền phòng sắp đến');
      setMessage(
        'Hạn đóng tiền phòng và dịch vụ tháng này là ngày 05/11. Vui lòng quét mã VietQR Techcombank trong ứng dụng để gạch nợ tự động.'
      );
    } else if (tpl === 'maintenance') {
      setType('MAINTENANCE');
      setTitle('Thông báo: Tạm ngưng cấp nước sinh hoạt để vệ sinh bể');
      setMessage(
        'Ban quản lý cơ sở 26 Đinh Bộ Lĩnh sẽ tiến hành súc rửa bể chứa nước sinh hoạt từ 08:00 đến 11:00 sáng mai. Mong quý cư dân trữ nước trước.'
      );
    }
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="bg-surface-container-lowest w-full max-w-lg rounded-2xl p-6 shadow-2xl border border-surface-container flex flex-col gap-4 animate-in fade-in zoom-in-95">
        <div className="flex items-center justify-between border-b border-surface-container pb-3">
          <div className="flex items-center gap-2">
            <span className="material-symbols-outlined text-primary text-2xl">campaign</span>
            <div>
              <h3 className="font-headline text-base font-bold text-on-surface">
                Phát thông báo tới Cư dân
              </h3>
              <p className="text-[11px] text-on-surface-variant">
                Đa kênh In-App, Zalo OA, SMS (BE-04.4 / SRS FR-MGR-08)
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

        {/* Quick Templates */}
        <div className="flex items-center gap-1.5 flex-wrap text-[11px]">
          <span className="text-on-surface-variant font-semibold">Mẫu nhanh:</span>
          <button
            type="button"
            onClick={() => handleTemplateChange('meter')}
            className="px-2.5 py-1 rounded-lg bg-surface-container text-primary font-bold hover:bg-primary/10 transition-colors"
          >
            ⚡ Chốt điện nước
          </button>
          <button
            type="button"
            onClick={() => handleTemplateChange('overdue')}
            className="px-2.5 py-1 rounded-lg bg-surface-container text-secondary font-bold hover:bg-secondary/10 transition-colors"
          >
            💰 Nhắc nợ hóa đơn
          </button>
          <button
            type="button"
            onClick={() => handleTemplateChange('maintenance')}
            className="px-2.5 py-1 rounded-lg bg-surface-container text-on-surface font-bold hover:bg-surface-container-high transition-colors"
          >
            🔧 Tạm ngừng điện nước
          </button>
        </div>

        <form onSubmit={handleSubmit} className="flex flex-col gap-3 text-xs">
          {/* Target Audience Scope */}
          <div className="grid grid-cols-2 gap-3">
            <div className="flex flex-col gap-1">
              <label className="font-semibold text-on-surface">Phạm vi gửi (Target) *</label>
              <select
                value={targetScope}
                onChange={(e) => setTargetScope(e.target.value as any)}
                className="bg-surface-container-low border border-surface-container rounded-xl p-2.5 font-bold text-on-surface focus:outline-none cursor-pointer"
              >
                <option value="ALL">Toàn bộ 36 phòng (34 khách)</option>
                <option value="FLOOR_4">Cư dân Tầng 4</option>
                <option value="FLOOR_3">Cư dân Tầng 3</option>
                <option value="FLOOR_2">Cư dân Tầng 2</option>
                <option value="FLOOR_1">Cư dân Tầng 1</option>
                <option value="SPECIFIC">Chọn phòng cụ thể...</option>
              </select>
            </div>

            {targetScope === 'SPECIFIC' && (
              <div className="flex flex-col gap-1">
                <label className="font-semibold text-on-surface">Mã số phòng *</label>
                <input
                  type="text"
                  value={specificRoom}
                  onChange={(e) => setSpecificRoom(e.target.value)}
                  placeholder="P.204"
                  required
                  className="bg-surface-container-low border border-surface-container rounded-xl p-2.5 font-bold text-primary focus:outline-none"
                />
              </div>
            )}
          </div>

          <div className="flex flex-col gap-1">
            <label className="font-semibold text-on-surface">Tiêu đề thông báo *</label>
            <input
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              required
              className="bg-surface-container-low border border-surface-container rounded-xl p-2.5 font-bold text-on-surface focus:outline-none focus:ring-2 focus:ring-primary"
            />
          </div>

          <div className="flex flex-col gap-1">
            <label className="font-semibold text-on-surface">Nội dung chi tiết *</label>
            <textarea
              value={message}
              onChange={(e) => setMessage(e.target.value)}
              required
              rows={4}
              className="bg-surface-container-low border border-surface-container rounded-xl p-2.5 text-on-surface focus:outline-none focus:ring-2 focus:ring-primary resize-none leading-relaxed"
            />
          </div>

          {/* Delivery Channels */}
          <div className="flex flex-col gap-1.5 p-3 rounded-xl bg-surface-container">
            <span className="font-bold text-on-surface text-[11px]">Kênh gửi thông báo:</span>
            <div className="grid grid-cols-2 gap-2 text-xs">
              <label className="flex items-center gap-2 cursor-pointer font-medium">
                <input
                  type="checkbox"
                  checked={channels.inApp}
                  disabled
                  className="w-4 h-4 rounded text-primary accent-primary"
                />
                <span>App TroPro (Bắt buộc)</span>
              </label>

              <label className="flex items-center gap-2 cursor-pointer font-medium">
                <input
                  type="checkbox"
                  checked={channels.zalo}
                  onChange={(e) => setChannels({ ...channels, zalo: e.target.checked })}
                  className="w-4 h-4 rounded text-primary accent-primary"
                />
                <span>Zalo OA Cơ sở</span>
              </label>

              <label className="flex items-center gap-2 cursor-pointer font-medium">
                <input
                  type="checkbox"
                  checked={channels.sms}
                  onChange={(e) => setChannels({ ...channels, sms: e.target.checked })}
                  className="w-4 h-4 rounded text-primary accent-primary"
                />
                <span>SMS Brandname</span>
              </label>

              <label className="flex items-center gap-2 cursor-pointer font-medium">
                <input
                  type="checkbox"
                  checked={channels.email}
                  onChange={(e) => setChannels({ ...channels, email: e.target.checked })}
                  className="w-4 h-4 rounded text-primary accent-primary"
                />
                <span>Email thông báo</span>
              </label>
            </div>
          </div>

          <div className="flex items-center justify-end gap-2 pt-3 border-t border-surface-container">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 rounded-xl bg-surface-container text-on-surface-variant font-semibold hover:bg-surface-container-high transition-colors cursor-pointer"
            >
              Hủy
            </button>
            <button
              type="submit"
              className="px-5 py-2 rounded-xl bg-primary text-white font-bold hover:bg-primary-container transition-all cursor-pointer shadow-sm flex items-center gap-1.5"
            >
              <span className="material-symbols-outlined text-base">send</span>
              <span>Gửi thông báo ngay</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
