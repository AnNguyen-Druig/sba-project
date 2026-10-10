import React, { useState } from 'react';
import { ViewMode, Room, Contract, SeekingThread, MaintenanceTicket, Invoice } from './types';
import {
  initialRooms,
  initialContracts,
  initialThreads,
  initialMaintenanceTickets,
  initialInvoices
} from './data/mockData';

// Layout
import { Sidebar } from './components/layout/Sidebar';
import { Header } from './components/layout/Header';
import { PublicHeader } from './components/layout/PublicHeader';
import { PublicFooter } from './components/layout/PublicFooter';

// Manager Views
import { OverviewView } from './components/manager/OverviewView';
import { RoomsFloorView } from './components/manager/RoomsFloorView';
import { RoomDetailView } from './components/manager/RoomDetailView';
import { BillingView } from './components/manager/BillingView';
import { ThreadMatcherManagerView } from './components/manager/ThreadMatcherManagerView';
import { ContractsListView } from './components/manager/ContractsListView';
import { CreateContractView } from './components/manager/CreateContractView';
import { MaintenanceView } from './components/manager/MaintenanceView';
import { UtilitiesView } from './components/manager/UtilitiesView';
import { SettingsView } from './components/manager/SettingsView';

// Public Views
import { PublicRentSearchView } from './components/public/PublicRentSearchView';
import { PublicThreadCommunityView } from './components/public/PublicThreadCommunityView';

// Modals
import { CreateThreadModal } from './components/modals/CreateThreadModal';
import { AddRoomModal } from './components/modals/AddRoomModal';

export default function App() {
  // Navigation State
  const [currentView, setCurrentView] = useState<ViewMode>('overview');
  const [isPublicMode, setIsPublicMode] = useState<boolean>(false);
  const [selectedRoomId, setSelectedRoomId] = useState<string>('r-204');
  const [globalSearch, setGlobalSearch] = useState<string>('');

  // Domain State
  const [rooms, setRooms] = useState<Room[]>(initialRooms);
  const [contracts, setContracts] = useState<Contract[]>(initialContracts);
  const [threads, setThreads] = useState<SeekingThread[]>(initialThreads);
  const [tickets, setTickets] = useState<MaintenanceTicket[]>(initialMaintenanceTickets);
  const [invoices, setInvoices] = useState<Invoice[]>(initialInvoices);

  // Modals
  const [isCreateThreadModalOpen, setIsCreateThreadModalOpen] = useState(false);
  const [isAddRoomModalOpen, setIsAddRoomModalOpen] = useState(false);
  const [toastMessage, setToastMessage] = useState<string | null>(null);

  const showToast = (msg: string) => {
    setToastMessage(msg);
    setTimeout(() => setToastMessage(null), 4000);
  };

  // Switchers
  const handleSwitchToPublic = () => {
    setIsPublicMode(true);
    setCurrentView('public-search');
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleSwitchToManager = () => {
    setIsPublicMode(false);
    setCurrentView('overview');
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleSelectRoom = (roomId: string) => {
    setSelectedRoomId(roomId);
    setCurrentView('room-detail');
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  // Actions
  const handleAddRoom = (newRoomData: Partial<Room>) => {
    const newRoom: Room = {
      id: `r-${Date.now()}`,
      roomNumber: newRoomData.roomNumber || 'P.210',
      floor: newRoomData.floor || 2,
      floorName: newRoomData.floorName || 'Tầng 2',
      area: newRoomData.area || 28,
      type: newRoomData.type || 'Studio ban công',
      price: newRoomData.price || 4800000,
      status: newRoomData.status || 'available',
      statusLabel: newRoomData.statusLabel || 'Trống sẵn sàng',
      capacity: newRoomData.capacity || '2-3 người'
    };
    setRooms([newRoom, ...rooms]);
    showToast(`Đã thêm phòng ${newRoom.roomNumber} thành công!`);
  };

  const handleCreateNewThread = (newThreadData: Partial<SeekingThread>) => {
    const thread: SeekingThread = {
      id: `thr-${Date.now()}`,
      authorName: newThreadData.authorName || 'Người tìm trọ',
      authorRole: 'Khách tiềm năng',
      authorAvatar: 'https://lh3.googleusercontent.com/aida-public/AB6AXuCpUIjYtTTVWYpQcwlUaF1Es5Nn60C1fvlE8PKJUqYTy1oxB0vgCwDJyJjz2pj2__2teT5CGxfbJ7Vx4mrSqWIJAHMnY5XFkjjLRHDALIxJUzcIscJLA765nNgL_BwbG00DvN1pLWGQ_qdgudu-pi_RmQy49Gku7tlFWe2-lO0xYB7iJZ66cTzH9aghhnWnu_pU2Dl-5ckvyD9FAYXJjuH6-CqHKJf_IuBReBWjESaa-fDonG8FlJYx',
      isVerified: true,
      timeAgo: 'Vừa xong',
      title: newThreadData.title || 'Tìm phòng trọ',
      content: newThreadData.content || '',
      location: newThreadData.location || 'Bình Thạnh',
      maxBudget: newThreadData.maxBudget || 5000000,
      minArea: newThreadData.minArea || 20,
      moveInDate: newThreadData.moveInDate || 'Đầu tháng sau',
      occupants: newThreadData.occupants || '1 người',
      amenities: newThreadData.amenities || ['Máy lạnh'],
      status: 'matched_100',
      matchPercent: 100,
      proposalsCount: 0,
      viewsCount: 1,
      commentsCount: 0
    };
    setThreads([thread, ...threads]);
    showToast('Thread tìm trọ của bạn đã đăng thành công! Hệ thống đang quét phòng trống khớp 100%.');
  };

  const handleSendProposal = (threadId: string, roomCode: string, msg: string) => {
    setThreads(
      threads.map((t) =>
        t.id === threadId ? { ...t, proposalsCount: t.proposalsCount + 1, hasHostProposal: true } : t
      )
    );
    showToast(`Đã gửi đề xuất Phòng ${roomCode} vào thread thành công theo quy tắc BR-07!`);
  };

  const handleConfirmInvoicePayment = (invoiceId: string) => {
    setInvoices(
      invoices.map((inv) =>
        inv.id === invoiceId
          ? { ...inv, paid: inv.total, remaining: 0, status: 'paid', statusLabel: 'Đã thu' }
          : inv
      )
    );
    showToast('Đã xác nhận thanh toán hóa đơn VietQR thành công!');
  };

  const handleSendInvoiceReminder = (invoiceId: string) => {
    showToast('Đã gửi thông báo nhắc nợ qua Zalo OA đến khách thuê!');
  };

  const handleConfirmAllPendingInvoices = () => {
    setInvoices(
      invoices.map((inv) =>
        inv.status === 'pending_verification'
          ? { ...inv, paid: inv.total, remaining: 0, status: 'paid', statusLabel: 'Đã thu' }
          : inv
      )
    );
    showToast('Đã xác nhận thanh toán cho toàn bộ 3 phòng chờ đối soát!');
  };

  const handleRemindAllOverdue = () => {
    showToast('Đã gửi thông báo nhắc tự động qua Zalo đến 4 phòng quá hạn!');
  };

  const handleSignContract = (contractId: string) => {
    setContracts(
      contracts.map((c) =>
        c.id === contractId ? { ...c, status: 'active', statusLabel: 'Hiệu lực' } : c
      )
    );
    showToast('Bạn đã ký số thành công hợp đồng bằng chữ ký điện tử!');
  };

  const handleRemindTenantContract = (contractId: string) => {
    showToast('Đã gửi tin nhắn Zalo kèm liên kết ký hợp đồng bằng mã OTP tới khách thuê!');
  };

  const handleRenewContract = (contractId: string) => {
    setContracts(
      contracts.map((c) =>
        c.id === contractId
          ? { ...c, status: 'active', statusLabel: 'Hiệu lực', remainingMonths: 12, remainingDays: undefined }
          : c
      )
    );
    showToast('Đã gia hạn hợp đồng thêm 12 tháng thành công!');
  };

  const handleCreateContractSuccess = (contractData: any) => {
    const newContract: Contract = {
      id: `c-${Date.now()}`,
      code: `HĐ-DBL-${contractData.roomNumber.replace('P.', '')}-2024`,
      roomNumber: contractData.roomNumber,
      tenantName: contractData.tenantName,
      monthlyRent: contractData.monthlyRent,
      deposit: contractData.deposit,
      depositStatus: 'received',
      startDate: contractData.startDate,
      endDate: contractData.endDate,
      durationMonths: contractData.durationMonths,
      remainingMonths: contractData.durationMonths,
      status: 'active',
      statusLabel: 'Hiệu lực'
    };
    setContracts([newContract, ...contracts]);
    setCurrentView('contracts');
    showToast(`Tạo thành công hợp đồng điện tử ${newContract.code}!`);
  };

  const handleClaimTicket = (ticketId: string) => {
    setTickets(
      tickets.map((tk) =>
        tk.id === ticketId
          ? {
              ...tk,
              status: 'in_progress',
              statusLabel: 'Đang xử lý',
              claimedBy: 'Nguyễn Văn Nam (Chủ trọ)'
            }
          : tk
      )
    );
    showToast('Đã tiếp nhận (Claim) sự cố thành công theo quy tắc BR-11!');
  };

  const selectedRoom = rooms.find((r) => r.id === selectedRoomId) || rooms[0];

  return (
    <div className="min-h-screen bg-background font-sans text-on-surface flex flex-col">
      {/* Toast Notification Banner */}
      {toastMessage && (
        <div className="fixed top-20 right-6 z-50 bg-primary text-white px-5 py-3 rounded-2xl shadow-xl text-xs font-bold flex items-center gap-2 animate-in fade-in slide-in-from-top-4">
          <span className="material-symbols-outlined text-base">check_circle</span>
          <span>{toastMessage}</span>
        </div>
      )}

      {/* RENDER PUBLIC PORTAL OR MANAGER DASHBOARD */}
      {isPublicMode ? (
        <div className="flex flex-col min-h-screen">
          <PublicHeader
            currentView={currentView}
            onNavigate={(view) => {
              setCurrentView(view);
              window.scrollTo({ top: 0, behavior: 'smooth' });
            }}
            onOpenCreateThread={() => setIsCreateThreadModalOpen(true)}
            onSwitchToManager={handleSwitchToManager}
          />

          <main className="w-full pt-20 flex-1">
            {currentView === 'public-threads' ? (
              <PublicThreadCommunityView
                threads={threads}
                onOpenCreateThread={() => setIsCreateThreadModalOpen(true)}
                onSwitchToManager={handleSwitchToManager}
              />
            ) : (
              <PublicRentSearchView
                rooms={rooms}
                onOpenCreateThread={() => setIsCreateThreadModalOpen(true)}
                onNavigateToThreads={() => {
                  setCurrentView('public-threads');
                  window.scrollTo({ top: 0, behavior: 'smooth' });
                }}
              />
            )}
          </main>

          <PublicFooter />
        </div>
      ) : (
        /* MANAGER DASHBOARD */
        <div className="flex min-h-screen">
          <Sidebar
            currentView={currentView}
            onNavigate={(view) => {
              setCurrentView(view);
              window.scrollTo({ top: 0, behavior: 'smooth' });
            }}
            onSwitchToPublic={handleSwitchToPublic}
          />

          <div className="pl-72 flex flex-col flex-1 min-h-screen">
            <Header
              onSwitchToPublic={handleSwitchToPublic}
              searchQuery={globalSearch}
              onSearchChange={setGlobalSearch}
            />

            <main className="w-full pt-16 flex-1 bg-background">
              {currentView === 'overview' && (
                <OverviewView
                  rooms={rooms}
                  onSelectRoom={handleSelectRoom}
                  onNavigate={(v) => setCurrentView(v)}
                  onOpenAddRoom={() => setIsAddRoomModalOpen(true)}
                />
              )}

              {currentView === 'rooms' && (
                <RoomsFloorView
                  rooms={rooms}
                  selectedRoomId={selectedRoomId}
                  onSelectRoom={setSelectedRoomId}
                  onOpenRoomDetail={handleSelectRoom}
                  onOpenAddRoom={() => setIsAddRoomModalOpen(true)}
                />
              )}

              {currentView === 'room-detail' && (
                <RoomDetailView
                  room={selectedRoom}
                  onBackToRooms={() => setCurrentView('rooms')}
                  onNavigate={(v) => setCurrentView(v)}
                  onOpenMaintenance={(rNum) => {
                    setCurrentView('maintenance');
                    showToast(`Mở danh sách bảo trì cho ${rNum}`);
                  }}
                />
              )}

              {currentView === 'billing' && (
                <BillingView
                  invoices={invoices}
                  onConfirmInvoicePayment={handleConfirmInvoicePayment}
                  onSendInvoiceReminder={handleSendInvoiceReminder}
                  onConfirmAllPending={handleConfirmAllPendingInvoices}
                  onRemindAllOverdue={handleRemindAllOverdue}
                />
              )}

              {currentView === 'threads-manager' && (
                <ThreadMatcherManagerView
                  threads={threads}
                  onSendProposal={handleSendProposal}
                />
              )}

              {currentView === 'contracts' && (
                <ContractsListView
                  contracts={contracts}
                  onNavigate={(v) => setCurrentView(v)}
                  onOpenContractDetail={(id) => {
                    handleSelectRoom('r-204');
                  }}
                  onSignContract={handleSignContract}
                  onRemindTenant={handleRemindTenantContract}
                  onRenewContract={handleRenewContract}
                />
              )}

              {currentView === 'create-contract' && (
                <CreateContractView
                  onBack={() => setCurrentView('contracts')}
                  onNavigate={(v) => setCurrentView(v)}
                  onCreateSuccess={handleCreateContractSuccess}
                />
              )}

              {currentView === 'maintenance' && (
                <MaintenanceView
                  tickets={tickets}
                  onClaimTicket={handleClaimTicket}
                  onCreateTicket={() => showToast('Mở biểu mẫu tạo phiếu sự cố CSVC mới')}
                />
              )}

              {currentView === 'utilities' && (
                <UtilitiesView />
              )}

              {currentView === 'settings' && (
                <SettingsView />
              )}
            </main>
          </div>
        </div>
      )}

      {/* Global Modals */}
      <CreateThreadModal
        isOpen={isCreateThreadModalOpen}
        onClose={() => setIsCreateThreadModalOpen(false)}
        onSubmit={handleCreateNewThread}
      />

      <AddRoomModal
        isOpen={isAddRoomModalOpen}
        onClose={() => setIsAddRoomModalOpen(false)}
        onAddRoom={handleAddRoom}
      />
    </div>
  );
}
