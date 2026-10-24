import { z } from 'zod'
import { request, write } from './http'
import { pageSchema } from '@/interfaces/domain'
import { furnitureStatusSchema } from '@/interfaces/manager'

// BE FurnitureResponse / FurnitureWarrantyResponse (BE-06.3, P1-107/108).
const furnitureSchema = z.object({
  id: z.uuid(),
  roomId: z.uuid(),
  assetCode: z.string(),
  name: z.string(),
  furnitureType: z.string().nullable(),
  serialNumber: z.string().nullable(),
  purchaseDate: z.iso.date().nullable(),
  assetValue: z.number().nullable(),
  condition: z.string().nullable(),
  status: furnitureStatusSchema,
})
const warrantySchema = z.object({
  id: z.uuid(),
  furnitureId: z.uuid(),
  provider: z.string(),
  startDate: z.iso.date(),
  endDate: z.iso.date(),
  terms: z.string().nullable(),
  warrantyCode: z.string().nullable(),
})
const toWarranty = (w: z.infer<typeof warrantySchema>) => ({
  id: w.id,
  provider: w.provider,
  reference: w.warrantyCode || '',
  startDate: w.startDate,
  endDate: w.endDate,
  terms: w.terms || '',
})
// Same shape as the mock asset; BE has no slot or branch on furniture and keeps warranties separate.
const toAsset = (
  branchId: string,
  f: z.infer<typeof furnitureSchema>,
  warranties: z.infer<typeof warrantySchema>[] = [],
) => ({
  id: f.id,
  branchId,
  roomId: f.roomId,
  roomSlotId: null,
  code: f.assetCode,
  name: f.name,
  category: f.furnitureType || '',
  serial: f.serialNumber || '',
  purchaseDate: f.purchaseDate || '',
  value: f.assetValue ?? 0,
  condition: f.condition || '',
  operationalStatus: f.status,
  warranties: warranties.map(toWarranty),
})
const toBody = (v: Record<string, unknown>) => ({
  assetCode: v.code,
  name: v.name,
  furnitureType: v.category || null,
  serialNumber: v.serial || null,
  purchaseDate: v.purchaseDate || null,
  assetValue: v.value,
  condition: v.condition || null,
  status: v.operationalStatus,
})
const path = (id: string) => `/api/furnitures/${encodeURIComponent(id)}`
const nullable = z.unknown().nullable()

/** GET /api/furnitures requires roomId; without a room there is nothing to list. */
export async function realFurnitures(branchId: string, filters: Record<string, string>) {
  const size = Number(filters.size || 8)
  if (!filters.roomId) return { content: [], number: 1, size, totalElements: 0, totalPages: 0 }
  const page = await request(
    'managerFurnitures',
    `/api/furnitures?${new URLSearchParams({
      roomId: filters.roomId,
      ...(filters.status ? { status: filters.status } : {}),
      page: String(Number(filters.page || 0) + 1),
      size: String(size),
    })}`,
    pageSchema(furnitureSchema),
  )
  return { ...page, content: page.content.map((f) => toAsset(branchId, f)) }
}
export async function realFurniture(branchId: string, id: string) {
  const [furniture, warranties] = await Promise.all([
    request('managerFurnitures', path(id), furnitureSchema),
    request('managerFurnitures', `${path(id)}/warranties`, z.array(warrantySchema)),
  ])
  return toAsset(branchId, furniture, warranties)
}
/** Maps the generic manager actions used by the furniture screens onto the BE routes. */
export function realFurnitureAction(
  id: string | null,
  action: string,
  value: Record<string, unknown>,
  method: 'POST' | 'PATCH' | 'DELETE',
) {
  if (action === 'warranty-save' || action === 'warranty-delete') {
    const warrantyId = value.warrantyId as string | undefined
    const target = `${path(id!)}/warranties${warrantyId ? `/${encodeURIComponent(warrantyId)}` : ''}`
    if (action === 'warranty-delete')
      return request('managerFurnitures', target, nullable, write('DELETE'))
    return request(
      'managerFurnitures',
      target,
      warrantySchema,
      write(warrantyId ? 'PUT' : 'POST', {
        provider: value.provider,
        startDate: value.startDate,
        endDate: value.endDate,
        terms: value.terms || null,
        warrantyCode: value.reference || null,
      }),
    )
  }
  if (!id && method === 'POST')
    return request(
      'managerFurnitures',
      '/api/furnitures',
      furnitureSchema,
      write('POST', { roomId: value.roomId, ...toBody(value) }),
    )
  if (id && method === 'PATCH')
    return request('managerFurnitures', path(id), furnitureSchema, write('PUT', toBody(value)))
  // P1-110: furniture is retired through status, never hard-deleted.
  return Promise.reject(new Error('Tài sản không thể xóa; chuyển trạng thái sang Ngừng sử dụng.'))
}

/** P2-29: warranties ending within 30 days and furniture reported in at least 2 repairs. */
export async function furnitureInsights(roomId: string) {
  const [expiring, failures] = await Promise.all([
    request(
      'managerFurnitures',
      `/api/furnitures/warranties/expiring?${new URLSearchParams({ roomId, withinDays: '30' })}`,
      z.array(warrantySchema),
    ),
    request(
      'managerFurnitures',
      `/api/furnitures/frequent-failures?${new URLSearchParams({ roomId, minRequests: '2' })}`,
      z.array(z.object({ furnitureId: z.uuid(), requestCount: z.number() })),
    ),
  ])
  return {
    expiring: expiring.map((w) => ({ ...toWarranty(w), furnitureId: w.furnitureId })),
    failures,
  }
}
