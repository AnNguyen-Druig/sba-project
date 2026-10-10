import React, { useState } from 'react';
import { Contract, ViewMode } from '../../types';

interface ContractsListViewProps {
  contracts: Contract[];
  onNavigate: (view: ViewMode) => void;
  onOpenContractDetail: (contractId: string) => void;
  onSignContract: (contractId: string) => void;
  onRemindTenant: (contractId: string) => void;
  onRenewContract: (contractId: string) => void;
}

export const ContractsListView: React.FC<ContractsListViewProps> = ({
  contracts,
  onNavigate,
  onOpenContractDetail,
  onSignContract,
  onRemindTenant,
  onRenewContract
}) => {
  const [filterTab, setFilterTab] = useState<'all' | 'active' | 'pending' | 'draft' | 'ended'>('all');
  const [warningFilter, setWarningFilter] = useState<'all' | 'expiring' | 'deposit'>('all');
  const [searchTerm, setSearchTerm] = useState('');

  const filteredContracts = contracts.filter((c) => {
    if (searchTerm.trim()) {
      const matchRoom = c.roomNumber.toLowerCase().includes(searchTerm.toLowerCase());
      const matchTenant = c.tenantName.toLowerCase().includes(searchTerm.toLowerCase());
      if (!matchRoom && !matchTenant) return false;
    }

    if (warningFilter === 'expiring' && c.status !== 'expiring') return false;
    if (warningFilter === 'deposit' && c.depositStatus !== 'partial') return false;

    if (filterTab === 'active') return c.status === 'active';
    if (filterTab === 'pending') return c.status === 'pending_tenant' || c.status === 'pending_host';
    if (filterTab === 'draft') return c.status === 'draft';
    if (filterTab === 'ended') return c.status === 'ended';
    return true;
  });

  return (
    <div className="flex flex-col w-full p-6 lg:p-8 gap-6 max-w-[1440px] mx-auto">
      {/* Page Header */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div className="flex flex-col gap-0.5">
          <h1 className="font-headline text-2xl lg:text-3xl font-bold text-[#2F5D58] tracking-tight">
            Hợp đồng · Cơ sở Bình Thạnh
          </h1>
          <p className="text-xs text-[#6C7D79] font-medium">
            32 đang hiệu lực · 3 chờ ký · 1 nháp
          </p>
        </div>

        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={() => alert('Đang mở thư viện 5 mẫu hợp đồng chuẩn hóa theo Luật Nhà ở 2024')}
            className="inline-flex items-center gap-2 px-3.5 py-2 rounded-xl bg-surface-container-lowest border border-[#D1E5E0] text-[#2F5D58] text-xs font-semibold hover:bg-[#F2FAF8] transition-colors shadow-xs cursor-pointer"
          >
            <span className="material-symbols-outlined text-base">description</span>
            <span>Mẫu hợp đồng</span>
          </button>

          <button
            type="button"
            onClick={() => onNavigate('create-contract')}
            className="inline-flex items-center gap-1.5 px-4 py-2 rounded-xl bg-[#F4A68C] text-[#5A2A1A] text-xs font-bold hover:bg-[#EF977A] transition-colors shadow-sm cursor-pointer active:scale-95"
          >
            <span className="material-symbols-outlined text-base font-bold">add</span>
            <span>+ Tạo hợp đồng</span>
          </button>
        </div>
      </div>

      {/* 3 Actionable Stat Cards ("Cần xử lý") */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        {/* Card 1: Chờ bạn ký */}
        <div className="bg-surface-container-lowest border border-[#E2EDE9] rounded-2xl p-4 flex flex-col justify-between gap-3 shadow-xs">
          <div className="flex items-start justify-between">
            <div className="flex items-center gap-2.5">
              <div className="w-8 h-8 rounded-lg bg-[#EBF7F4] flex items-center justify-center text-[#2F5D58]">
                <span className="material-symbols-outlined text-lg">edit_note</span>
              </div>
              <div className="flex items-center gap-2">
                <span className="text-xs text-[#2F5D58] font-bold">Đang chờ bạn ký</span>
                <span className="inline-flex items-center justify-center w-5 h-5 rounded-full bg-[#E8F7EE] text-[#1E7E34] text-[11px] font-bold">
                  1
                </span>
              </div>
            </div>
          </div>
          <div className="flex items-center justify-between pt-1">
            <span className="text-xs text-[#6C7D79] truncate mr-2">P.401 · bên thuê đã ký 2 ngày trước</span>
            <button
              type="button"
              onClick={() => onSignContract('c-2')}
              className="flex-shrink-0 px-3 py-1.5 rounded-lg bg-[#5FB3A8] text-white text-xs font-bold hover:bg-[#529E94] transition-colors cursor-pointer"
            >
              Ký ngay
            </button>
          </div>
        </div>

        {/* Card 2: Chờ người thuê ký */}
        <div className="bg-surface-container-lowest border border-[#E2EDE9] rounded-2xl p-4 flex flex-col justify-between gap-3 shadow-xs">
          <div className="flex items-start justify-between">
            <div className="flex items-center gap-2.5">
              <div className="w-8 h-8 rounded-lg bg-[#FEF6E7] flex items-center justify-center text-[#B57A08]">
                <span className="material-symbols-outlined text-lg">hourglass_top</span>
              </div>
              <div className="flex items-center gap-2">
                <span className="text-xs text-[#2F5D58] font-bold">Chờ người thuê ký</span>
                <span className="inline-flex items-center justify-center w-5 h-5 rounded-full bg-[#FEF6E7] text-[#B57A08] text-[11px] font-bold">
                  2
                </span>
              </div>
            </div>
          </div>
          <div className="flex items-center justify-between pt-1">
            <span className="text-xs text-[#6C7D79] truncate mr-2">P.204 chờ 3 ngày · đã gửi 2 lần</span>
            <button
              type="button"
              onClick={() => onRemindTenant('c-1')}
              className="flex-shrink-0 px-3 py-1.5 rounded-lg bg-[#EAF5F3] text-[#2F5D58] border border-[#BCE1DA] text-xs font-bold hover:bg-[#DEF0EC] transition-colors cursor-pointer"
            >
              Nhắc qua Zalo
            </button>
          </div>
        </div>

        {/* Card 3: Hết hạn trong 30 ngày */}
        <div className="bg-surface-container-lowest border border-[#E2EDE9] rounded-2xl p-4 flex flex-col justify-between gap-3 shadow-xs">
          <div className="flex items-start justify-between">
            <div className="flex items-center gap-2.5">
              <div className="w-8 h-8 rounded-lg bg-[#FDF0ED] flex items-center justify-center text-[#D94426]">
                <span className="material-symbols-outlined text-lg">event_busy</span>
              </div>
              <div className="flex items-center gap-2">
                <span className="text-xs text-[#2F5D58] font-bold">Hết hạn trong 30 ngày</span>
                <span className="inline-flex items-center justify-center w-5 h-5 rounded-full bg-[#FDF0ED] text-[#D94426] text-[11px] font-bold">
                  4
                </span>
              </div>
            </div>
          </div>
          <div className="flex items-center justify-between pt-1">
            <span className="text-xs text-[#6C7D79] truncate mr-2">Gần nhất: P.102 còn 15 ngày</span>
            <button
              type="button"
              onClick={() => onRenewContract('c-3')}
              className="flex-shrink-0 px-3 py-1.5 rounded-lg bg-surface-container-lowest text-[#33413F] border border-[#D5E3DF] text-xs font-bold hover:bg-[#F2FAF8] transition-colors cursor-pointer"
            >
              Gia hạn / Thanh lý
            </button>
          </div>
        </div>
      </div>

      {/* Filter Bar */}
      <div className="bg-surface-container-lowest border border-[#E3EFEA] rounded-2xl p-3 flex flex-wrap items-center justify-between gap-3 shadow-xs">
        <div className="flex flex-wrap items-center gap-2">
          {/* Segmented Status Pills */}
          <div className="inline-flex items-center bg-[#F2F7F5] p-1 rounded-xl">
            <button
              type="button"
              onClick={() => setFilterTab('all')}
              className={`px-3 py-1 rounded-lg text-xs font-bold cursor-pointer transition-all ${
                filterTab === 'all' ? 'bg-white text-[#2F5D58] shadow-xs' : 'text-[#6C7D79] hover:text-[#2F5D58]'
              }`}
            >
              Tất cả (36)
            </button>
            <button
              type="button"
              onClick={() => setFilterTab('active')}
              className={`px-3 py-1 rounded-lg text-xs font-bold cursor-pointer transition-all ${
                filterTab === 'active' ? 'bg-white text-[#2F5D58] shadow-xs' : 'text-[#6C7D79] hover:text-[#2F5D58]'
              }`}
            >
              Hiệu lực (32)
            </button>
            <button
              type="button"
              onClick={() => setFilterTab('pending')}
              className={`px-3 py-1 rounded-lg text-xs font-bold cursor-pointer transition-all ${
                filterTab === 'pending' ? 'bg-white text-[#2F5D58] shadow-xs' : 'text-[#6C7D79] hover:text-[#2F5D58]'
              }`}
            >
              Chờ ký (3)
            </button>
            <button
              type="button"
              onClick={() => setFilterTab('draft')}
              className={`px-3 py-1 rounded-lg text-xs font-bold cursor-pointer transition-all ${
                filterTab === 'draft' ? 'bg-white text-[#2F5D58] shadow-xs' : 'text-[#6C7D79] hover:text-[#2F5D58]'
              }`}
            >
              Nháp (1)
            </button>
            <button
              type="button"
              onClick={() => setFilterTab('ended')}
              className={`px-3 py-1 rounded-lg text-xs font-bold cursor-pointer transition-all ${
                filterTab === 'ended' ? 'bg-white text-[#2F5D58] shadow-xs' : 'text-[#6C7D79] hover:text-[#2F5D58]'
              }`}
            >
              Đã kết thúc (15)
            </button>
          </div>

          <div className="h-6 w-px bg-[#D5E3DF] mx-1"></div>

          {/* Warning Filter Chips */}
          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={() => setWarningFilter(warningFilter === 'expiring' ? 'all' : 'expiring')}
              className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold cursor-pointer border transition-colors ${
                warningFilter === 'expiring'
                  ? 'bg-[#FDF0ED] text-[#D94426] border-[#D94426]'
                  : 'bg-[#FDF0ED] text-[#D94426] border-[#F8D5CE] hover:bg-[#FCE8E4]'
              }`}
            >
              <span className="w-1.5 h-1.5 rounded-full bg-[#D94426]"></span>
              <span>Sắp hết hạn</span>
            </button>

            <button
              type="button"
              onClick={() => setWarningFilter(warningFilter === 'deposit' ? 'all' : 'deposit')}
              className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-semibold cursor-pointer border transition-colors ${
                warningFilter === 'deposit'
                  ? 'bg-[#FEF6E7] text-[#B57A08] border-[#B57A08]'
                  : 'bg-[#FEF6E7] text-[#B57A08] border-[#FBE8C4] hover:bg-[#FDF0D9]'
              }`}
            >
              <span className="w-1.5 h-1.5 rounded-full bg-[#B57A08]"></span>
              <span>Chưa đủ cọc</span>
            </button>
          </div>
        </div>

        {/* Search */}
        <div className="relative w-64">
          <span className="material-symbols-outlined absolute left-2.5 top-1/2 -translate-y-1/2 text-[#7A8B87] text-base">
            search
          </span>
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="Tìm số phòng, người thuê..."
            className="w-full pl-8 pr-3 py-1.5 rounded-lg bg-[#F8FAF9] border border-[#E3EFEA] text-xs text-[#33413F] placeholder-[#7A8B87] focus:outline-none focus:border-[#5FB3A8] transition-colors"
          />
        </div>
      </div>

      {/* Contracts Table */}
      <div className="w-full bg-surface-container-lowest border border-[#E3EFEA] rounded-2xl overflow-hidden shadow-xs flex flex-col">
        <div className="overflow-x-auto w-full">
          <table className="w-full text-left border-collapse text-xs">
            <thead>
              <tr className="bg-[#F4F9F7] border-b border-[#E3EFEA]">
                <th className="py-3.5 px-5 font-bold uppercase tracking-wider text-[#2F5D58] text-[11px]">
                  Phòng · Người đại diện
                </th>
                <th className="py-3.5 px-5 font-bold uppercase tracking-wider text-[#2F5D58] text-[11px]">
                  Giá thuê · Tiền cọc
                </th>
                <th className="py-3.5 px-5 font-bold uppercase tracking-wider text-[#2F5D58] text-[11px]">
                  Thời hạn hợp đồng
                </th>
                <th className="py-3.5 px-5 font-bold uppercase tracking-wider text-[#2F5D58] text-[11px]">
                  Tiến độ / Trạng thái
                </th>
                <th className="py-3.5 px-5 font-bold uppercase tracking-wider text-[#2F5D58] text-[11px] text-right">
                  Thao tác
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-[#EDF5F2] font-medium text-[#33413F]">
              {filteredContracts.map((c) => (
                <tr key={c.id} className="hover:bg-[#F9FCFA] transition-colors">
                  <td className="py-3.5 px-5">
                    <div className="flex items-center gap-3">
                      <span className="font-headline text-base font-bold text-[#2F5D58] min-w-[50px]">
                        {c.roomNumber}
                      </span>
                      <div>
                        <span className="font-bold text-xs text-[#33413F] block">
                          {c.tenantName}
                        </span>
                        <span className="text-[11px] font-mono text-[#7A8B87] mt-0.5 block">
                          {c.code}
                        </span>
                      </div>
                    </div>
                  </td>

                  <td className="py-3.5 px-5 whitespace-nowrap">
                    <div className="font-bold text-[#33413F]">{c.monthlyRent.toLocaleString()} đ</div>
                    <div className="text-[11px] text-[#6C7D79]">
                      Cọc {c.deposit.toLocaleString()} đ {c.depositStatus === 'received' ? '(Đã nhận)' : ''}
                    </div>
                  </td>

                  <td className="py-3.5 px-5 whitespace-nowrap">
                    <div className="font-medium text-[#33413F]">
                      {c.startDate} → {c.endDate}
                    </div>
                    {c.remainingDays ? (
                      <div className="text-xs text-[#D94426] font-bold mt-0.5">
                        Còn {c.remainingDays} ngày
                      </div>
                    ) : (
                      <div className="text-[11px] text-[#6C7D79] mt-0.5">
                        {c.remainingMonths ? `Còn ${c.remainingMonths} tháng` : `Kỳ hạn ${c.durationMonths} tháng`}
                      </div>
                    )}
                  </td>

                  <td className="py-3.5 px-5">
                    <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold whitespace-nowrap border ${
                      c.status === 'active'
                        ? 'bg-[#E8F7EE] text-[#1E7E34] border-[#C6EDD3]'
                        : c.status === 'pending_host'
                        ? 'bg-[#E8F7EE] text-[#1E7E34] border-[#C6EDD3]'
                        : c.status === 'pending_tenant'
                        ? 'bg-[#FEF6E7] text-[#B57A08] border-[#FBE8C4]'
                        : c.status === 'expiring'
                        ? 'bg-[#FDF0ED] text-[#D94426] border-[#F8D5CE]'
                        : 'bg-[#EEF2F1] text-[#657471] border-[#D9E3E0]'
                    }`}>
                      <span className={`w-1.5 h-1.5 rounded-full ${
                        c.status === 'active' || c.status === 'pending_host'
                          ? 'bg-[#1E7E34]'
                          : c.status === 'pending_tenant'
                          ? 'bg-[#B57A08]'
                          : c.status === 'expiring'
                          ? 'bg-[#D94426]'
                          : 'bg-[#8E9B97]'
                      }`}></span>
                      {c.statusLabel}
                    </span>
                  </td>

                  <td className="py-3.5 px-5 text-right whitespace-nowrap">
                    <div className="flex items-center justify-end gap-2">
                      {c.status === 'pending_tenant' && (
                        <button
                          type="button"
                          onClick={() => onRemindTenant(c.id)}
                          className="inline-flex items-center gap-1 px-3 py-1.5 rounded-lg bg-[#EAF5F3] text-[#2F5D58] border border-[#BCE1DA] text-xs font-bold hover:bg-[#DEF0EC] transition-colors cursor-pointer"
                        >
                          <span className="material-symbols-outlined text-sm">send</span>
                          <span>Nhắc Zalo</span>
                        </button>
                      )}

                      {c.status === 'pending_host' && (
                        <button
                          type="button"
                          onClick={() => onSignContract(c.id)}
                          className="inline-flex items-center gap-1 px-3 py-1.5 rounded-lg bg-[#5FB3A8] text-white text-xs font-bold hover:bg-[#529E94] transition-colors cursor-pointer"
                        >
                          <span className="material-symbols-outlined text-sm">edit_note</span>
                          <span>Ký ngay</span>
                        </button>
                      )}

                      {c.status === 'expiring' && (
                        <button
                          type="button"
                          onClick={() => onRenewContract(c.id)}
                          className="inline-flex items-center gap-1 px-3 py-1.5 rounded-lg bg-surface-container-lowest text-[#33413F] border border-[#D5E3DF] text-xs font-bold hover:bg-[#F2FAF8] transition-colors cursor-pointer"
                        >
                          <span className="material-symbols-outlined text-sm">autorenew</span>
                          <span>Gia hạn</span>
                        </button>
                      )}

                      <button
                        type="button"
                        onClick={() => onOpenContractDetail(c.id)}
                        className="inline-flex items-center gap-1 px-3 py-1.5 rounded-lg bg-surface-container-lowest border border-[#D1E5E0] text-[#2F5D58] text-xs font-bold hover:bg-[#F2FAF8] transition-colors cursor-pointer"
                      >
                        <span>Xem chi tiết</span>
                        <span className="material-symbols-outlined text-sm">chevron_right</span>
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Pagination Footer */}
        <div className="py-3.5 px-5 bg-[#F8FAF9] border-t border-[#E3EFEA] flex items-center justify-between text-xs text-[#6C7D79]">
          <span className="font-medium">Hiển thị {filteredContracts.length} / 36 hợp đồng</span>
          <div className="flex items-center gap-2">
            <span className="font-bold text-[#2F5D58] px-2.5 py-0.5 rounded bg-[#EBF7F4]">Trang 1 / 1</span>
          </div>
        </div>
      </div>
    </div>
  );
};
