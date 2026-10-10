import React, { useState } from 'react';
import { SeekingThread } from '../../types';

interface ThreadMatcherManagerViewProps {
  threads: SeekingThread[];
  onSendProposal: (threadId: string, roomCode: string, message: string) => void;
}

export const ThreadMatcherManagerView: React.FC<ThreadMatcherManagerViewProps> = ({
  threads,
  onSendProposal
}) => {
  const [selectedThreadId, setSelectedThreadId] = useState(threads[0]?.id || 'thr-1');
  const [selectedRoomToPropose, setSelectedRoomToPropose] = useState('401');
  const [proposalMsg, setProposalMsg] = useState(
    'Chào bạn Minh Trang, cơ sở TroPro Đinh Bộ Lĩnh (cách Hàng Xanh 300m) bên mình hiện sẵn sàng duy nhất Phòng 401 tầng 4 đúng y hệt tiêu chí của bạn: 28m2 rộng rãi, ban công riêng đón gió mát, máy lạnh Daikin mới bảo trì và giờ giấc tự do thẻ từ 24/7. Mời bạn ghé xem trực tiếp nhé!'
  );
  const [comments, setComments] = useState<Array<{ name: string; time: string; text: string; isTenant?: boolean }>>([
    {
      name: 'Bất Động Sản An Cư Bình Thạnh',
      time: '1 giờ trước',
      text: 'Bên mình có phòng gần ngã tư Bạch Đằng nhưng giá 5.8tr hơi nhích nhẹ so với ngân sách của bạn một xíu, nếu bạn quan tâm có thể nhắn mình nhé!'
    },
    {
      name: 'Minh Trang (Người đăng)',
      time: '45 phút trước',
      text: 'Dạ em cảm ơn ạ, nhưng tụi em chỉ cố tối đa 5.5tr thôi ạ, vì còn chi phí điện nước nữa. Em ưu tiên các phòng đúng khoảng giá này ạ!',
      isTenant: true
    }
  ]);
  const [newCommentText, setNewCommentText] = useState('');

  const selectedThread = threads.find((t) => t.id === selectedThreadId) || threads[0];

  const handlePostComment = () => {
    if (!newCommentText.trim()) return;
    setComments([
      ...comments,
      {
        name: 'Nguyễn Văn Nam (Quản lý TroPro)',
        time: 'Vừa xong',
        text: newCommentText
      }
    ]);
    setNewCommentText('');
  };

  const handlePropose = () => {
    onSendProposal(selectedThread.id, selectedRoomToPropose, proposalMsg);
  };

  return (
    <div className="flex flex-col w-full p-6 lg:p-8 gap-6 max-w-[1600px] mx-auto">
      {/* Top Operations & Match KPI Header Bar */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-1.5 text-primary text-xs uppercase tracking-wider font-bold">
            <span className="material-symbols-outlined text-base">hub</span>
            <span>Hệ thống Tự Động So Khớp BR-07 & BR-08</span>
          </div>
          <h1 className="font-headline text-2xl lg:text-3xl font-bold text-on-surface mt-1 tracking-tight">
            Kênh Thread Tìm Trọ - Cơ hội ghép khách trực tiếp
          </h1>
          <p className="text-xs text-on-surface-variant font-medium mt-0.5">
            Hệ thống tự động so khớp phòng trống của cơ sở với các tiêu chí thời gian thực của người thuê trọ.
          </p>
        </div>

        {/* Facility Match Analytics Chips */}
        <div className="flex items-center gap-3 flex-wrap">
          <div className="flex items-center gap-3 px-4 py-2 rounded-xl bg-surface-container-lowest shadow-xs border border-surface-container">
            <div className="w-8 h-8 rounded-lg bg-surface-container flex items-center justify-center text-primary">
              <span className="material-symbols-outlined text-base">hotel</span>
            </div>
            <div className="flex flex-col">
              <span className="text-[10px] text-on-surface-variant font-medium">Phòng trống cơ sở</span>
              <span className="font-headline text-sm font-bold text-primary">
                03 <span className="text-xs text-on-surface-variant font-normal">/ 18 phòng</span>
              </span>
            </div>
          </div>

          <div className="flex items-center gap-3 px-4 py-2 rounded-xl bg-surface-container-lowest shadow-xs border border-surface-container">
            <div className="w-8 h-8 rounded-lg bg-primary/10 flex items-center justify-center text-primary">
              <span className="material-symbols-outlined text-base">bolt</span>
            </div>
            <div className="flex flex-col">
              <span className="text-[10px] text-on-surface-variant font-medium">Khớp 100% tiêu chí</span>
              <span className="font-headline text-sm font-bold text-primary">01 Thread</span>
            </div>
          </div>

          <div className="flex items-center gap-3 px-4 py-2 rounded-xl bg-secondary-container/10 border border-secondary-container/20">
            <div className="w-8 h-8 rounded-lg bg-secondary-container/20 flex items-center justify-center text-secondary-container">
              <span className="material-symbols-outlined text-base">send</span>
            </div>
            <div className="flex flex-col">
              <span className="text-[10px] text-on-surface-variant font-medium">Đề xuất chờ duyệt</span>
              <span className="font-headline text-sm font-bold text-secondary">02 lượt</span>
            </div>
          </div>
        </div>
      </div>

      {/* Filter Bar Card */}
      <div className="bg-surface-container-lowest rounded-2xl p-4 shadow-xs border border-surface-container flex flex-col lg:flex-row items-stretch lg:items-center justify-between gap-4">
        <div className="flex flex-wrap items-center gap-3 flex-1">
          {/* Area */}
          <div className="flex items-center gap-2 px-3.5 py-1.5 bg-surface-container-low rounded-xl text-xs font-semibold">
            <span className="material-symbols-outlined text-primary text-base">location_on</span>
            <span className="text-on-surface-variant">Khu vực:</span>
            <span className="text-on-surface">Bình Thạnh (Đinh Bộ Lĩnh / P.26)</span>
          </div>

          {/* Budget */}
          <div className="flex items-center gap-2 px-3.5 py-1.5 bg-surface-container-low rounded-xl text-xs font-semibold">
            <span className="material-symbols-outlined text-primary text-base">payments</span>
            <span className="text-on-surface-variant">Khoảng giá:</span>
            <span className="text-on-surface">Tất cả mức giá</span>
          </div>

          {/* Amenities */}
          <div className="flex items-center gap-2 px-3.5 py-1.5 bg-surface-container-low rounded-xl text-xs font-semibold">
            <span className="material-symbols-outlined text-primary text-base">tune</span>
            <span className="text-on-surface-variant">Tiện ích:</span>
            <span className="text-on-surface">Máy lạnh, Ban công...</span>
          </div>
        </div>

        {/* Priority Badge */}
        <div className="flex items-center gap-2 bg-primary/10 px-3.5 py-1.5 rounded-xl self-start lg:self-auto cursor-pointer">
          <span className="material-symbols-outlined text-primary text-base">auto_awesome</span>
          <span className="text-xs font-bold text-primary">Chế độ ưu tiên khớp 100%</span>
          <span className="w-2 h-2 rounded-full bg-primary animate-pulse"></span>
        </div>
      </div>

      {/* Main Dual Panel Workspace */}
      <div className="grid grid-cols-1 xl:grid-cols-12 gap-8 items-start">
        {/* LEFT PANEL: Seeking Threads List (5 cols) */}
        <div className="xl:col-span-5 flex flex-col gap-4">
          <div className="flex items-center justify-between px-1">
            <div className="flex items-center gap-2">
              <span className="font-headline text-base font-bold text-on-surface">Thread Đang Tìm Trọ</span>
              <span className="px-2 py-0.5 rounded-full bg-surface-container-high text-primary text-xs font-bold">
                14 bài mới
              </span>
            </div>
            <span className="text-xs text-on-surface-variant">Cập nhật 1 phút trước</span>
          </div>

          {threads.map((thread) => {
            const isSelected = thread.id === selectedThreadId;
            const isMatched = thread.matchPercent === 100;

            return (
              <div
                key={thread.id}
                onClick={() => setSelectedThreadId(thread.id)}
                className={`rounded-2xl p-4 shadow-xs border transition-all cursor-pointer flex flex-col gap-3 relative overflow-hidden ${
                  isSelected
                    ? 'bg-surface-container-lowest border-primary ring-2 ring-primary/20 shadow-md bg-gradient-to-r from-primary-fixed/20 via-transparent to-transparent'
                    : 'bg-surface-container-lowest border-surface-container hover:shadow-md'
                }`}
              >
                {/* Header */}
                <div className="flex items-start justify-between gap-3">
                  <div className="flex items-center gap-3">
                    <img
                      src={thread.authorAvatar}
                      alt={thread.authorName}
                      className="w-10 h-10 rounded-full object-cover shadow-sm ring-1 ring-primary/20"
                    />
                    <div>
                      <div className="flex items-center gap-1.5">
                        <span className="text-xs font-bold text-on-surface">{thread.authorName}</span>
                        {thread.isVerified && (
                          <span className="material-symbols-outlined text-primary text-sm" title="Đã xác thực CCCD">
                            verified
                          </span>
                        )}
                      </div>
                      <span className="text-[11px] text-on-surface-variant">{thread.timeAgo} · {thread.authorRole}</span>
                    </div>
                  </div>

                  <span className={`px-2.5 py-0.5 rounded-full text-[11px] font-bold flex items-center gap-1 ${
                    thread.status === 'matched_100'
                      ? 'bg-primary/10 text-primary'
                      : 'bg-surface-container text-on-surface-variant'
                  }`}>
                    <span className="w-1.5 h-1.5 rounded-full bg-primary animate-ping"></span>
                    Đang mở
                  </span>
                </div>

                {/* Title & Preview */}
                <h3 className="font-headline text-sm font-bold text-on-surface line-clamp-2">
                  {thread.title}
                </h3>
                <p className="text-xs text-on-surface-variant line-clamp-2 leading-relaxed">
                  {thread.content}
                </p>

                {/* Requirement Metrics */}
                <div className="grid grid-cols-2 gap-2 bg-surface-container-low p-3 rounded-xl text-xs">
                  <div className="flex items-center gap-1">
                    <span className="material-symbols-outlined text-primary text-sm">payments</span>
                    <span className="text-on-surface-variant">Ngân sách:</span>
                    <span className="font-bold text-primary tabular-nums">
                      Tối đa {(thread.maxBudget / 1000000).toFixed(1)}tr
                    </span>
                  </div>
                  <div className="flex items-center gap-1">
                    <span className="material-symbols-outlined text-primary text-sm">aspect_ratio</span>
                    <span className="text-on-surface-variant">Diện tích:</span>
                    <span className="font-bold text-on-surface">≥ {thread.minArea}m²</span>
                  </div>
                  <div className="flex items-center gap-1">
                    <span className="material-symbols-outlined text-primary text-sm">group</span>
                    <span className="text-on-surface-variant">Số lượng:</span>
                    <span className="font-bold text-on-surface">{thread.occupants}</span>
                  </div>
                  <div className="flex items-center gap-1">
                    <span className="material-symbols-outlined text-primary text-sm">near_me</span>
                    <span className="text-on-surface-variant">Vị trí:</span>
                    <span className="font-bold text-on-surface truncate">{thread.location.split('(')[0]}</span>
                  </div>
                </div>

                {/* Amenities */}
                <div className="flex flex-wrap gap-1.5">
                  {thread.amenities.map((am) => (
                    <span key={am} className="px-2 py-0.5 rounded-md bg-surface-container text-on-surface-variant text-[11px] font-medium">
                      {am}
                    </span>
                  ))}
                </div>

                {/* Match Banner or Warning */}
                {isMatched ? (
                  <div className="flex items-center justify-between p-2.5 rounded-xl bg-primary text-white shadow-xs">
                    <div className="flex items-center gap-2">
                      <span className="material-symbols-outlined text-base animate-bounce">bolt</span>
                      <span className="text-xs font-bold tracking-wide">
                        CÓ 1 PHÒNG CỦA BẠN KHỚP 100% (Phòng 401)
                      </span>
                    </div>
                    <span className="material-symbols-outlined text-base">arrow_forward</span>
                  </div>
                ) : (
                  <div className="flex items-center gap-2 p-2.5 rounded-xl bg-surface-container-high text-on-surface-variant text-xs font-medium">
                    <span className="material-symbols-outlined text-outline text-base">info</span>
                    <span>Không có phòng nào khớp mức giá này (Phòng thấp nhất 4.8tr)</span>
                  </div>
                )}
              </div>
            );
          })}
        </div>

        {/* RIGHT PANEL: Detailed Thread & Propose Workspace (7 cols) */}
        <div className="xl:col-span-7 flex flex-col gap-6">
          <div className="bg-surface-container-lowest rounded-2xl p-6 shadow-xs border border-surface-container flex flex-col gap-6">
            {/* Thread User Header */}
            <div className="flex items-start justify-between pb-4 border-b border-surface-container">
              <div className="flex items-center gap-3">
                <img
                  src={selectedThread.authorAvatar}
                  alt={selectedThread.authorName}
                  className="w-14 h-14 rounded-full object-cover shadow-sm ring-2 ring-primary/20"
                />
                <div>
                  <div className="flex items-center gap-2">
                    <span className="font-headline text-base font-bold text-on-surface">
                      {selectedThread.authorName}
                    </span>
                    <span className="px-2.5 py-0.5 rounded-full bg-primary/10 text-primary text-[11px] font-bold">
                      {selectedThread.authorRole}
                    </span>
                  </div>
                  <div className="flex items-center gap-4 text-on-surface-variant text-xs mt-1">
                    <span className="flex items-center gap-1">
                      <span className="material-symbols-outlined text-sm">schedule</span> {selectedThread.timeAgo}
                    </span>
                    <span className="flex items-center gap-1">
                      <span className="material-symbols-outlined text-sm">visibility</span> {selectedThread.viewsCount} xem
                    </span>
                    <span className="flex items-center gap-1 text-primary font-bold">
                      <span className="material-symbols-outlined text-sm">forum</span> {comments.length} phản hồi
                    </span>
                  </div>
                </div>
              </div>
            </div>

            {/* Title & Full Text */}
            <div>
              <h2 className="font-headline text-lg font-bold text-on-surface mb-2">
                {selectedThread.title}
              </h2>
              <p className="text-xs text-on-surface leading-relaxed">
                {selectedThread.content}
              </p>
            </div>

            {/* Auto-matching matrix evaluation */}
            <div className="flex flex-col gap-2">
              <div className="flex items-center justify-between">
                <span className="text-xs font-bold text-primary uppercase tracking-wider">
                  Bảng Tiêu Chí So Khớp Hệ Thống (TroPro Auto-matcher Engine)
                </span>
                <span className="px-2.5 py-0.5 rounded-full bg-primary-fixed text-on-primary-fixed text-xs font-bold">
                  ĐẠT 5/5 TIÊU CHÍ (100%)
                </span>
              </div>

              <div className="grid grid-cols-2 md:grid-cols-3 gap-2 text-xs">
                <div className="bg-surface-container-low p-2.5 rounded-xl flex items-center justify-between border border-surface-container">
                  <div>
                    <span className="text-[11px] text-on-surface-variant block">Khu vực tìm</span>
                    <span className="font-bold text-on-surface">P.26, Bình Thạnh</span>
                  </div>
                  <span className="material-symbols-outlined text-primary text-base">check_circle</span>
                </div>

                <div className="bg-surface-container-low p-2.5 rounded-xl flex items-center justify-between border border-surface-container">
                  <div>
                    <span className="text-[11px] text-on-surface-variant block">Ngân sách tối đa</span>
                    <span className="font-bold text-on-surface">5.500.000đ/tháng</span>
                  </div>
                  <span className="material-symbols-outlined text-primary text-base">check_circle</span>
                </div>

                <div className="bg-surface-container-low p-2.5 rounded-xl flex items-center justify-between border border-surface-container">
                  <div>
                    <span className="text-[11px] text-on-surface-variant block">Diện tích tối thiểu</span>
                    <span className="font-bold text-on-surface">≥ 25m²</span>
                  </div>
                  <span className="material-symbols-outlined text-primary text-base">check_circle</span>
                </div>

                <div className="bg-surface-container-low p-2.5 rounded-xl flex items-center justify-between border border-surface-container">
                  <div>
                    <span className="text-[11px] text-on-surface-variant block">Tiện ích: Ban công</span>
                    <span className="font-bold text-on-surface">Bắt buộc</span>
                  </div>
                  <span className="material-symbols-outlined text-primary text-base">check_circle</span>
                </div>

                <div className="bg-surface-container-low p-2.5 rounded-xl flex items-center justify-between border border-surface-container">
                  <div>
                    <span className="text-[11px] text-on-surface-variant block">Tiện ích: Máy lạnh</span>
                    <span className="font-bold text-on-surface">Bắt buộc</span>
                  </div>
                  <span className="material-symbols-outlined text-primary text-base">check_circle</span>
                </div>

                <div className="bg-surface-container-low p-2.5 rounded-xl flex items-center justify-between border border-surface-container">
                  <div>
                    <span className="text-[11px] text-on-surface-variant block">Quy mô ở</span>
                    <span className="font-bold text-on-surface">2 người</span>
                  </div>
                  <span className="material-symbols-outlined text-primary text-base">check_circle</span>
                </div>
              </div>
            </div>

            {/* CORE PROPOSAL MODULE (BR-07 / BR-08) */}
            <div className="bg-surface-container p-5 rounded-2xl shadow-xs border border-primary/20 flex flex-col gap-4 relative overflow-hidden">
              <div className="flex items-center gap-3">
                <div className="w-8 h-8 rounded-full bg-secondary-container text-white flex items-center justify-center font-bold">
                  <span className="material-symbols-outlined text-base">verified</span>
                </div>
                <div>
                  <h4 className="font-headline text-sm font-bold text-on-surface">
                    Gửi Đề Xuất Phòng Khớp Tiêu Chí (BR-07)
                  </h4>
                  <p className="text-[11px] text-on-surface-variant">
                    Chỉ cho phép đề xuất khi phòng <strong>TRỐNG</strong> và <strong>KHỚP 100%</strong> các yêu cầu cứng của người tìm trọ.
                  </p>
                </div>
              </div>

              {/* Room Selection */}
              <div className="flex flex-col gap-1.5">
                <label className="text-xs font-bold text-primary">
                  Chọn phòng từ Cơ sở Bình Thạnh - Đinh Bộ Lĩnh:
                </label>
                <select
                  value={selectedRoomToPropose}
                  onChange={(e) => setSelectedRoomToPropose(e.target.value)}
                  className="w-full bg-surface-container-lowest text-on-surface text-xs font-bold p-3 rounded-xl shadow-xs focus:outline-none focus:ring-2 focus:ring-primary border border-surface-container cursor-pointer"
                >
                  <option value="401">
                    Phòng 401 - Tầng 4 (Trống, 28m2, 5.000.000đ - Đầy đủ máy lạnh, ban công riêng) [Khớp 100%]
                  </option>
                  <option disabled className="text-outline bg-surface-container-low">
                    Phòng 102 - Tầng 1 (Trống, 32m2, 6.200.000đ) - [Bị khóa: Vượt ngân sách khách]
                  </option>
                  <option disabled className="text-outline bg-surface-container-low">
                    Phòng 303 - Tầng 3 (24m2, 4.800.000đ) - [Bị khóa: Đang có khách thuê đến T12]
                  </option>
                  <option disabled className="text-outline bg-surface-container-low">
                    Phòng 204 - Tầng 2 (Trống, 22m2, 4.500.000đ) - [Bị khóa: Diện tích dưới 25m2 & Không ban công]
                  </option>
                </select>
              </div>

              {/* Qualified Room Preview Card */}
              <div className="bg-surface-container-lowest p-3 rounded-xl flex flex-col sm:flex-row gap-3 items-center border border-surface-container">
                <img
                  src="https://lh3.googleusercontent.com/aida-public/AB6AXuCE-QOks5L8pF1lYqtEuWl6iuHTHBS9Gc-giekEM_wt_fNWY8myt-ZVDepZyYih9jFSQf1DGoWJIRVTSTOVnu0J6wOoyAb4yossSuikrhhAaNDzQ-vfQlsZezAA1TXEuVQCpn4YpRRaXFnXKZEjy0VohEjfYOltZSNizPkEeZ1Wr5xBsJDyBOpJGhir8vtpRCvQUEjW0-LyE7lRcnE3gDPVNaC2xIqQuqPvypGjyuvAWsn7oWec6_q7"
                  alt="Phòng 401"
                  className="w-full sm:w-28 h-20 rounded-lg object-cover"
                />
                <div className="flex-1 min-w-0">
                  <div className="flex items-center justify-between">
                    <span className="font-headline text-xs font-bold text-on-surface">
                      Phòng 401 - Studio Ban Công Thoáng
                    </span>
                    <span className="font-headline text-sm font-bold text-secondary">
                      5.000.000đ<span className="text-[11px] font-normal text-on-surface-variant">/th</span>
                    </span>
                  </div>
                  <div className="flex items-center gap-3 text-[11px] text-on-surface-variant mt-1">
                    <span>Diện tích: <strong>28 m²</strong></span>
                    <span>•</span>
                    <span>Tầng 4 (Thang máy)</span>
                    <span>•</span>
                    <span className="text-primary font-bold">Tình trạng: Sẵn sàng dọn vào</span>
                  </div>
                </div>
              </div>

              {/* Message */}
              <div className="flex flex-col gap-1.5">
                <label className="text-xs font-bold text-primary">
                  Lời nhắn gửi đính kèm từ Quản lý:
                </label>
                <textarea
                  value={proposalMsg}
                  onChange={(e) => setProposalMsg(e.target.value)}
                  rows={3}
                  className="w-full bg-surface-container-lowest text-on-surface text-xs p-3 rounded-xl shadow-xs focus:outline-none focus:ring-2 focus:ring-primary border border-surface-container leading-relaxed resize-none"
                />
              </div>

              {/* Submit CTA */}
              <div className="flex flex-col sm:flex-row items-center justify-between gap-3 pt-1">
                <div className="flex items-center gap-1.5 text-on-surface-variant text-[11px]">
                  <span className="material-symbols-outlined text-sm text-primary">policy</span>
                  <span>Đề xuất sẽ hiển thị thẻ phòng chính thức trong tin nhắn của khách</span>
                </div>
                <button
                  type="button"
                  onClick={handlePropose}
                  className="w-full sm:w-auto px-5 py-2.5 bg-secondary-container hover:bg-secondary text-white font-bold text-xs rounded-xl shadow-md hover:shadow-lg transition-all flex items-center justify-center gap-2 cursor-pointer active:scale-95"
                >
                  <span className="material-symbols-outlined text-base">forward_to_inbox</span>
                  <span>Gửi Đề Xuất Phòng Vào Thread</span>
                </button>
              </div>
            </div>

            {/* Public Discussion Area */}
            <div className="flex flex-col gap-3 pt-2">
              <div className="flex items-center justify-between">
                <span className="font-headline text-sm font-bold text-on-surface">
                  Trao Đổi & Thảo Luận Trong Thread ({comments.length})
                </span>
                <span className="text-[11px] text-on-surface-variant font-medium">
                  Chủ trọ / Quản lý luôn có thể bình luận hỗ trợ
                </span>
              </div>

              <div className="flex flex-col gap-2.5">
                {comments.map((cm, idx) => (
                  <div
                    key={idx}
                    className={`flex items-start gap-3 p-3 rounded-xl border ${
                      cm.isTenant
                        ? 'bg-primary/5 border-primary/20 pl-6'
                        : 'bg-surface-container-low border-surface-container'
                    }`}
                  >
                    <div className="flex-1">
                      <div className="flex items-center justify-between mb-1">
                        <span className={`text-xs font-bold ${cm.isTenant ? 'text-primary' : 'text-on-surface'}`}>
                          {cm.name}
                        </span>
                        <span className="text-[11px] text-on-surface-variant">{cm.time}</span>
                      </div>
                      <p className="text-xs text-on-surface leading-relaxed">{cm.text}</p>
                    </div>
                  </div>
                ))}
              </div>

              {/* New Comment Input */}
              <div className="flex items-center gap-2 pt-2">
                <input
                  type="text"
                  value={newCommentText}
                  onChange={(e) => setNewCommentText(e.target.value)}
                  onKeyDown={(e) => e.key === 'Enter' && handlePostComment()}
                  placeholder="Viết câu hỏi hoặc tư vấn thêm cho bạn Trang..."
                  className="flex-1 bg-surface-container-low px-4 py-2 rounded-xl text-xs text-on-surface focus:outline-none focus:bg-surface-container-lowest border border-surface-container"
                />
                <button
                  type="button"
                  onClick={handlePostComment}
                  className="px-4 py-2 bg-primary text-white rounded-xl text-xs font-bold hover:bg-primary-container transition-colors flex items-center gap-1 cursor-pointer"
                >
                  <span className="material-symbols-outlined text-sm">send</span>
                  <span>Gửi</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
