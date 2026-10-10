export type ViewMode = 
  | 'overview' 
  | 'rooms' 
  | 'room-detail' 
  | 'billing' 
  | 'threads-manager' 
  | 'contracts' 
  | 'create-contract' 
  | 'maintenance' 
  | 'utilities'
  | 'settings'
  | 'public-search'
  | 'public-threads';

export type RoomStatus = 'rented' | 'available' | 'pending_contract' | 'maintenance' | 'overdue';

export interface Room {
  id: string;
  roomNumber: string;
  floor: number;
  floorName: string;
  area: number;
  type: string;
  price: number;
  status: RoomStatus;
  statusLabel: string;
  currentTenant?: string;
  tenantPhone?: string;
  tenantAvatar?: string;
  contractEnd?: string;
  contractExpiryDays?: number;
  occupants?: string;
  capacity?: string;
  debt?: number;
  maintenanceNote?: string;
  note?: string;
  features?: string[];
  images?: string[];
  depositAmount?: number;
}

export interface Resident {
  id: string;
  name: string;
  role: 'primary' | 'roommate';
  roleLabel: string;
  occupation: string;
  workplace: string;
  phone: string;
  hometown: string;
  cccd: string;
  cccdStatus: string;
  vehicle: string;
  vehicleSlot: string;
  policeApproval: string;
  avatar: string;
}

export interface InventoryItem {
  id: string;
  code: string;
  name: string;
  model: string;
  quantity: string;
  condition: string;
  lastMaintenance: string;
  roomNumber: string;
  status: 'good' | 'damaged' | 'in_repair';
}

export interface Invoice {
  id: string;
  roomNumber: string;
  tenantName: string;
  month: string;
  total: number;
  paid: number;
  remaining: number;
  status: 'paid' | 'pending_verification' | 'overdue' | 'partial' | 'viewed';
  statusLabel: string;
  dueDate: string;
  overdueDays?: number;
  electricityKwh: number;
  electricityCost: number;
  waterM3: number;
  waterCost: number;
  serviceCost: number;
  baseRent: number;
}

export interface Contract {
  id: string;
  code: string;
  roomNumber: string;
  tenantName: string;
  monthlyRent: number;
  deposit: number;
  depositStatus: 'received' | 'partial' | 'returned';
  startDate: string;
  endDate: string;
  durationMonths: number;
  remainingMonths?: number;
  remainingDays?: number;
  status: 'active' | 'pending_tenant' | 'pending_host' | 'expiring' | 'draft' | 'ended';
  statusLabel: string;
  daysPending?: number;
  signDate?: string;
}

export interface SeekingThread {
  id: string;
  authorName: string;
  authorRole: string;
  authorAvatar: string;
  isVerified: boolean;
  timeAgo: string;
  title: string;
  content: string;
  location: string;
  maxBudget: number;
  minArea: number;
  moveInDate: string;
  occupants: string;
  amenities: string[];
  status: 'open' | 'matched_100' | 'proposed' | 'closed';
  matchPercent: number;
  matchedRoom?: {
    roomNumber: string;
    area: number;
    price: number;
    floor: number;
    facilityName: string;
    image: string;
  };
  proposalsCount: number;
  viewsCount: number;
  commentsCount: number;
  hasHostProposal?: boolean;
}

export interface MaintenanceTicket {
  id: string;
  ticketCode: string;
  roomNumber: string;
  floor: number;
  title: string;
  description: string;
  urgency: 'high' | 'normal' | 'low';
  status: 'new' | 'in_progress' | 'completed';
  statusLabel: string;
  createdAt: string;
  tenantName: string;
  tenantPhone: string;
  claimedBy?: string;
  scheduledTime?: string;
  actualCost?: number;
  resolvedAt?: string;
}
