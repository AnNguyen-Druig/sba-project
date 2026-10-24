import { z } from 'zod'
import { request, write } from './http'
import type { Feature } from './config'
import { pageSchema } from '@/interfaces/domain'
import { reportSchema, type MaintenanceReport, type ReportInput } from '@/interfaces/tenant'

// BE-06.4 /api/maintenance-requests (P1-111..119). Tenants never receive COST/LIABILITY records.
const status = reportSchema.shape.status
const priority = reportSchema.shape.priority
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
  recordType: z.string(),
  description: z.string().nullable(),
  statusAfter: z.string().nullable(),
  amount: z.number().nullable(),
  responsibleParty: z.enum(['OWNER', 'TENANT']).nullable(),
  approvalStatus: z.enum(['PENDING', 'APPROVED', 'REJECTED']).nullable(),
  occurredAt: z.string(),
})
const detailSchema = summarySchema.extend({
  contractTenantId: z.uuid(),
  description: z.string(),
  furnitureItems: z.array(z.object({ furnitureId: z.uuid() })),
  serviceItems: z.array(z.object({ roomServiceId: z.uuid() })),
  records: z.array(recordSchema),
  rating: z.number().nullable(),
  ratingComment: z.string().nullable(),
})
const furniturePageSchema = pageSchema(
  z.object({
    id: z.uuid(),
    name: z.string(),
    condition: z.string().nullable(),
    status: z.string(),
  }),
)
const roomServicesSchema = z.array(
  z.object({ id: z.uuid(), serviceName: z.string().nullable(), status: z.string().nullable() }),
)
export type ReportOption = {
  id: string
  name: string
  category: 'FURNITURE' | 'SERVICE'
  condition: string
}

/** Reportable items of the rented room: its furniture (P1-111) and active room services. */
export async function reportOptions(
  roomId: string,
  feature: Feature = 'tenantMaintenance',
): Promise<ReportOption[]> {
  const [furniture, services] = await Promise.all([
    request(
      feature,
      `/api/furnitures?${new URLSearchParams({ roomId, page: '1', size: '100' })}`,
      furniturePageSchema,
    ),
    request(feature, `/api/rooms/${encodeURIComponent(roomId)}/services`, roomServicesSchema),
  ])
  return [
    ...furniture.content
      .filter((f) => f.status !== 'RETIRED')
      .map((f) => ({
        id: f.id,
        name: f.name,
        category: 'FURNITURE' as const,
        condition: f.condition || '',
      })),
    ...services
      .filter((s) => s.status !== 'INACTIVE')
      .map((s) => ({
        id: s.id,
        name: s.serviceName || 'Dịch vụ',
        category: 'SERVICE' as const,
        condition: '',
      })),
  ]
}

const base = {
  // Evidence is a Cloudinary reference on the BE; the FE has no upload integration yet.
  attachments: [],
  // The assignee is a user id only; show that someone is assigned without exposing the id.
  assigned: (id: string | null) => (id ? 'Đã phân công' : null),
}
const fromSummary = (r: z.infer<typeof summarySchema>): MaintenanceReport => ({
  id: r.id,
  contractTenantId: '',
  // The summary carries no item; the list shows title/date only.
  assetId: '',
  category: 'FURNITURE',
  assetName: '',
  title: r.title,
  description: '',
  priority: r.priority,
  status: r.status,
  createdAt: r.createdAt,
  assignedTo: base.assigned(r.assignedTo),
  attachments: base.attachments,
  records: [],
})
export type RatedReport = MaintenanceReport & {
  rating: number | null
  ratingComment: string | null
}
const fromDetail = (r: z.infer<typeof detailSchema>, options: ReportOption[]): RatedReport => {
  const ids = [
    ...r.furnitureItems.map((i) => i.furnitureId),
    ...r.serviceItems.map((i) => i.roomServiceId),
  ]
  return {
    ...fromSummary(r),
    contractTenantId: r.contractTenantId,
    assetId: ids[0] || '',
    category: r.furnitureItems.length ? 'FURNITURE' : 'SERVICE',
    assetName: ids.map((id) => options.find((o) => o.id === id)?.name || 'Mục đã báo').join(', '),
    description: r.description,
    rating: r.rating,
    ratingComment: r.ratingComment,
    records: r.records.map((x) => ({
      id: x.id,
      at: x.occurredAt,
      status: x.statusAfter || x.recordType,
      description: x.description || '',
      performedBy: '',
      evidence: [],
      cost: x.amount ?? 0,
      responsibleParty: x.responsibleParty,
      approvalStatus: x.approvalStatus,
    })),
  }
}

/** P1-119: the caller's own requests, narrowed to the selected tenancy's room. */
export async function myReports(roomId: string) {
  const page = await request(
    'tenantMaintenance',
    '/api/maintenance-requests/my?page=1&size=100',
    pageSchema(summarySchema),
  )
  return page.content.filter((r) => r.roomId === roomId).map(fromSummary)
}
export async function myReport(roomId: string, reportId: string) {
  const [detail, options] = await Promise.all([
    request(
      'tenantMaintenance',
      `/api/maintenance-requests/${encodeURIComponent(reportId)}`,
      detailSchema,
    ),
    reportOptions(roomId),
  ])
  return fromDetail(detail, options)
}
/** P1-111/112: one furniture OR service item per request, as the existing form collects it. */
export async function createReport(roomId: string, value: ReportInput) {
  const detail = await request(
    'tenantMaintenance',
    '/api/maintenance-requests',
    detailSchema,
    write('POST', {
      roomId,
      title: value.title,
      description: value.description,
      priority: value.priority,
      furnitureItems: value.category === 'FURNITURE' ? [{ furnitureId: value.assetId }] : [],
      serviceItems: value.category === 'SERVICE' ? [{ roomServiceId: value.assetId }] : [],
      evidence: [],
    }),
  )
  return fromDetail(detail, [])
}

/** P2-31: the requester rates a completed request once. */
export async function rateReport(
  roomId: string,
  reportId: string,
  rating: number,
  comment: string,
) {
  const detail = await request(
    'tenantMaintenance',
    `/api/maintenance-requests/${encodeURIComponent(reportId)}/rating`,
    detailSchema,
    write('POST', { rating, comment: comment || null }),
  )
  return fromDetail(detail, await reportOptions(roomId))
}
