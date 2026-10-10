import React, { useState } from 'react';
import { Room } from '../../types';
import { BookingModal } from '../modals/BookingModal';

interface PublicRentSearchViewProps {
  rooms: Room[];
  onOpenCreateThread: () => void;
  onNavigateToThreads: () => void;
}

export const PublicRentSearchView: React.FC<PublicRentSearchViewProps> = ({
  rooms,
  onOpenCreateThread,
  onNavigateToThreads
}) => {
  // Public verified rooms
  const publicRooms: Room[] = [
    {
      id: 'pub-401',
      roomNumber: 'Phòng 401',
      floor: 4,
      floorName: 'Tầng 4',
      area: 28,
      type: 'Studio Ban Công Thoáng Sáng',
      price: 5000000,
      status: 'available',
      statusLabel: 'Sẵn sàng dọn vào ngay',
      capacity: 'Tối đa 3 người',
      features: ['28 m²', 'Tối đa 3 người', 'Ban công riêng', 'Khóa vân tay'],
      images: [
        'https://lh3.googleusercontent.com/aida-public/AB6AXuAhowM1C2TwkZSCiG3Gy-EAKU_WXEN25CfSICmr5QU7chuzhuhPx9z1Evp_0cvgOIm1ckn2PkoG24hbuUbLHonCOr3gUa1CxfUJeSCIBP3fod9iG4iatRQzd2lfZq57L-jRnOILkFnlncm2XuQD3ziTGaboaOOr_CvlloPdW2vivwBRjwc42plUuibSvwiXr15Ap1Iwb4_7YW0CtQWBnMHVk_WSsWG0LNHRKjgN2Fe2rNb6Bi78lU5V',
        'https://lh3.googleusercontent.com/aida-public/AB6AXuD6NQ7-M_XvKgsPw6T6E2ksdc-um2jvj1tvnFmzD7qwgO11K_jE0Lz8kRNXnkSQ7aJHeqgwDJ1_VvFa4muxr5y0RAmErWdktmsp3UbiKnWvIt3u1D3GgSMgQZlV2zCBmj6gUZUpAFnU-auTv6kUOGJ2X8cFKDXyFuL9rTcxFgutEgG7n5a2VRUQsehLXoZpldnkZ5icmAQ_u744ZJyHmRV54Ehy6hdZ9F-RD5OnRxg_3XHZZnZCxzA5',
        'https://lh3.googleusercontent.com/aida-public/AB6AXuDi6Hw-oxJfN8Cg6PoVpP4d06geuyodEA-RY-r5l9OF4kYf1AgejrywByZdYV_A63lVGRBChWpbDVsVOtqpUe2MfqFlvxPA4_5rxADdCQo6RIfrwytRgRDZqV37fxsTSuz6tvysG_AWiGCE-f71natSNHhO8fXZtQkDzog55xiCxp6jypV1-QNtOMoyiQMc4unIQnbxHaHp_-sqVciGBWWUSrI72L3IvX0TISrl2I-ygVO9EFVBlt16'
      ]
    },
    {
      id: 'pub-203',
      roomNumber: 'Phòng 203',
      floor: 2,
      floorName: 'Tầng 2',
      area: 25,
      type: 'Phòng đơn tiêu chuẩn cửa sổ trời thoáng mát',
      price: 4600000,
      status: 'available',
      statusLabel: 'Trống sẵn sàng',
      capacity: '1-2 người',
      features: ['25 m²', '1-2 người', 'Máy lạnh Daikin', 'Tủ âm tường'],
      images: [
        'https://lh3.googleusercontent.com/aida-public/AB6AXuCKgWI8PewDEYPvFDT0bRw5GobFjc1kv8rKncJVDVezLGXQAXu1qmxbheS1s1BcpOySDSpXguHtx7Xcpfc3sPeB4cGuic4XbV0O7FRDR-AN0PW2HkT58k6Rlqr1GqcHZ8GNxiuHUWIWbEw7XNTOCgY_sNe-zsRQmZ1xNCszplKQUaDTMnLD5p8a3YfmB2vSn_11V1Txa-iMZdOdeoOpaH6D11GKaxjvSfMtQ4HyogwciwO8bjan6298'
      ]
    },
    {
      id: 'pub-302',
      roomNumber: 'Phòng 302',
      floor: 3,
      floorName: 'Tầng 3',
      area: 35,
      type: 'Căn hộ Studio 1 Phòng ngủ riêng biệt',
      price: 5800000,
      status: 'available',
      statusLabel: 'Trống sẵn sàng',
      capacity: '2-3 người',
      features: ['35 m²', '2-3 người', 'Bếp riêng biệt', 'Đệm lò xo cao cấp'],
      images: [
        'https://lh3.googleusercontent.com/aida-public/AB6AXuAIdhdweI19dcfu-W_F4Q5WJ4qHfvx0Syl7SZl6IGLYmMjGfC_mIELrQLUbN_C-ssYeCk-4FF4DsKi3ayICeXLv3jPaoCQkJpxc5N32ILSiYnsjJRXoYh0TpLjLuNolYOiQAY9jfzBwTUR_Rg34EgAohqefATW9DaYBS8MBwg9kgJgGKxD_TARUXy0biFiRZFqPLyYPIJqOS1kXxtZcL64iRgx-G07cNzeINjFd5rpnrvy1RN2PwZ4W'
      ]
    },
    {
      id: 'pub-101',
      roomNumber: 'Phòng 101',
      floor: 1,
      floorName: 'Tầng Trệt',
      area: 24,
      type: 'Phòng tầng trệt lối đi riêng tiện gửi xe',
      price: 4200000,
      status: 'available',
      statusLabel: 'Trống sẵn sàng',
      capacity: '1-2 người',
      features: ['24 m²', '1-2 người', 'Lối đi riêng', 'WC khép kín'],
      images: [
        'https://lh3.googleusercontent.com/aida-public/AB6AXuAV1-yIgknsiEP3kcahT5ioAYBYLcsp9CHS3-hxSeVM-syG6YH_6A_yVLtKiDJZndsEEKXlC6fz9uZesde8DCxe68W0LdOF6ag1V26_SNIuSUu_cSWEKfuSIT_6v3EWdnUkHErLBMvB9SbZ62gBlM5ulcu3VmKk_h9KfFp-EGjbg1ViVuE1xMKXW1Vv4xlE2iSYdo5vj3urPIgDl2nNqyRAMCRVRPyqdQXj8mYpY-ArqPXvCTyClbDk'
      ]
    }
  ];

  const [selectedRoom, setSelectedRoom] = useState<Room>(publicRooms[0]);
  const [selectedArea, setSelectedArea] = useState('binh-thanh');
  const [selectedBudget, setSelectedBudget] = useState('3.5-5');
  const [selectedSize, setSelectedSize] = useState('20');
  const [selectedAmenities, setSelectedAmenities] = useState<string[]>(['Máy lạnh', 'Ban công thoáng']);
  const [isBookingModalOpen, setIsBookingModalOpen] = useState(false);
  const [bookingSuccessAlert, setBookingSuccessAlert] = useState<string | null>(null);

  const toggleAmenity = (name: string) => {
    if (selectedAmenities.includes(name)) {
      setSelectedAmenities(selectedAmenities.filter((a) => a !== name));
    } else {
      setSelectedAmenities([...selectedAmenities, name]);
    }
  };

  const handleBookingSubmit = (info: { name: string; phone: string; time: string }) => {
    setBookingSuccessAlert(
      `Đã gửi lịch hẹn xem phòng ${selectedRoom.roomNumber} thành công! Quản lý cơ sở (Anh Tuấn - 0918.421.905) sẽ liên hệ xác nhận với bạn qua Zalo: ${info.phone}.`
    );
    setTimeout(() => setBookingSuccessAlert(null), 6000);
  };

  return (
    <div className="flex flex-col w-full pb-16">
      {/* Search Hero & Multi-Parameter Filter Area */}
      <section
        className="relative w-full px-6 py-12 lg:py-16 overflow-hidden"
        style={{
          background: 'linear-gradient(135deg, rgb(7, 78, 71) 0%, rgb(13, 148, 136) 48%, rgb(17, 94, 89) 75%, rgb(174, 49, 21) 100%)'
        }}
      >
        <div className="max-w-7xl mx-auto flex flex-col gap-8 relative z-10">
          {/* Trust Header Badge & Value Proposition */}
          <div className="flex flex-col items-start gap-3 max-w-4xl">
            <div className="inline-flex items-center gap-2 px-4 py-1 rounded-full bg-white/15 backdrop-blur-md text-white border border-white/20 text-xs font-semibold shadow-sm">
              <span className="material-symbols-outlined text-sm text-[#ffdad2]">verified_user</span>
              <span>Dữ liệu thực tế • Minh bạch 100% • Miễn phí kết nối</span>
            </div>

            <h1 className="font-headline text-3xl lg:text-5xl font-bold text-white tracking-tight leading-tight drop-shadow-sm">
              Tìm Phòng Trọ Trực Tiếp Chính Chủ – Minh Bạch, Không Phí Môi Giới
            </h1>

            <p className="text-sm lg:text-base text-white/90 max-w-3xl leading-relaxed">
              Khách vãng lai không cần tài khoản vẫn tự do tra cứu phòng trống chuẩn xác theo thời gian thực, liên hệ trực tiếp chủ trọ qua số điện thoại hoặc Zalo cá nhân. Không giữ chỗ online, không chuyển tiền qua trung gian.
            </p>
          </div>

          {/* Elevated Filter Container */}
          <div className="w-full bg-surface-container-lowest rounded-2xl p-6 shadow-2xl border border-white/60 flex flex-col gap-6">
            <div className="grid grid-cols-1 md:grid-cols-12 gap-4 items-end">
              {/* Location */}
              <div className="md:col-span-4 flex flex-col gap-1.5">
                <label className="text-xs text-tertiary flex items-center gap-1 font-bold">
                  <span className="material-symbols-outlined text-base text-[#0d9488]">location_on</span>
                  Khu vực phòng trọ
                </label>
                <div className="relative">
                  <select
                    value={selectedArea}
                    onChange={(e) => setSelectedArea(e.target.value)}
                    className="w-full bg-surface-container-lowest border border-outline-variant/60 rounded-xl py-2.5 px-3 pr-8 text-on-surface text-xs font-medium appearance-none cursor-pointer focus:outline-none focus:ring-2 focus:ring-primary shadow-xs"
                  >
                    <option value="binh-thanh">Quận Bình Thạnh (P.26, Đinh Bộ Lĩnh, Hàng Xanh)</option>
                    <option value="quan-1">Quận 1 (Đa Kao, Bến Nghé, Tân Định)</option>
                    <option value="thu-duc">TP. Thủ Đức (Linh Trung, ĐH Quốc Gia)</option>
                    <option value="phu-nhuan">Quận Phú Nhuận (Phan Xích Long, P.7)</option>
                  </select>
                  <span className="material-symbols-outlined absolute right-2.5 top-1/2 -translate-y-1/2 text-on-surface-variant pointer-events-none text-base">
                    expand_more
                  </span>
                </div>
              </div>

              {/* Price */}
              <div className="md:col-span-3 flex flex-col gap-1.5">
                <label className="text-xs text-tertiary flex items-center gap-1 font-bold">
                  <span className="material-symbols-outlined text-base text-[#0d9488]">payments</span>
                  Khoảng ngân sách
                </label>
                <div className="relative">
                  <select
                    value={selectedBudget}
                    onChange={(e) => setSelectedBudget(e.target.value)}
                    className="w-full bg-surface-container-lowest border border-outline-variant/60 rounded-xl py-2.5 px-3 pr-8 text-on-surface text-xs font-medium appearance-none cursor-pointer focus:outline-none focus:ring-2 focus:ring-primary shadow-xs"
                  >
                    <option value="3.5-5">3.500.000 - 5.000.000 đ / tháng</option>
                    <option value="under-3.5">Dưới 3.500.000 đ</option>
                    <option value="5-7">5.000.000 - 7.000.000 đ</option>
                    <option value="all">Mọi mức giá</option>
                  </select>
                  <span className="material-symbols-outlined absolute right-2.5 top-1/2 -translate-y-1/2 text-on-surface-variant pointer-events-none text-base">
                    expand_more
                  </span>
                </div>
              </div>

              {/* Area */}
              <div className="md:col-span-2 flex flex-col gap-1.5">
                <label className="text-xs text-tertiary flex items-center gap-1 font-bold">
                  <span className="material-symbols-outlined text-base text-[#0d9488]">aspect_ratio</span>
                  Diện tích từ
                </label>
                <div className="relative">
                  <select
                    value={selectedSize}
                    onChange={(e) => setSelectedSize(e.target.value)}
                    className="w-full bg-surface-container-lowest border border-outline-variant/60 rounded-xl py-2.5 px-3 pr-8 text-on-surface text-xs font-medium appearance-none cursor-pointer focus:outline-none focus:ring-2 focus:ring-primary shadow-xs"
                  >
                    <option value="20">Từ 20 m² trở lên</option>
                    <option value="25">Từ 25 m² trở lên</option>
                    <option value="30">Từ 30 m² trở lên</option>
                  </select>
                  <span className="material-symbols-outlined absolute right-2.5 top-1/2 -translate-y-1/2 text-on-surface-variant pointer-events-none text-base">
                    expand_more
                  </span>
                </div>
              </div>

              {/* Search button */}
              <div className="md:col-span-3 flex">
                <button
                  type="button"
                  className="w-full h-10 px-4 rounded-xl text-white text-xs font-bold shadow-md hover:shadow-lg transition-all flex items-center justify-center gap-2 cursor-pointer active:scale-95"
                  style={{ background: 'linear-gradient(135deg, #fd6a49 0%, #ae3115 100%)' }}
                >
                  <span className="material-symbols-outlined text-base">search</span>
                  <span>Tìm phòng trống (14)</span>
                </button>
              </div>
            </div>

            {/* Amenities Pills & Compliance */}
            <div className="flex flex-col lg:flex-row lg:items-center justify-between gap-4 pt-3 border-t border-outline-variant/30">
              <div className="flex flex-wrap items-center gap-2">
                <span className="text-[11px] text-on-surface-variant uppercase tracking-wider font-bold mr-1">
                  Tiện ích:
                </span>
                {['Máy lạnh', 'Ban công thoáng', 'Tủ lạnh', 'Giờ tự do', 'Khóa vân tay', 'Thang máy', 'Chỗ để xe'].map((am) => {
                  const isSelected = selectedAmenities.includes(am);
                  return (
                    <button
                      key={am}
                      type="button"
                      onClick={() => toggleAmenity(am)}
                      className={`inline-flex items-center gap-1 px-3 py-1.5 rounded-full text-xs font-semibold cursor-pointer transition-all ${
                        isSelected
                          ? 'bg-[#008378] text-white shadow-xs'
                          : 'bg-surface-container text-on-surface-variant hover:bg-surface-container-high'
                      }`}
                    >
                      {isSelected && <span className="material-symbols-outlined text-xs">check</span>}
                      <span>{isSelected ? am : `+ ${am}`}</span>
                    </button>
                  );
                })}
              </div>

              <div className="inline-flex items-center gap-2 px-3 py-1 rounded-lg bg-surface-container-low text-tertiary border border-tertiary/20 text-xs font-medium self-start lg:self-auto shrink-0 shadow-xs">
                <span className="w-2 h-2 rounded-full bg-[#008378] animate-pulse"></span>
                <span>Chỉ hiển thị Cơ sở Công khai & Phòng TRỐNG thực tế (FR-SRC-02)</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Booking Alert Banner */}
      {bookingSuccessAlert && (
        <div className="max-w-7xl mx-auto w-full px-6 pt-4">
          <div className="p-4 rounded-2xl bg-emerald-50 border border-emerald-200 text-emerald-800 text-xs flex items-center justify-between shadow-sm animate-in fade-in">
            <div className="flex items-center gap-2">
              <span className="material-symbols-outlined text-lg text-emerald-600">task_alt</span>
              <span className="font-semibold">{bookingSuccessAlert}</span>
            </div>
            <button
              onClick={() => setBookingSuccessAlert(null)}
              className="text-emerald-700 hover:text-emerald-900"
            >
              <span className="material-symbols-outlined text-base">close</span>
            </button>
          </div>
        </div>
      )}

      {/* Interactive 2-Column Search & Live Detail Preview Layout */}
      <section className="max-w-7xl w-full mx-auto px-6 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">
          {/* LEFT COLUMN: Public Room Listings (7 cols) */}
          <div className="lg:col-span-7 flex flex-col gap-4">
            {/* Summary & Sort */}
            <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 bg-surface-container-lowest p-4 rounded-2xl shadow-xs border border-surface-container">
              <div>
                <h2 className="font-headline text-base font-bold text-on-surface">
                  14 phòng trống đã xác minh chính chủ
                </h2>
                <p className="text-xs text-on-surface-variant">Khu vực: Quận Bình Thạnh • Trực thuộc quản lý cơ sở TroPro</p>
              </div>

              <div className="flex items-center gap-2 shrink-0 text-xs">
                <span className="text-on-surface-variant">Sắp xếp:</span>
                <select className="bg-surface-container rounded-lg py-1 px-2.5 text-on-surface text-xs font-semibold focus:outline-none border-0 cursor-pointer">
                  <option>Mới cập nhật</option>
                  <option>Giá: Thấp đến Cao</option>
                  <option>Giá: Cao đến Thấp</option>
                  <option>Diện tích lớn nhất</option>
                </select>
              </div>
            </div>

            {/* Privacy Shield Notice (FR-SRC-04) */}
            <div className="bg-surface-container-low rounded-2xl p-4 flex items-center gap-3 border border-surface-container">
              <div className="w-8 h-8 rounded-full bg-surface-container flex items-center justify-center shrink-0 text-primary">
                <span className="material-symbols-outlined text-base">shield</span>
              </div>
              <p className="text-xs text-on-surface-variant leading-relaxed">
                <strong className="text-tertiary font-bold">Cam kết bảo mật (FR-SRC-04):</strong> Nền tảng tuyệt đối không công khai dữ liệu cá nhân, danh tính, hoặc lịch sử của khách thuê cũ/hiện tại trên cổng thông tin chung.
              </p>
            </div>

            {/* Listing Cards */}
            <div className="flex flex-col gap-4">
              {publicRooms.map((room) => {
                const isSelected = room.id === selectedRoom.id;

                return (
                  <article
                    key={room.id}
                    onClick={() => setSelectedRoom(room)}
                    className={`rounded-2xl p-4 transition-all cursor-pointer border relative ${
                      isSelected
                        ? 'bg-surface-container-lowest border-primary shadow-md ring-2 ring-primary/20 bg-gradient-to-r from-surface-container-low via-surface-container-lowest to-surface-container-lowest'
                        : 'bg-surface-container-lowest border-surface-container hover:shadow-md'
                    }`}
                  >
                    <div className="absolute top-4 right-4">
                      <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full bg-primary-container text-white text-[10px] font-bold">
                        <span className="material-symbols-outlined text-xs">check_circle</span>
                        {room.statusLabel}
                      </span>
                    </div>

                    <div className="flex flex-col sm:flex-row gap-4">
                      {/* Thumbnail */}
                      <div className="sm:w-44 h-36 rounded-xl overflow-hidden shrink-0 relative bg-surface-container">
                        <img
                          src={room.images?.[0]}
                          alt={room.roomNumber}
                          className="w-full h-full object-cover"
                        />
                        <div className="absolute bottom-2 left-2 bg-black/70 backdrop-blur-xs text-white px-2 py-0.5 rounded text-[10px] font-semibold">
                          {room.images?.length || 4} ảnh thực tế
                        </div>
                      </div>

                      {/* Content */}
                      <div className="flex flex-col justify-between flex-1 min-w-0 pr-8 sm:pr-0">
                        <div>
                          <div className="flex items-center gap-1.5 text-primary text-xs font-bold mb-1">
                            <span className="material-symbols-outlined text-sm">apartment</span>
                            <span>TroPro Đinh Bộ Lĩnh</span>
                            <span className="text-on-surface-variant">• {room.floorName}</span>
                          </div>

                          <h3 className="font-headline text-base font-bold text-on-surface truncate">
                            {room.roomNumber} - {room.type}
                          </h3>
                          <p className="text-xs text-on-surface-variant truncate mt-0.5">
                            Số 26 Đinh Bộ Lĩnh, Phường 26, Quận Bình Thạnh (Gần Hàng Xanh)
                          </p>

                          <div className="flex flex-wrap items-center gap-1.5 mt-2.5">
                            {room.features?.map((f) => (
                              <span key={f} className="px-2 py-0.5 rounded-md bg-surface-container text-on-surface-variant text-[11px] font-medium">
                                {f}
                              </span>
                            ))}
                          </div>
                        </div>

                        <div className="flex items-baseline justify-between pt-2 border-t border-surface-container mt-2">
                          <div className="flex items-baseline gap-1">
                            <span className="font-headline text-lg font-bold text-secondary tabular-nums">
                              {room.price.toLocaleString()} đ
                            </span>
                            <span className="text-xs text-on-surface-variant">/ tháng</span>
                          </div>

                          <span className={`inline-flex items-center gap-1 text-xs font-bold ${
                            isSelected ? 'text-primary' : 'text-on-surface-variant hover:text-primary'
                          }`}>
                            <span>{isSelected ? 'Đang xem chi tiết' : 'Xem phòng này →'}</span>
                            <span className="material-symbols-outlined text-sm">arrow_forward</span>
                          </span>
                        </div>
                      </div>
                    </div>
                  </article>
                );
              })}
            </div>
          </div>

          {/* RIGHT COLUMN: Live Selected Room Detail Preview (5 cols, sticky) */}
          <div className="lg:col-span-5 flex flex-col gap-4 sticky top-24">
            <div className="bg-surface-container-lowest rounded-2xl p-6 shadow-md border border-surface-container flex flex-col gap-4">
              {/* Detail Header & Status */}
              <div className="flex items-start justify-between gap-3">
                <div>
                  <div className="flex items-center gap-1.5 text-primary text-[11px] font-bold uppercase tracking-wider">
                    <span className="w-2 h-2 rounded-full bg-primary animate-pulse"></span>
                    <span>Phòng Trống Đang Xem</span>
                  </div>
                  <h3 className="font-headline text-xl font-bold text-on-surface mt-0.5">
                    {selectedRoom.roomNumber} • {selectedRoom.area}m²
                  </h3>
                  <p className="text-xs text-on-surface-variant">Cơ sở TroPro 26 Đinh Bộ Lĩnh, P.26, Q. Bình Thạnh</p>
                </div>

                <div className="text-right">
                  <span className="font-headline text-lg font-bold text-secondary tabular-nums">
                    {selectedRoom.price.toLocaleString()} đ
                  </span>
                  <span className="block text-[11px] text-on-surface-variant">Cọc: 1 tháng (5.0 tr)</span>
                </div>
              </div>

              {/* Photo Showcase */}
              <div className="flex flex-col gap-2">
                <div className="w-full h-52 rounded-xl overflow-hidden bg-surface-container relative">
                  <img
                    src={selectedRoom.images?.[0] || 'https://lh3.googleusercontent.com/aida-public/AB6AXuAbeHTXGMi2Ak9DG7MNQKnruaiycUEYlpWj_q6wUWrJnq5fdmSkY7B8_pGapw6kVFAB1P-EQeZE0DBzYvuk1qXaTdDhjvdB-L_MwzBszUMv2425N0Jcx4qxCsrVSj0xPt6ALgglNraXQVFU1YLjlh-i8AbKbfW6Ka9Qd0bOVCNnzAGVuM91poDy3ZkohY4o_ln1fA0bjfkgcf_ZRtIUMrPtVuCVGvGua7ETcuFS-UqzjLIOEnsCDun_'}
                    alt={selectedRoom.roomNumber}
                    className="w-full h-full object-cover"
                  />
                  <div className="absolute bottom-2 right-2 bg-black/75 backdrop-blur-xs text-white px-2.5 py-1 rounded-md text-[10px] font-bold flex items-center gap-1">
                    <span className="material-symbols-outlined text-xs">photo_camera</span>
                    <span>Ảnh chụp thực tế 100%</span>
                  </div>
                </div>

                {/* Thumbnail Strip */}
                <div className="grid grid-cols-3 gap-2">
                  <div className="h-16 rounded-lg overflow-hidden bg-surface-container">
                    <img
                      src="https://lh3.googleusercontent.com/aida-public/AB6AXuD6NQ7-M_XvKgsPw6T6E2ksdc-um2jvj1tvnFmzD7qwgO11K_jE0Lz8kRNXnkSQ7aJHeqgwDJ1_VvFa4muxr5y0RAmErWdktmsp3UbiKnWvIt3u1D3GgSMgQZlV2zCBmj6gUZUpAFnU-auTv6kUOGJ2X8cFKDXyFuL9rTcxFgutEgG7n5a2VRUQsehLXoZpldnkZ5icmAQ_u744ZJyHmRV54Ehy6hdZ9F-RD5OnRxg_3XHZZnZCxzA5"
                      alt="Balcony"
                      className="w-full h-full object-cover"
                    />
                  </div>
                  <div className="h-16 rounded-lg overflow-hidden bg-surface-container">
                    <img
                      src="https://lh3.googleusercontent.com/aida-public/AB6AXuDi6Hw-oxJfN8Cg6PoVpP4d06geuyodEA-RY-r5l9OF4kYf1AgejrywByZdYV_A63lVGRBChWpbDVsVOtqpUe2MfqFlvxPA4_5rxADdCQo6RIfrwytRgRDZqV37fxsTSuz6tvysG_AWiGCE-f71natSNHhO8fXZtQkDzog55xiCxp6jypV1-QNtOMoyiQMc4unIQnbxHaHp_-sqVciGBWWUSrI72L3IvX0TISrl2I-ygVO9EFVBlt16"
                      alt="Bathroom"
                      className="w-full h-full object-cover"
                    />
                  </div>
                  <div className="h-16 rounded-lg overflow-hidden bg-surface-container">
                    <img
                      src="https://lh3.googleusercontent.com/aida-public/AB6AXuCul0KOjIQvsC93H2EBECjawICZhmC2PsbRnoGcC9a44JHloyGXCYMcKSGkEz3TwRKeInA8cEoD-Bsn24Zm-YT1geIRVen-kvlQSZkbSperJpogctkMOYLOOLGwVjd-dgB1HL7q63DBbVvJMMAAFbKJSUwQ2gCagybJ7sapCTT6gM5NoCbeU1xnHgzgWyv1NStDdhaBffFPJyHlRirJY8yGN5rl6UAtmh6e0BOnzPEiwFnnBw6VOe-c"
                      alt="Desk"
                      className="w-full h-full object-cover"
                    />
                  </div>
                </div>
              </div>

              {/* Transparent Utility Pricing */}
              <div className="bg-surface-container-low rounded-xl p-4 flex flex-col gap-2 border border-surface-container">
                <div className="flex items-center justify-between pb-1">
                  <span className="text-xs font-bold text-on-surface flex items-center gap-1">
                    <span className="material-symbols-outlined text-base text-primary">receipt_long</span>
                    Bảng giá dịch vụ niêm yết
                  </span>
                  <span className="text-[11px] text-tertiary font-medium">Cam kết không phát sinh</span>
                </div>

                <div className="grid grid-cols-2 gap-2 text-xs">
                  <div className="flex items-center justify-between p-2 rounded-lg bg-surface-container-lowest">
                    <span className="text-on-surface-variant flex items-center gap-1">
                      <span className="material-symbols-outlined text-sm text-tertiary">bolt</span> Điện
                    </span>
                    <span className="font-bold text-on-surface">3.500 đ / kWh</span>
                  </div>

                  <div className="flex items-center justify-between p-2 rounded-lg bg-surface-container-lowest">
                    <span className="text-on-surface-variant flex items-center gap-1">
                      <span className="material-symbols-outlined text-sm text-tertiary">water_drop</span> Nước
                    </span>
                    <span className="font-bold text-on-surface">25.000 đ / m³</span>
                  </div>

                  <div className="flex items-center justify-between p-2 rounded-lg bg-surface-container-lowest">
                    <span className="text-on-surface-variant flex items-center gap-1">
                      <span className="material-symbols-outlined text-sm text-tertiary">wifi</span> Wifi + DV
                    </span>
                    <span className="font-bold text-on-surface">150.000 đ / th</span>
                  </div>

                  <div className="flex items-center justify-between p-2 rounded-lg bg-surface-container-lowest">
                    <span className="text-on-surface-variant flex items-center gap-1">
                      <span className="material-symbols-outlined text-sm text-tertiary">two_wheeler</span> Xe máy
                    </span>
                    <span className="font-bold text-primary">Miễn phí 2 xe</span>
                  </div>
                </div>
              </div>

              {/* Furniture */}
              <div className="flex flex-col gap-1.5">
                <span className="text-[11px] text-on-surface-variant uppercase tracking-wider font-semibold">
                  Nội thất bàn giao sẵn:
                </span>
                <div className="flex flex-wrap gap-1.5 text-xs">
                  <span className="px-2 py-1 rounded bg-surface-container flex items-center gap-1">
                    <span className="material-symbols-outlined text-sm text-primary">ac_unit</span> Daikin Inverter
                  </span>
                  <span className="px-2 py-1 rounded bg-surface-container flex items-center gap-1">
                    <span className="material-symbols-outlined text-sm text-primary">kitchen</span> Tủ lạnh Aqua 180L
                  </span>
                  <span className="px-2 py-1 rounded bg-surface-container flex items-center gap-1">
                    <span className="material-symbols-outlined text-sm text-primary">bed</span> Giường gỗ sồi & Nệm
                  </span>
                  <span className="px-2 py-1 rounded bg-surface-container flex items-center gap-1">
                    <span className="material-symbols-outlined text-sm text-primary">hot_tub</span> Ariston 20L
                  </span>
                </div>
              </div>

              {/* Map Snapshot */}
              <div className="flex flex-col gap-1.5">
                <div className="flex items-center justify-between text-xs">
                  <span className="text-[11px] text-on-surface-variant uppercase tracking-wider font-semibold">
                    Vị trí trên bản đồ:
                  </span>
                  <span className="text-primary font-bold">Gần ĐH Ngoại Thương • Hutech</span>
                </div>
                <div
                  className="w-full h-28 rounded-xl bg-cover bg-center relative overflow-hidden border border-surface-container"
                  style={{
                    backgroundImage: `url('https://lh3.googleusercontent.com/aida-public/AB6AXuD24FOdU2pohjGqKC-c5v-yN8HJQkbzSzUewdZG3IVFzhuAil7nI6yIBTQ7czZt8gGrvPFhEpXA9VsNOxzfEjkFS1Y4wWqWEtiVSVptemP77CKbIqLIBsC-mX6kRRCzYzHkEG_fTJCBqEqPTQcSMjd8rriKlIN7PdpbAWvTfBv_IoKtMvdFK77tasG9_NWHTDcpmu29zmEqmd4Ac12owRYuzGyoeyBuQ2OVOflqqLBXVsvf5-rxWG5O')`
                  }}
                >
                  <div className="absolute inset-0 bg-black/10 flex items-center justify-center">
                    <div className="bg-white/95 px-3 py-1 rounded-full shadow-md flex items-center gap-1 text-on-surface text-xs font-bold">
                      <span className="material-symbols-outlined text-secondary text-base">location_on</span>
                      <span>26 Đinh Bộ Lĩnh, Bình Thạnh</span>
                    </div>
                  </div>
                </div>
              </div>

              {/* Direct Host Contact Actions */}
              <div className="pt-1 flex flex-col gap-2.5">
                <div className="p-3 rounded-xl bg-surface-container flex items-start gap-2.5 text-xs border border-surface-container-high">
                  <span className="material-symbols-outlined text-secondary text-lg shrink-0 mt-0.5">warning</span>
                  <p className="text-[11px] leading-relaxed text-on-surface-variant">
                    <strong className="text-secondary font-bold">Lưu ý an toàn:</strong> TroPro <strong>KHÔNG</strong> nhận đặt cọc hoặc giữ chỗ online qua ví trung gian. Quý khách vui lòng gọi điện thoại trực tiếp Quản lý cơ sở hoặc đến tận nơi khảo sát phòng trước khi ký hợp đồng.
                  </p>
                </div>

                <a
                  href="tel:0918421905"
                  className="w-full h-11 px-4 rounded-xl bg-primary text-white hover:bg-primary-container transition-colors flex items-center justify-center gap-2 text-xs font-bold shadow-sm"
                >
                  <span className="material-symbols-outlined text-lg">call</span>
                  <span>Gọi Quản lý Cơ sở: 0918.421.905 (Anh Tuấn)</span>
                </a>

                <a
                  href="https://zalo.me"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="w-full h-10 px-4 rounded-xl bg-tertiary text-white hover:opacity-95 transition-opacity flex items-center justify-center gap-2 text-xs font-bold shadow-xs"
                >
                  <span className="material-symbols-outlined text-lg">chat</span>
                  <span>Nhắn tin Zalo Cơ sở Đinh Bộ Lĩnh</span>
                </a>

                <button
                  type="button"
                  onClick={() => setIsBookingModalOpen(true)}
                  className="w-full py-2.5 rounded-xl bg-surface-container text-tertiary hover:bg-surface-container-high transition-colors text-xs font-bold flex items-center justify-center gap-1.5 cursor-pointer"
                >
                  <span className="material-symbols-outlined text-base">calendar_month</span>
                  <span>Hẹn lịch xem phòng trực tiếp (Hôm nay / Ngày mai)</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Safety & Rental Process Walkthrough */}
      <section id="safety-guide" className="w-full bg-surface-container-lowest py-16 px-6 mt-12 border-t border-surface-container">
        <div className="max-w-7xl mx-auto flex flex-col gap-10">
          <div className="text-center max-w-2xl mx-auto flex flex-col items-center gap-2">
            <span className="px-3 py-1 rounded-full bg-surface-container text-primary text-xs font-bold">
              Chuẩn Mực Minh Bạch
            </span>
            <h2 className="font-headline text-2xl lg:text-3xl font-bold text-on-surface">
              Quy trình thuê phòng an toàn 4 bước tại TroPro
            </h2>
            <p className="text-xs text-on-surface-variant leading-relaxed">
              Đảm bảo không phí môi giới, tin phòng chính xác 100%, bảo vệ quyền lợi pháp lý trọn vẹn cho người thuê nhà.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
            <div className="bg-surface-container-low rounded-2xl p-5 flex flex-col gap-2 border border-surface-container">
              <div className="w-9 h-9 rounded-full bg-primary text-white flex items-center justify-center font-headline text-base font-bold">
                1
              </div>
              <h3 className="font-headline text-sm font-bold text-on-surface mt-1">Xem phòng thực tế</h3>
              <p className="text-xs text-on-surface-variant leading-relaxed">
                Liên hệ trực tiếp Quản lý cơ sở để đến xem hiện trạng phòng thực tế, kiểm tra trang thiết bị và vị trí thực tế tại khu trọ.
              </p>
            </div>

            <div className="bg-surface-container-low rounded-2xl p-5 flex flex-col gap-2 border border-surface-container">
              <div className="w-9 h-9 rounded-full bg-primary text-white flex items-center justify-center font-headline text-base font-bold">
                2
              </div>
              <h3 className="font-headline text-sm font-bold text-on-surface mt-1">Thống nhất điều khoản</h3>
              <p className="text-xs text-on-surface-variant leading-relaxed">
                Rà soát giá thuê niêm yết, định mức điện nước, chính sách số lượng người ở, chỗ để xe máy cùng Quản lý tòa nhà.
              </p>
            </div>

            <div className="bg-surface-container-low rounded-2xl p-5 flex flex-col gap-2 border border-surface-container">
              <div className="w-9 h-9 rounded-full bg-primary text-white flex items-center justify-center font-headline text-base font-bold">
                3
              </div>
              <h3 className="font-headline text-sm font-bold text-on-surface mt-1">Ký hợp đồng điện tử</h3>
              <p className="text-xs text-on-surface-variant leading-relaxed">
                Ký hợp đồng thuê pháp lý rõ ràng thông qua mã OTP SMS chính chủ gửi về điện thoại, lưu trữ minh bạch trên hệ thống TroPro.
              </p>
            </div>

            <div className="bg-surface-container-low rounded-2xl p-5 flex flex-col gap-2 border border-surface-container">
              <div className="w-9 h-9 rounded-full bg-secondary-container text-white flex items-center justify-center font-headline text-base font-bold">
                4
              </div>
              <h3 className="font-headline text-sm font-bold text-on-surface mt-1">Thanh toán trực tiếp</h3>
              <p className="text-xs text-on-surface-variant leading-relaxed">
                Chuyển tiền cọc và tiền phòng trực tiếp vào tài khoản ngân hàng chính chủ của Chủ trọ thông qua VietQR có xuất biên nhận.
              </p>
            </div>
          </div>

          {/* Hotline banner */}
          <div className="rounded-2xl bg-surface-container p-6 flex flex-col sm:flex-row items-center justify-between gap-4 border border-surface-container-high">
            <div className="flex items-center gap-4">
              <div className="w-12 h-12 rounded-full bg-primary text-white flex items-center justify-center shrink-0">
                <span className="material-symbols-outlined text-2xl">support_agent</span>
              </div>
              <div>
                <h4 className="font-headline text-sm font-bold text-on-surface">Cần hỗ trợ hướng dẫn đi xem phòng miễn phí?</h4>
                <p className="text-xs text-on-surface-variant">Đội ngũ hỗ trợ khách thuê TroPro sẵn sàng giải đáp mọi thắc mắc về khu vực và giá cả.</p>
              </div>
            </div>

            <a
              href="tel:19006868"
              className="px-5 py-2.5 rounded-xl bg-primary text-white hover:bg-primary-container text-xs font-bold transition-colors flex items-center gap-1.5 shadow-sm shrink-0"
            >
              <span className="material-symbols-outlined text-base">phone_in_talk</span>
              <span>Hotline: 1900 6868 (Miễn cước)</span>
            </a>
          </div>
        </div>
      </section>

      {/* Booking Modal */}
      <BookingModal
        room={selectedRoom}
        isOpen={isBookingModalOpen}
        onClose={() => setIsBookingModalOpen(false)}
        onSuccess={handleBookingSubmit}
      />
    </div>
  );
};
