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

// ================= STUDENT 4 DOMAIN TYPES =================
export interface ServiceItem {
  id: string;
  code: string;
  name: string;
  unit: string;
  serviceType: 'ELECTRICITY' | 'WATER' | 'INTERNET' | 'PARKING' | 'CLEANING' | 'SECURITY' | 'LAUNDRY' | 'OTHER';
  billingMethod: 'METERED' | 'PER_PERSON' | 'PER_ROOM' | 'FIXED' | 'PER_USE';
  referencePrice: number;
  mandatory: boolean;
  active: boolean;
  description?: string;
  icon?: string;
}

export interface MeterReadingRecord {
  id: string;
  roomId: string;
  roomNumber: string;
  floor: number;
  tenantName: string;
  billingMonth: string;
  previousElectricity: number;
  currentElectricity: number;
  electricityUsage: number;
  electricityRate: number;
  electricityTotal: number;
  previousWater: number;
  currentWater: number;
  waterUsage: number;
  waterRate: number;
  waterTotal: number;
  fixedServicesTotal: number;
  totalAmount: number;
  status: 'draft' | 'recorded' | 'finalized' | 'abnormal';
  statusLabel: string;
  isAbnormal: boolean;
  abnormalReason?: string;
  meterPhotoUrl?: string;
  waterPhotoUrl?: string;
  readingDate: string;
  finalizedAt?: string;
}

export interface CostProposalMember {
  userId: string;
  name: string;
  avatar: string;
  residentDays: number;
  percentage: number;
  allocatedAmount: number;
  note?: string;
}

export interface CostProposal {
  id: string;
  chargeId: string;
  roomId: string;
  roomNumber: string;
  totalAmount: number;
  proposedBy: string;
  proposedByAvatar: string;
  method: 'RESIDENT_DAYS' | 'APPROVED_CUSTOM';
  createdAt: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  statusLabel: string;
  reason: string;
  members: CostProposalMember[];
}

export interface AppNotification {
  id: string;
  recipientUserId: string;
  recipientName: string;
  type: string;
  title: string;
  message: string;
  referenceType?: string;
  referenceId?: string;
  read: boolean;
  readAt?: string;
  createdAt: string;
  channel: 'IN_APP' | 'EMAIL' | 'SMS';
  deliveryStatus: 'SENT' | 'PENDING' | 'FAILED';
  category: 'utilities' | 'financial' | 'contract' | 'maintenance' | 'system';
}

