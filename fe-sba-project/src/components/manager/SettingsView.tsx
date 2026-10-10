import React, { useState } from 'react';
import { facilityInfo } from '../../data/mockData';

export const SettingsView: React.FC = () => {
  const [name, setName] = useState(facilityInfo.name);
  const [address, setAddress] = useState(facilityInfo.address);
  const [managerPhone, setManagerPhone] = useState(facilityInfo.managerPhone);
  const [electricTariff, setElectricTariff] = useState('3.800');
  const [waterTariff, setWaterTariff] = useState('22.000');
  const [bankAcc, setBankAcc] = useState(facilityInfo.bankAccount.accountNumber);
  const [bankName, setBankName] = useState(facilityInfo.bankAccount.bank);
  const [isSaved, setIsSaved] = useState(false);

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    setIsSaved(true);
    setTimeout(() => setIsSaved(false), 3000);
  };

  return (
    <div className="flex flex-col w-full p-6 lg:p-8 gap-6 max-w-[1200px] mx-auto">
      <div className="flex flex-col gap-1">
        <h1 className="font-headline text-2xl lg:text-3xl font-bold text-on-surface tracking-tight">
          Cài đặt Cơ sở Vận hành
        </h1>
        <p className="text-xs text-on-surface-variant font-medium">
          Quản lý thông tin địa điểm, định mức thu điện nước, tài khoản VietQR và quyền quản trị
        </p>
      </div>

      <form onSubmit={handleSave} className="flex flex-col gap-6">
        {/* Basic Info */}
        <div className="bg-surface-container-lowest p-6 rounded-2xl shadow-xs border border-surface-container flex flex-col gap-4">
          <h3 className="font-headline text-base font-bold text-on-surface flex items-center gap-2">
            <span className="material-symbols-outlined text-primary text-xl">apartment</span>
            Thông tin Tòa nhà & Cơ sở
          </h3>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-xs">
            <div className="flex flex-col gap-1">
              <label className="font-semibold text-on-surface">Tên cơ sở hiển thị</label>
              <input
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                className="bg-surface-container-low p-2.5 rounded-xl border border-surface-container text-xs font-medium"
              />
            </div>

            <div className="flex flex-col gap-1">
              <label className="font-semibold text-on-surface">Hotline quản lý trực tiếp</label>
              <input
                type="text"
                value={managerPhone}
                onChange={(e) => setManagerPhone(e.target.value)}
                className="bg-surface-container-low p-2.5 rounded-xl border border-surface-container text-xs font-medium"
              />
            </div>

            <div className="col-span-1 md:col-span-2 flex flex-col gap-1">
              <label className="font-semibold text-on-surface">Địa chỉ chuẩn hóa Google Maps & Tạm trú</label>
              <input
                type="text"
                value={address}
                onChange={(e) => setAddress(e.target.value)}
                className="bg-surface-container-low p-2.5 rounded-xl border border-surface-container text-xs font-medium"
              />
            </div>
          </div>
        </div>

        {/* Utility Tariff Defaults */}
        <div className="bg-surface-container-lowest p-6 rounded-2xl shadow-xs border border-surface-container flex flex-col gap-4">
          <h3 className="font-headline text-base font-bold text-on-surface flex items-center gap-2">
            <span className="material-symbols-outlined text-primary text-xl">receipt_long</span>
            Định mức Đơn giá Điện & Nước
          </h3>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-xs">
            <div className="flex flex-col gap-1">
              <label className="font-semibold text-on-surface">Đơn giá Điện (đ / kWh)</label>
              <input
                type="text"
                value={electricTariff}
                onChange={(e) => setElectricTariff(e.target.value)}
                className="bg-surface-container-low p-2.5 rounded-xl border border-surface-container text-xs font-bold text-secondary"
              />
              <span className="text-[11px] text-outline">Áp dụng tự động cho chỉ số công tơ điện tử</span>
            </div>

            <div className="flex flex-col gap-1">
              <label className="font-semibold text-on-surface">Đơn giá Nước (đ / m³)</label>
              <input
                type="text"
                value={waterTariff}
                onChange={(e) => setWaterTariff(e.target.value)}
                className="bg-surface-container-low p-2.5 rounded-xl border border-surface-container text-xs font-bold text-primary"
              />
              <span className="text-[11px] text-outline">Đồng hồ nước thủy cục tách biệt từng phòng</span>
            </div>
          </div>
        </div>

        {/* VietQR Bank Integration */}
        <div className="bg-surface-container-lowest p-6 rounded-2xl shadow-xs border border-surface-container flex flex-col gap-4">
          <h3 className="font-headline text-base font-bold text-on-surface flex items-center gap-2">
            <span className="material-symbols-outlined text-primary text-xl">qr_code_scanner</span>
            Tài khoản Ngân hàng nhận thanh toán (VietQR Tự Động)
          </h3>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-xs">
            <div className="flex flex-col gap-1">
              <label className="font-semibold text-on-surface">Ngân hàng</label>
              <select
                value={bankName}
                onChange={(e) => setBankName(e.target.value)}
                className="bg-surface-container-low p-2.5 rounded-xl border border-surface-container text-xs font-medium cursor-pointer"
              >
                <option value="MBBank">MBBank (Quân Đội)</option>
                <option value="Techcombank">Techcombank</option>
                <option value="Vietcombank">Vietcombank</option>
                <option value="ACB">ACB</option>
              </select>
            </div>

            <div className="flex flex-col gap-1">
              <label className="font-semibold text-on-surface">Số tài khoản</label>
              <input
                type="text"
                value={bankAcc}
                onChange={(e) => setBankAcc(e.target.value)}
                className="bg-surface-container-low p-2.5 rounded-xl border border-surface-container text-xs font-mono font-bold"
              />
            </div>

            <div className="flex flex-col gap-1">
              <label className="font-semibold text-on-surface">Tên chủ tài khoản</label>
              <input
                type="text"
                value={facilityInfo.bankAccount.accountHolder}
                readOnly
                className="bg-surface-container-high p-2.5 rounded-xl border border-surface-container text-xs font-bold cursor-not-allowed"
              />
            </div>
          </div>
        </div>

        <div className="flex items-center justify-between">
          {isSaved ? (
            <span className="text-xs font-bold text-primary flex items-center gap-1">
              <span className="material-symbols-outlined text-base">check_circle</span>
              Đã lưu cài đặt cơ sở thành công!
            </span>
          ) : <span></span>}

          <button
            type="submit"
            className="px-6 py-2.5 rounded-xl bg-primary hover:bg-primary-container text-white text-xs font-bold shadow-md transition-all cursor-pointer active:scale-95"
          >
            Lưu thay đổi
          </button>
        </div>
      </form>
    </div>
  );
};
