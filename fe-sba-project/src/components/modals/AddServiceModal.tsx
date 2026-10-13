import React, { useState } from 'react';
import { ServiceItem } from '../../types';

interface AddServiceModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (service: Partial<ServiceItem>) => void;
  initialService?: ServiceItem | null;
}

export const AddServiceModal: React.FC<AddServiceModalProps> = ({
  isOpen,
  onClose,
  onSave,
  initialService
}) => {
  const [code, setCode] = useState(initialService?.code || '');
  const [name, setName] = useState(initialService?.name || '');
  const [unit, setUnit] = useState(initialService?.unit || 'tháng');
  const [serviceType, setServiceType] = useState<ServiceItem['serviceType']>(
    initialService?.serviceType || 'OTHER'
  );
  const [billingMethod, setBillingMethod] = useState<ServiceItem['billingMethod']>(
    initialService?.billingMethod || 'FIXED'
  );
  const [referencePrice, setReferencePrice] = useState<number>(
    initialService?.referencePrice ?? 50000
  );
  const [mandatory, setMandatory] = useState<boolean>(initialService?.mandatory || false);
  const [active, setActive] = useState<boolean>(initialService?.active ?? true);
  const [description, setDescription] = useState(initialService?.description || '');

  if (!isOpen) return null;

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onSave({
      id: initialService?.id || `srv-${Date.now()}`,
      code: code || `SRV-${Date.now().toString().slice(-4)}`,
      name,
      unit,
      serviceType,
      billingMethod,
      referencePrice,
      mandatory,
      active,
      description,
      icon:
        serviceType === 'ELECTRICITY'
          ? 'bolt'
          : serviceType === 'WATER'
          ? 'water_drop'
          : serviceType === 'INTERNET'
          ? 'wifi'
          : serviceType === 'PARKING'
          ? 'two_wheeler'
          : serviceType === 'CLEANING'
          ? 'delete_sweep'
          : serviceType === 'LAUNDRY'
          ? 'local_laundry_service'
          : 'room_service'
    });
    onClose();
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-sm flex items-center justify-center p-4">
      <div className="bg-surface-container-lowest w-full max-w-lg rounded-2xl p-6 shadow-2xl border border-surface-container flex flex-col gap-4 animate-in fade-in zoom-in-95">
        <div className="flex items-center justify-between border-b border-surface-container pb-3">
          <div className="flex items-center gap-2">
            <span className="material-symbols-outlined text-primary text-2xl">
              {initialService ? 'edit_note' : 'add_circle'}
            </span>
            <div>
              <h3 className="font-headline text-base font-bold text-on-surface">
                {initialService ? 'Cập nhật dịch vụ' : 'Thêm dịch vụ & Tiện ích mới'}
              </h3>
              <p className="text-[11px] text-on-surface-variant">
                Cấu hình danh mục dịch vụ cơ sở (BE-04.1 / SRS FR-MGR-07)
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

        <form onSubmit={handleSubmit} className="flex flex-col gap-3 text-xs">
          <div className="grid grid-cols-2 gap-3">
            <div className="flex flex-col gap-1">
              <label className="font-semibold text-on-surface">Tên dịch vụ *</label>
              <input
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder="VD: Nước sinh hoạt, Wifi"
                required
                className="bg-surface-container-low border border-surface-container rounded-xl p-2.5 font-semibold text-on-surface focus:outline-none focus:ring-2 focus:ring-primary"
              />
            </div>
            <div className="flex flex-col gap-1">
              <label className="font-semibold text-on-surface">Mã dịch vụ</label>
              <input
                type="text"
                value={code}
                onChange={(e) => setCode(e.target.value.toUpperCase())}
                placeholder="VD: ELEC-01, WIFI"
                className="bg-surface-container-low border border-surface-container rounded-xl p-2.5 font-mono font-bold text-primary focus:outline-none focus:ring-2 focus:ring-primary"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div className="flex flex-col gap-1">
              <label className="font-semibold text-on-surface">Phân loại dịch vụ</label>
              <select
                value={serviceType}
                onChange={(e) => setServiceType(e.target.value as any)}
                className="bg-surface-container-low border border-surface-container rounded-xl p-2.5 font-semibold text-on-surface focus:outline-none cursor-pointer"
              >
                <option value="ELECTRICITY">Điện sinh hoạt (⚡)</option>
                <option value="WATER">Nước máy thủy cục (💧)</option>
                <option value="INTERNET">Internet / Wifi (📶)</option>
                <option value="PARKING">Giữ xe máy (🛵)</option>
                <option value="CLEANING">Vệ sinh & Rác (🗑️)</option>
                <option value="LAUNDRY">Giặt sấy tự động (🧺)</option>
                <option value="SECURITY">An ninh / Thẻ từ (🛡️)</option>
                <option value="OTHER">Dịch vụ khác</option>
              </select>
            </div>

            <div className="flex flex-col gap-1">
              <label className="font-semibold text-on-surface">Cách tính phí (Billing)</label>
              <select
                value={billingMethod}
                onChange={(e) => setBillingMethod(e.target.value as any)}
                className="bg-surface-container-low border border-surface-container rounded-xl p-2.5 font-semibold text-on-surface focus:outline-none cursor-pointer"
              >
                <option value="METERED">Theo chỉ số đồng hồ (METERED)</option>
                <option value="PER_ROOM">Cố định theo phòng (PER_ROOM)</option>
                <option value="PER_PERSON">Theo số người ở (PER_PERSON)</option>
                <option value="PER_USE">Theo lượt sử dụng (PER_USE)</option>
                <option value="FIXED">Miễn phí / Cố định (FIXED)</option>
              </select>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div className="flex flex-col gap-1">
              <label className="font-semibold text-on-surface">Đơn giá tham chiếu (VNĐ) *</label>
              <input
                type="number"
                value={referencePrice}
                onChange={(e) => setReferencePrice(Number(e.target.value))}
                min={0}
                step={500}
                required
                className="bg-surface-container-low border border-surface-container rounded-xl p-2.5 font-bold text-secondary focus:outline-none focus:ring-2 focus:ring-primary tabular-nums"
              />
            </div>

            <div className="flex flex-col gap-1">
              <label className="font-semibold text-on-surface">Đơn vị tính *</label>
              <input
                type="text"
                value={unit}
                onChange={(e) => setUnit(e.target.value)}
                placeholder="VD: kWh, m³, người/tháng, phòng"
                required
                className="bg-surface-container-low border border-surface-container rounded-xl p-2.5 font-semibold text-on-surface focus:outline-none focus:ring-2 focus:ring-primary"
              />
            </div>
          </div>

          <div className="flex flex-col gap-1">
            <label className="font-semibold text-on-surface">Mô tả chi tiết & Quy định</label>
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Ghi chú điều kiện áp dụng, ngày chốt chỉ số, tiêu chuẩn chất lượng..."
              rows={2}
              className="bg-surface-container-low border border-surface-container rounded-xl p-2.5 text-on-surface focus:outline-none focus:ring-2 focus:ring-primary resize-none"
            />
          </div>

          {/* Flags */}
          <div className="p-3 rounded-xl bg-surface-container flex items-center justify-between gap-4">
            <label className="flex items-center gap-2 cursor-pointer">
              <input
                type="checkbox"
                checked={mandatory}
                onChange={(e) => setMandatory(e.target.checked)}
                className="w-4 h-4 rounded text-primary accent-primary"
              />
              <div>
                <span className="font-bold text-on-surface block">Dịch vụ bắt buộc</span>
                <span className="text-[10px] text-on-surface-variant block">
                  Khách thuê không thể tự ý hủy đăng ký (Áp dụng cho Điện, Nước)
                </span>
              </div>
            </label>

            <label className="flex items-center gap-2 cursor-pointer">
              <input
                type="checkbox"
                checked={active}
                onChange={(e) => setActive(e.target.checked)}
                className="w-4 h-4 rounded text-primary accent-primary"
              />
              <span className="font-bold text-on-surface">Đang hoạt động</span>
            </label>
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
              <span className="material-symbols-outlined text-base">check</span>
              <span>{initialService ? 'Cập nhật' : 'Lưu dịch vụ'}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
