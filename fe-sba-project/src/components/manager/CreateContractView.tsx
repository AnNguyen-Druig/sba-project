import React, { useState } from 'react';
import { ViewMode } from '../../types';
import { facilityInfo } from '../../data/mockData';

interface CreateContractViewProps {
  onBack: () => void;
  onNavigate: (view: ViewMode) => void;
  onCreateSuccess: (contractData: any) => void;
}

export const CreateContractView: React.FC<CreateContractViewProps> = ({
  onBack,
  onNavigate,
  onCreateSuccess
}) => {
  const [currentStep, setCurrentStep] = useState(3);
  const [durationMonths, setDurationMonths] = useState(12);
  const [startDate, setStartDate] = useState('15/09/2024');
  const [endDate, setEndDate] = useState('15/09/2025');
  const [autoRemind, setAutoRemind] = useState(true);
  const [rentPrice] = useState(5200000);
  const [depositAmount] = useState(10400000);
  
  // Services
  const [services, setServices] = useState({
    electric: true,
    water: true,
    wifi: true,
    parking: true,
    cleaning: false
  });

  const [isOtpSent, setIsOtpSent] = useState(false);
  const [otpCode, setOtpCode] = useState('');

  const handleDurationSelect = (months: number) => {
    setDurationMonths(months);
    if (months === 6) {
      setEndDate('15/03/2025');
    } else if (months === 12) {
      setEndDate('15/09/2025');
    }
  };

  const handleFinishContract = () => {
    onCreateSuccess({
      roomNumber: 'P.204',
      tenantName: 'Nguyễn Hoàng Long',
      monthlyRent: rentPrice,
      deposit: depositAmount,
      durationMonths,
      startDate,
      endDate
    });
  };

  return (
    <div className="w-full max-w-[1300px] mx-auto p-6 lg:p-8 flex flex-col gap-6">
      {/* Top Breadcrumb & Header */}
      <div className="flex items-center justify-between">
        <div className="flex flex-col gap-1">
          <nav className="flex items-center gap-1.5 text-on-surface-variant text-xs font-medium">
            <button type="button" onClick={() => onNavigate('contracts')} className="hover:text-primary transition-colors cursor-pointer">
              Hợp đồng
            </button>
            <span className="text-outline-variant">/</span>
            <span className="text-on-surface-variant">Cơ sở Bình Thạnh</span>
            <span className="text-outline-variant">/</span>
            <span className="text-on-surface font-bold">Tạo hợp đồng mới</span>
          </nav>

          <div className="flex items-center gap-3">
            <h1 className="font-headline text-2xl lg:text-3xl font-bold text-[#2F5D58] tracking-tight">
              Tạo hợp đồng thuê phòng điện tử
            </h1>
            <span className="px-2.5 py-0.5 rounded-full bg-[#EBF7F4] text-[#2F5D58] text-xs font-semibold">
              Bản thảo lưu tự động
            </span>
          </div>
        </div>

        <button
          type="button"
          onClick={onBack}
          className="flex items-center gap-1.5 px-3.5 py-2 rounded-xl bg-surface-container-lowest text-[#2F5D58] text-xs font-bold hover:bg-surface-container transition-colors shadow-xs cursor-pointer border border-[#D5EBE6]"
        >
          <span className="material-symbols-outlined text-base">save</span>
          <span>Lưu nháp & Thoát</span>
        </button>
      </div>

      {/* 4-Step Wizard Progress Bar */}
      <div className="w-full bg-surface-container-lowest rounded-2xl p-4 shadow-xs border border-surface-container">
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 items-center">
          {/* Step 1 */}
          <div
            onClick={() => setCurrentStep(1)}
            className="flex items-center gap-3 p-2 rounded-xl cursor-pointer hover:bg-surface-container-low transition-colors"
          >
            <div className="w-8 h-8 rounded-full bg-[#EBF7F4] text-[#2F5D58] flex items-center justify-center text-sm font-bold flex-shrink-0">
              <span className="material-symbols-outlined text-base">check</span>
            </div>
            <div className="flex flex-col min-w-0">
              <span className="text-[10px] text-on-surface-variant uppercase tracking-wider font-semibold">Bước 1</span>
              <span className="text-xs text-on-surface font-bold truncate">1. Chọn phòng</span>
              <span className="text-[11px] text-[#2F5D58] truncate">P.204 · Tầng 2</span>
            </div>
          </div>

          {/* Step 2 */}
          <div
            onClick={() => setCurrentStep(2)}
            className="flex items-center gap-3 p-2 rounded-xl cursor-pointer hover:bg-surface-container-low transition-colors"
          >
            <div className="w-8 h-8 rounded-full bg-[#EBF7F4] text-[#2F5D58] flex items-center justify-center text-sm font-bold flex-shrink-0">
              <span className="material-symbols-outlined text-base">check</span>
            </div>
            <div className="flex flex-col min-w-0">
              <span className="text-[10px] text-on-surface-variant uppercase tracking-wider font-semibold">Bước 2</span>
              <span className="text-xs text-on-surface font-bold truncate">2. Người thuê</span>
              <span className="text-[11px] text-[#2F5D58] truncate">Nguyễn Hoàng Long</span>
            </div>
          </div>

          {/* Step 3 (Active) */}
          <div className="flex items-center gap-3 p-2 bg-[#F4FBF9] rounded-xl border border-[#D5EBE6]">
            <div className="w-8 h-8 rounded-full bg-[#2F5D58] text-white flex items-center justify-center text-sm font-bold flex-shrink-0">
              <span className="material-symbols-outlined text-base">edit_document</span>
            </div>
            <div className="flex flex-col min-w-0">
              <span className="text-[10px] text-[#2F5D58] font-bold uppercase tracking-wider">Bước 3 · Đang điền</span>
              <span className="text-xs text-[#2F5D58] font-bold truncate">3. Điều khoản & Dịch vụ</span>
              <span className="text-[11px] text-[#5C7773] truncate">Mẫu chuẩn 2024</span>
            </div>
          </div>

          {/* Step 4 */}
          <div className="flex items-center gap-3 p-2 opacity-60">
            <div className="w-8 h-8 rounded-full bg-surface-container text-on-surface-variant flex items-center justify-center text-xs font-semibold flex-shrink-0">
              4
            </div>
            <div className="flex flex-col min-w-0">
              <span className="text-[10px] text-on-surface-variant uppercase tracking-wider">Bước 4</span>
              <span className="text-xs text-on-surface-variant font-medium truncate">4. Xem trước & Gửi ký</span>
              <span className="text-[11px] text-outline truncate">Chưa tới</span>
            </div>
          </div>
        </div>
      </div>

      {/* Main Two-Column Layout */}
      <div className="grid grid-cols-1 xl:grid-cols-12 gap-8 items-start">
        {/* Left Column: Form Blocks (~65% -> 8 cols) */}
        <div className="xl:col-span-8 flex flex-col gap-6">
          {/* Block 1: Mẫu hợp đồng pháp lý */}
          <div className="bg-surface-container-lowest rounded-2xl p-6 shadow-xs border border-surface-container flex flex-col gap-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2.5">
                <span className="material-symbols-outlined text-[#2F5D58] text-xl">gavel</span>
                <h2 className="font-headline text-base font-bold text-on-surface">1. Mẫu hợp đồng pháp lý</h2>
              </div>
              <span className="px-3 py-0.5 rounded-full bg-[#EBF7F4] text-[#2F5D58] text-xs font-bold flex items-center gap-1">
                <span className="material-symbols-outlined text-xs">verified</span>
                Đã xác thực pháp lý
              </span>
            </div>

            <div className="flex flex-col gap-1.5">
              <label className="text-xs text-on-surface-variant font-semibold">Chọn biểu mẫu chuẩn hóa</label>
              <div className="relative">
                <select className="w-full bg-surface-container-low px-4 py-2.5 rounded-xl text-xs text-on-surface appearance-none focus:outline-none focus:bg-surface-container-lowest transition-colors pr-10 cursor-pointer border border-surface-container font-medium">
                  <option>Mẫu hợp đồng phòng đơn 12 tháng (Chuẩn TroPro 2024)</option>
                  <option>Mẫu hợp đồng căn hộ Studio mini (Ngắn hạn 6 tháng)</option>
                  <option>Mẫu thỏa thuận thuê phòng trọ ghép / Ký túc xá dịch vụ</option>
                </select>
                <span className="material-symbols-outlined absolute right-3 top-1/2 -translate-y-1/2 pointer-events-none text-outline">
                  expand_more
                </span>
              </div>
              <p className="text-[11px] text-[#5C7773] mt-1 leading-relaxed">
                Mẫu này áp dụng chuẩn Nghị định 13/2023/NĐ-CP và Luật Nhà ở sửa đổi, cam kết rõ quyền sử dụng và bảo mật dữ liệu khách thuê.
              </p>
            </div>
          </div>

          {/* Block 2: Thời hạn thuê phòng */}
          <div className="bg-surface-container-lowest rounded-2xl p-6 shadow-xs border border-surface-container flex flex-col gap-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2.5">
                <span className="material-symbols-outlined text-[#2F5D58] text-xl">calendar_month</span>
                <h2 className="font-headline text-base font-bold text-on-surface">2. Thời hạn thuê phòng</h2>
              </div>
              <span className="text-xs text-on-surface-variant font-medium">
                Thời hạn đủ {durationMonths * 30} ngày ({durationMonths} tháng)
              </span>
            </div>

            {/* Quick Pills */}
            <div className="flex items-center gap-2">
              <button
                type="button"
                onClick={() => handleDurationSelect(6)}
                className={`px-4 py-1.5 rounded-xl text-xs font-semibold transition-all cursor-pointer ${
                  durationMonths === 6
                    ? 'bg-[#2F5D58] text-white'
                    : 'bg-surface-container-low text-on-surface-variant hover:bg-surface-container'
                }`}
              >
                6 tháng
              </button>
              <button
                type="button"
                onClick={() => handleDurationSelect(12)}
                className={`px-4 py-1.5 rounded-xl text-xs font-bold transition-all cursor-pointer ${
                  durationMonths === 12
                    ? 'bg-[#2F5D58] text-white shadow-xs'
                    : 'bg-surface-container-low text-on-surface-variant hover:bg-surface-container'
                }`}
              >
                12 tháng (Khuyên dùng)
              </button>
              <button
                type="button"
                onClick={() => handleDurationSelect(24)}
                className={`px-4 py-1.5 rounded-xl text-xs font-semibold transition-all cursor-pointer ${
                  durationMonths === 24
                    ? 'bg-[#2F5D58] text-white'
                    : 'bg-surface-container-low text-on-surface-variant hover:bg-surface-container'
                }`}
              >
                24 tháng
              </button>
            </div>

            {/* Date Inputs */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="flex flex-col gap-1">
                <label className="text-xs text-on-surface-variant font-semibold">Ngày bắt đầu hợp đồng</label>
                <div className="relative">
                  <input
                    type="text"
                    value={startDate}
                    onChange={(e) => setStartDate(e.target.value)}
                    className="w-full bg-surface-container-low pl-10 pr-3 py-2 rounded-xl text-xs text-on-surface focus:outline-none focus:bg-surface-container-lowest transition-colors border border-surface-container font-medium"
                  />
                  <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline text-base">
                    calendar_today
                  </span>
                </div>
                <span className="text-[11px] text-outline">Bắt đầu tính tiền phòng từ ngày này</span>
              </div>

              <div className="flex flex-col gap-1">
                <label className="text-xs text-on-surface-variant font-semibold">Ngày kết thúc dự kiến</label>
                <div className="relative">
                  <input
                    type="text"
                    value={endDate}
                    readOnly
                    className="w-full bg-surface-container-high pl-10 pr-3 py-2 rounded-xl text-xs text-on-surface focus:outline-none border border-surface-container font-semibold cursor-not-allowed"
                  />
                  <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-outline text-base">
                    event_available
                  </span>
                </div>
                <span className="text-[11px] text-[#2F5D58] font-bold">Hệ thống tự động cộng đúng {durationMonths} tháng</span>
              </div>
            </div>

            <div className="flex items-center gap-2.5 p-3 rounded-xl bg-surface-container-low">
              <input
                type="checkbox"
                id="auto-remind-contract"
                checked={autoRemind}
                onChange={(e) => setAutoRemind(e.target.checked)}
                className="w-4 h-4 rounded text-primary accent-[#2F5D58] cursor-pointer"
              />
              <label htmlFor="auto-remind-contract" className="text-xs text-on-surface cursor-pointer select-none font-medium">
                Tự động gửi thông báo nhắc gia hạn hợp đồng qua Zalo trước 30 ngày cho cả khách và chủ trọ
              </label>
            </div>
          </div>

          {/* Block 3: Giá thuê & Tiền đặt cọc */}
          <div className="bg-surface-container-lowest rounded-2xl p-6 shadow-xs border border-surface-container flex flex-col gap-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2.5">
                <span className="material-symbols-outlined text-[#2F5D58] text-xl">payments</span>
                <h2 className="font-headline text-base font-bold text-on-surface">3. Giá thuê & Tiền đặt cọc</h2>
              </div>
              <span className="text-xs text-outline font-medium">Đơn vị: VNĐ</span>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="flex flex-col gap-1 p-4 rounded-xl bg-surface-container-low border border-surface-container">
                <div className="flex items-center justify-between">
                  <span className="text-xs text-on-surface-variant font-semibold">Giá thuê phòng</span>
                  <span className="text-[11px] text-[#2F5D58] font-bold">Niêm yết</span>
                </div>
                <div className="flex items-baseline gap-1 mt-1">
                  <span className="font-headline text-2xl font-bold text-on-surface tabular-nums">
                    {rentPrice.toLocaleString()}
                  </span>
                  <span className="text-xs text-on-surface-variant">đ / tháng</span>
                </div>
                <span className="text-[11px] text-[#5C7773] mt-1 flex items-center gap-1 font-medium">
                  <span className="material-symbols-outlined text-xs">info</span>
                  Lấy từ giá niêm yết phòng P.204
                </span>
              </div>

              <div className="flex flex-col gap-1 p-4 rounded-xl bg-surface-container-low border border-surface-container">
                <div className="flex items-center justify-between">
                  <span className="text-xs text-on-surface-variant font-semibold">Tiền đặt cọc thực tế</span>
                  <span className="px-2 py-0.5 rounded bg-[#EBF7F4] text-[#2F5D58] text-[11px] font-bold">
                    Đã nhận đủ
                  </span>
                </div>
                <div className="flex items-baseline gap-1 mt-1">
                  <span className="font-headline text-2xl font-bold text-[#2F5D58] tabular-nums">
                    {depositAmount.toLocaleString()}
                  </span>
                  <span className="text-xs text-on-surface-variant">đ</span>
                </div>
                <span className="text-[11px] text-on-surface-variant mt-1 font-medium">
                  Mặc định tương đương 02 tháng tiền phòng
                </span>
              </div>
            </div>
          </div>

          {/* Block 4: Kỳ thanh toán & Thu phí */}
          <div className="bg-surface-container-lowest rounded-2xl p-6 shadow-xs border border-surface-container flex flex-col gap-4">
            <div className="flex items-center gap-2.5">
              <span className="material-symbols-outlined text-[#2F5D58] text-xl">account_balance</span>
              <h2 className="font-headline text-base font-bold text-on-surface">4. Kỳ thanh toán & Thu phí</h2>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="flex flex-col gap-1.5">
                <label className="text-xs text-on-surface-variant font-semibold">Hạn chót thanh toán hằng tháng</label>
                <div className="p-3 rounded-xl bg-surface-container-low flex items-center justify-between border border-surface-container">
                  <div className="flex items-center gap-2">
                    <input type="radio" checked readOnly className="accent-[#2F5D58] w-4 h-4" />
                    <span className="text-xs text-on-surface font-bold">Từ ngày 01 đến 05 hằng tháng</span>
                  </div>
                  <span className="material-symbols-outlined text-outline text-base">schedule</span>
                </div>
              </div>

              <div className="flex flex-col gap-1.5">
                <label className="text-xs text-on-surface-variant font-semibold">Kênh nhận tiền thanh toán</label>
                <div className="p-3 rounded-xl bg-surface-container-low flex items-center justify-between border border-surface-container">
                  <div className="flex items-center gap-2.5">
                    <span className="material-symbols-outlined text-[#2F5D58] text-xl">qr_code_scanner</span>
                    <div className="flex flex-col">
                      <span className="text-xs text-on-surface font-bold">VietQR tự động (MBBank)</span>
                      <span className="text-[11px] text-on-surface-variant">
                        STK: {facilityInfo.bankAccount.accountNumber} · {facilityInfo.bankAccount.accountHolder}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Block 5: Dịch vụ & Đơn giá niêm yết */}
          <div className="bg-surface-container-lowest rounded-2xl p-6 shadow-xs border border-surface-container flex flex-col gap-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2.5">
                <span className="material-symbols-outlined text-[#2F5D58] text-xl">room_service</span>
                <h2 className="font-headline text-base font-bold text-on-surface">5. Dịch vụ & Đơn giá niêm yết</h2>
              </div>
              <span className="text-xs text-on-surface-variant font-medium">
                {Object.values(services).filter(Boolean).length}/5 dịch vụ đã được chọn
              </span>
            </div>

            <div className="flex flex-col gap-2">
              {/* Electric */}
              <label className="flex items-center justify-between p-3 rounded-xl bg-surface-container-low hover:bg-surface-container transition-colors cursor-pointer border border-surface-container">
                <div className="flex items-center gap-3">
                  <input
                    type="checkbox"
                    checked={services.electric}
                    onChange={(e) => setServices({ ...services, electric: e.target.checked })}
                    className="w-4 h-4 rounded accent-[#2F5D58]"
                  />
                  <div className="flex flex-col">
                    <span className="text-xs font-bold text-on-surface">Điện sinh hoạt</span>
                    <span className="text-[11px] text-on-surface-variant">Theo chỉ số công tơ điện tử riêng từng phòng</span>
                  </div>
                </div>
                <div className="text-right">
                  <span className="text-xs font-bold text-on-surface tabular-nums">3.800 đ</span>
                  <span className="text-[11px] text-outline"> / kWh</span>
                </div>
              </label>

              {/* Water */}
              <label className="flex items-center justify-between p-3 rounded-xl bg-surface-container-low hover:bg-surface-container transition-colors cursor-pointer border border-surface-container">
                <div className="flex items-center gap-3">
                  <input
                    type="checkbox"
                    checked={services.water}
                    onChange={(e) => setServices({ ...services, water: e.target.checked })}
                    className="w-4 h-4 rounded accent-[#2F5D58]"
                  />
                  <div className="flex flex-col">
                    <span className="text-xs font-bold text-on-surface">Nước sinh hoạt</span>
                    <span className="text-[11px] text-on-surface-variant">Theo chỉ số đồng hồ nước riêng từng phòng</span>
                  </div>
                </div>
                <div className="text-right">
                  <span className="text-xs font-bold text-on-surface tabular-nums">22.000 đ</span>
                  <span className="text-[11px] text-outline"> / m³</span>
                </div>
              </label>

              {/* Wifi */}
              <label className="flex items-center justify-between p-3 rounded-xl bg-surface-container-low hover:bg-surface-container transition-colors cursor-pointer border border-surface-container">
                <div className="flex items-center gap-3">
                  <input
                    type="checkbox"
                    checked={services.wifi}
                    onChange={(e) => setServices({ ...services, wifi: e.target.checked })}
                    className="w-4 h-4 rounded accent-[#2F5D58]"
                  />
                  <div className="flex flex-col">
                    <span className="text-xs font-bold text-on-surface">Wifi tốc độ cao, Rác & Vệ sinh hành lang</span>
                    <span className="text-[11px] text-on-surface-variant">Khoán trọn gói cố định mỗi tháng</span>
                  </div>
                </div>
                <div className="text-right">
                  <span className="text-xs font-bold text-on-surface tabular-nums">150.000 đ</span>
                  <span className="text-[11px] text-outline"> / tháng</span>
                </div>
              </label>

              {/* Parking */}
              <label className="flex items-center justify-between p-3 rounded-xl bg-surface-container-low hover:bg-surface-container transition-colors cursor-pointer border border-surface-container">
                <div className="flex items-center gap-3">
                  <input
                    type="checkbox"
                    checked={services.parking}
                    onChange={(e) => setServices({ ...services, parking: e.target.checked })}
                    className="w-4 h-4 rounded accent-[#2F5D58]"
                  />
                  <div className="flex flex-col">
                    <span className="text-xs font-bold text-on-surface">Phí giữ xe máy</span>
                    <span className="text-[11px] text-on-surface-variant">Đăng ký 01 xe: Honda AirBlade · Biển 77G1-829.41</span>
                  </div>
                </div>
                <div className="text-right">
                  <span className="text-xs font-bold text-on-surface tabular-nums">100.000 đ</span>
                  <span className="text-[11px] text-outline"> / xe / tháng</span>
                </div>
              </label>

              {/* Optional Cleaning */}
              <label className="flex items-center justify-between p-3 rounded-xl bg-surface-container-lowest hover:bg-surface-container-low transition-colors cursor-pointer border border-surface-container opacity-80">
                <div className="flex items-center gap-3">
                  <input
                    type="checkbox"
                    checked={services.cleaning}
                    onChange={(e) => setServices({ ...services, cleaning: e.target.checked })}
                    className="w-4 h-4 rounded accent-[#2F5D58]"
                  />
                  <div className="flex flex-col">
                    <span className="text-xs font-medium text-on-surface">Dọn phòng định kỳ (2 lần/tháng)</span>
                    <span className="text-[11px] text-outline">Gói tùy chọn thêm nếu khách có nhu cầu</span>
                  </div>
                </div>
                <div className="text-right">
                  <span className="text-xs font-semibold text-outline tabular-nums">200.000 đ</span>
                  <span className="text-[11px] text-outline"> / tháng</span>
                </div>
              </label>
            </div>
          </div>
        </div>

        {/* Right Column: Sticky Contract Summary (~35% -> 4 cols) */}
        <div className="xl:col-span-4 sticky top-20 flex flex-col gap-6">
          <div className="bg-surface-container-lowest rounded-2xl p-6 shadow-xs border border-surface-container flex flex-col gap-4">
            <div className="flex items-center justify-between pb-1 border-b border-surface-container">
              <h3 className="font-headline text-base font-bold text-[#2F5D58]">Tóm tắt hợp đồng P.204</h3>
              <span className="px-2 py-0.5 rounded bg-[#EBF7F4] text-[#2F5D58] text-[11px] font-bold">
                Chờ hoàn tất
              </span>
            </div>

            {/* Room info */}
            <div className="p-3.5 rounded-xl bg-surface-container-low flex flex-col gap-1 border border-surface-container">
              <div className="flex items-center justify-between">
                <span className="text-xs text-on-surface-variant font-medium">Phòng thuê</span>
                <span className="text-xs font-bold text-on-surface">P.204 (Tầng 2)</span>
              </div>
              <div className="flex items-center justify-between text-[11px] text-[#5C7773]">
                <span>Diện tích: 28 m² · Ban công</span>
                <span>Cơ sở Bình Thạnh</span>
              </div>
            </div>

            {/* Tenant info */}
            <div className="p-3.5 rounded-xl bg-surface-container-low flex flex-col gap-1 border border-surface-container">
              <div className="flex items-center justify-between">
                <span className="text-xs text-on-surface-variant font-medium">Đại diện ký thuê</span>
                <span className="text-xs font-bold text-on-surface">Nguyễn Hoàng Long</span>
              </div>
              <div className="flex items-center justify-between text-[11px] text-on-surface-variant">
                <span>SĐT: 0918.421.905</span>
                <span>CCCD: 079201004821</span>
              </div>
              <div className="flex items-center gap-1 text-[11px] text-[#2F5D58] mt-1 font-semibold">
                <span className="material-symbols-outlined text-xs">group</span>
                <span>2 người cư trú: N.H.Long & L.V.Bảo</span>
              </div>
            </div>

            {/* Projected monthly fee */}
            <div className="flex flex-col gap-1.5 pt-1 text-xs">
              <span className="font-bold text-on-surface text-xs">Dự toán khoản thanh toán hằng tháng</span>
              <div className="flex items-center justify-between text-on-surface-variant">
                <span>Tiền phòng cố định:</span>
                <span className="font-semibold text-on-surface">{rentPrice.toLocaleString()} đ</span>
              </div>
              <div className="flex items-center justify-between text-on-surface-variant">
                <span>Wifi + vệ sinh, rác:</span>
                <span className="font-semibold text-on-surface">150.000 đ</span>
              </div>
              <div className="flex items-center justify-between text-on-surface-variant">
                <span>Phí giữ xe máy (01 xe):</span>
                <span className="font-semibold text-on-surface">100.000 đ</span>
              </div>
              <div className="flex items-center justify-between text-outline text-[11px]">
                <span>Điện & Nước sinh hoạt:</span>
                <span className="italic text-on-surface-variant">Theo chỉ số thực tế</span>
              </div>

              <div className="my-2 h-px bg-surface-container"></div>

              <div className="flex items-center justify-between">
                <div className="flex flex-col">
                  <span className="text-xs font-bold text-on-surface uppercase">TỔNG CỐ ĐỊNH / THÁNG</span>
                  <span className="text-[11px] text-outline">Chưa bao gồm điện, nước</span>
                </div>
                <span className="font-headline text-lg font-bold text-[#2F5D58]">
                  {(rentPrice + 250000).toLocaleString()} đ
                </span>
              </div>

              <div className="flex items-center justify-between p-3 rounded-xl bg-[#EBF7F4] mt-2 border border-[#D5EBE6]">
                <span className="text-xs text-[#2F5D58] font-bold">Tiền cọc giữ chỗ đã thu:</span>
                <span className="text-xs font-bold text-[#2F5D58]">{depositAmount.toLocaleString()} đ</span>
              </div>
            </div>

            {/* Note */}
            <div className="p-3.5 rounded-xl bg-surface-container-low flex items-start gap-2 border border-surface-container">
              <span className="material-symbols-outlined text-[#2F5D58] text-base mt-0.5">verified_user</span>
              <p className="text-[11px] text-[#5C7773] leading-relaxed">
                Sau khi bấm gửi, hệ thống TroPro sẽ chuyển phát hợp đồng qua Zalo OA và gửi mã OTP xác nhận điện tử có giá trị pháp lý tới số điện thoại của anh Nguyễn Hoàng Long.
              </p>
            </div>
          </div>
        </div>
      </div>

      {/* Sticky Bottom Bar */}
      <div className="sticky bottom-4 z-30 w-full bg-surface-container-lowest/95 backdrop-blur-md rounded-2xl p-4 shadow-xl border border-surface-container flex items-center justify-between">
        <button
          type="button"
          onClick={onBack}
          className="flex items-center gap-1.5 px-4 py-2.5 rounded-xl bg-surface-container-low text-on-surface hover:bg-surface-container text-xs font-bold transition-colors cursor-pointer"
        >
          <span className="material-symbols-outlined text-base">arrow_back</span>
          <span>Quay lại: Người thuê</span>
        </button>

        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={() => alert('Đã lưu bản nháp hợp đồng thành công!')}
            className="px-4 py-2.5 rounded-xl bg-surface-container text-on-surface text-xs font-bold hover:bg-surface-container-high transition-colors cursor-pointer"
          >
            Lưu bản nháp
          </button>

          <button
            type="button"
            onClick={() => setIsOtpSent(true)}
            className="flex items-center gap-2 px-6 py-2.5 rounded-xl bg-[#F4A68C] hover:bg-[#EE9578] text-[#5A2A1A] font-headline text-xs font-bold transition-all shadow-md active:scale-95 cursor-pointer"
          >
            <span>Tiếp tục: Xem trước và gửi ký</span>
            <span className="material-symbols-outlined text-lg">arrow_forward</span>
          </button>
        </div>
      </div>

      {/* OTP Sign Simulation Modal */}
      {isOtpSent && (
        <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-surface-container-lowest w-full max-w-md rounded-2xl p-6 shadow-2xl border border-surface-container flex flex-col gap-4">
            <div className="flex items-center justify-between border-b border-surface-container pb-3">
              <div className="flex items-center gap-2">
                <span className="material-symbols-outlined text-primary text-xl">lock_clock</span>
                <h3 className="font-headline text-base font-bold text-on-surface">Xác thực Ký số SmartOTP</h3>
              </div>
              <button
                type="button"
                onClick={() => setIsOtpSent(false)}
                className="w-7 h-7 rounded-full bg-surface-container flex items-center justify-center text-on-surface-variant cursor-pointer"
              >
                <span className="material-symbols-outlined text-sm">close</span>
              </button>
            </div>

            <p className="text-xs text-on-surface-variant leading-relaxed">
              Mã OTP xác thực điện tử đã được chuyển phát qua Zalo OA đến số điện thoại <strong>0918.421.905</strong> của anh Nguyễn Hoàng Long.
            </p>

            <div className="flex flex-col gap-1.5">
              <label className="text-xs font-bold text-on-surface">Nhập mã OTP 6 số (Mã thử nghiệm: 686868)</label>
              <input
                type="text"
                value={otpCode}
                onChange={(e) => setOtpCode(e.target.value)}
                placeholder="686868"
                maxLength={6}
                className="w-full bg-surface-container rounded-xl px-4 py-2.5 text-center text-lg tracking-widest font-mono font-bold text-primary focus:outline-none focus:ring-2 focus:ring-primary"
              />
            </div>

            <div className="flex items-center justify-end gap-2 pt-2 border-t border-surface-container">
              <button
                type="button"
                onClick={() => setIsOtpSent(false)}
                className="px-4 py-2 rounded-xl bg-surface-container text-on-surface text-xs font-semibold"
              >
                Đóng
              </button>
              <button
                type="button"
                onClick={() => {
                  handleFinishContract();
                  setIsOtpSent(false);
                }}
                className="px-5 py-2.5 rounded-xl bg-[#5FB3A8] text-white text-xs font-bold hover:bg-[#529E94] shadow-sm transition-all"
              >
                Xác nhận Ký Hợp Đồng
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
