import { z } from 'zod'
import { request, write } from './http'
import { pageSchema } from '@/interfaces/domain'
import { useSession } from '@/features/auth/session-store'
import type { ManagerRecord } from '@/interfaces/manager'
import { reportOptions } from './tenant-maintenance'

// BE-06.4 Owner/Manager side of /api/maintenance-requests (P1-114..118).
type Report = ManagerRecord<'maintenance'>
const status = z.enum(['OPEN', 'ACCEPTED', 'IN_PROGRESS', 'COMPLETED', 'REJECTED', 'CANCELLED'])
const priority = z.enum(['LOW', 'NORMAL', 'HIGH', 'URGENT'])
const approval = z.enum(['PENDING', 'APPROVED', 'REJECTED']).nullable()
const summarySchema = z.object({
  id: z.uuid(),
  roomId: z.uuid(),
  title: z.string(),
  priority,
  status,
  assignedTo: z.uuid().nullable(),
  createdAt: z.string(),
})
const recordSchema = z.object({
  id: z.uuid(),
  recordType: z.enum(['WORK', 'ASSIGNMENT', 'STATUS', 'COST', 'LIABILITY']),
  parentCostRecordId: z.uuid().nullable(),
  responsibleParty: z.enum(['OWNER', 'TENANT']).nullable(),
  amount: z.number().nullable(),
  description: z.string().nullable(),
  performedBy: z.uuid().nullable(),
  statusAfter: z.string().nullable(),
  approvalStatus: approval,
  occurredAt: z.string(),
})
const detailSchema = summarySchema.extend({
  contractTenantId: z.uuid().nullable(),
  description: z.string(),
  furnitureItems: z.array(z.object({ furnitureId: z.uuid() })),
  serviceItems: z.array(z.object({ roomServiceId: z.uuid() })),
  records: z.array(recordSchema),
  rating: z.number().nullable(),
  ratingComment: z.string().nullable(),
})
type MaintenanceRecord = z.infer<typeof recordSchema>
const base = (id: string) => `/api/maintenance-requests/${encodeURIComponent(id)}`
const nullable = z.unknown().nullable()
const party = (p: string) => (p === 'TENANT' ? 'Người thuê' : 'Chủ nhà')
// BE returns user ids only; there is no user lookup in Student 6 scope.
const who = (id: string | null) =>
  !id ? '' : id === useSession.getState().user?.id ? 'Bạn' : 'Người quản lý'

const fromSummary = (r: z.infer<typeof summarySchema>): Report => ({
  id: r.id,
  roomId: r.roomId,
  // Summary carries no room label, requester or item.
  roomName: '',
  tenantName: '',
  contractTenantId: '',
  assetId: '',
  category: 'FURNITURE',
  assetName: '',
  title: r.title,
  description: '',
  priority: r.priority,
  status: r.status,
  createdAt: r.createdAt,
  assignedTo: r.assignedTo ? who(r.assignedTo) || 'Đã phân công' : null,
  attachments: [],
  records: [],
})
/** COST records carry the liabilities (P1-117) that split them; LIABILITY rows are folded in. */
const toRecords = (records: MaintenanceRecord[]): Report['records'] =>
  records
    .filter((r) => r.recordType !== 'LIABILITY')
    .map((r) => {
      const shares = records.filter(
        (l) =>
          l.recordType === 'LIABILITY' &&
          l.parentCostRecordId === r.id &&
          l.approvalStatus !== 'REJECTED',
      )
      const parties = [...new Set(shares.map((l) => l.responsibleParty!))]
      const split =
        r.recordType !== 'COST'
          ? ''
          : shares.length === 0
            ? '\nChưa phân bổ trách nhiệm chi phí.'
            : parties.length > 1
              ? `\n${shares.map((l) => `${party(l.responsibleParty!)}: ${l.amount}`).join(' · ')}`
              : ''
      return {
        id: r.id,
        at: r.occurredAt,
        status: r.statusAfter || r.recordType,
        description: `${r.description || ''}${split}`,
        performedBy: who(r.performedBy),
        evidence: [],
        cost: r.recordType === 'COST' ? (r.amount ?? 0) : 0,
        responsibleParty: parties.length === 1 ? parties[0] : null,
        approvalStatus: r.recordType === 'COST' ? r.approvalStatus : null,
      }
    })

/** GET /api/maintenance-requests requires roomId (P1-114). */
export async function realMaintenanceList(filters: Record<string, string>) {
  const size = Number(filters.size || 8)
  if (!filters.roomId) return { content: [], number: 1, size, totalElements: 0, totalPages: 0 }
  const page = await request(
    'managerMaintenance',
    `/api/maintenance-requests?${new URLSearchParams({
      roomId: filters.roomId,
      ...(filters.status ? { status: filters.status } : {}),
      page: String(Number(filters.page || 0) + 1),
      size: String(size),
    })}`,
    pageSchema(summarySchema),
  )
  return { ...page, content: page.content.map(fromSummary) }
}
export async function realMaintenance(id: string) {
  const detail = await request('managerMaintenance', base(id), detailSchema)
  const options = await reportOptions(detail.roomId, 'managerMaintenance').catch(() => [])
  const ids = [
    ...detail.furnitureItems.map((i) => i.furnitureId),
    ...detail.serviceItems.map((i) => i.roomServiceId),
  ]
  return {
    ...fromSummary(detail),
    contractTenantId: detail.contractTenantId || '',
    assetId: ids[0] || '',
    category: detail.furnitureItems.length ? 'FURNITURE' : 'SERVICE',
    assetName: ids.map((i) => options.find((o) => o.id === i)?.name || 'Mục đã báo').join(', '),
    description: detail.description,
    records: toRecords(detail.records),
    rating: detail.rating,
    ratingComment: detail.ratingComment,
  } satisfies Report & { rating: number | null; ratingComment: string | null }
}
const post = (path: string, body?: unknown) =>
  request('managerMaintenance', path, nullable, write('POST', body))

/** Maps the existing manager actions onto the BE transitions and record endpoints. */
export async function realMaintenanceAction(
  id: string,
  action: string,
  value: Record<string, unknown>,
) {
  const note = (value.reason as string | undefined) || null
  switch (action) {
    case 'accept':
    case 'reject':
    case 'start':
    case 'complete':
      return post(`${base(id)}/${action}`, { note })
    case 'assign':
      return post(`${base(id)}/assign`, { assigneeUserId: value.handler })
    case 'record': {
      const cost = Number(value.cost || 0)
      const description = String(value.description)
      if (cost <= 0)
        return post(`${base(id)}/records`, { recordType: 'WORK', description, evidence: [] })
      const record = await request(
        'managerMaintenance',
        `${base(id)}/records`,
        z.object({ id: z.uuid() }),
        write('POST', { recordType: 'COST', description, amount: cost, evidence: [] }),
      )
      if (value.responsibleParty)
        await post(`${base(id)}/records/${record.id}/liabilities`, {
          responsibleParty: value.responsibleParty,
          amount: cost,
          description,
        })
      return record
    }
    case 'cost-approve': {
      // P1-118: liabilities first; the cost is approved only when they add up to it.
      const recordId = String(value.recordId)
      const detail = await request('managerMaintenance', base(id), detailSchema)
      for (const l of detail.records)
        if (
          l.recordType === 'LIABILITY' &&
          l.parentCostRecordId === recordId &&
          l.approvalStatus === 'PENDING'
        )
          await post(`${base(id)}/records/${l.id}/approve`)
      return post(`${base(id)}/records/${recordId}/approve`)
    }
    case 'cost-reject':
      return post(`${base(id)}/records/${String(value.recordId)}/reject`)
    default:
      throw new Error('Thao tác này chưa được hỗ trợ trên hệ thống thật.')
  }
}

/** P2-32: approved repair liabilities per room and party, for the whole Branch. */
export const costStats = (branchId: string) =>
  request(
    'managerMaintenance',
    `/api/maintenance-requests/cost-stats?${new URLSearchParams({ branchId })}`,
    z.array(
      z.object({
        roomId: z.uuid(),
        responsibleParty: z.enum(['OWNER', 'TENANT']),
        totalAmount: z.number(),
      }),
    ),
  )
