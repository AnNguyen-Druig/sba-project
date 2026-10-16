import { z } from 'zod'
import { request, write } from './http'
import { pageSchema, type Page } from '@/interfaces/domain'
import {
  branchAssignmentSchema,
  managerSchemas,
  reportSummarySchema,
  invoiceSourcesSchema,
  invoiceInputSchema,
  type ManagerResource,
  type ManagerRecord,
} from '@/interfaces/manager'
import { provider } from './config'
import { utilitiesService } from './utilities'
const scope = (branchId: string) => `/api/manager/branches/${encodeURIComponent(branchId)}`
const result = z.object({ id: z.string().uuid().optional() }).passthrough().nullable()
export const managerApi = {
  previewCharge: (branchId: string, value: unknown) =>
    request(
      'manager',
      `${scope(branchId)}/charge-preview`,
      managerSchemas['utility-charges'],
      write('POST', value),
    ),
  invoiceEditor: (branchId: string, id: string) =>
    request(
      'manager',
      `${scope(branchId)}/invoices/${encodeURIComponent(id)}/editor`,
      invoiceInputSchema,
    ),
  branches: () => request('manager', '/api/manager/branches', z.array(branchAssignmentSchema)),
  list: async <K extends ManagerResource>(
    branchId: string,
    resource: K,
    filters: Record<string, string> = {},
  ): Promise<Page<ManagerRecord<K>>> => {
    if (resource === 'services' && provider('utilities') === 'real') {
      const raw = await utilitiesService.catalog.list(branchId, {
        page: Number(filters.page || 0) + 1,
        size: Number(filters.size || 8),
      })
      return {
        ...raw,
        number: Math.max(0, raw.number - 1),
        content: raw.content.map((item) => ({
          id: item.id,
          branchId: item.branchId,
          code: item.code,
          name: item.name,
          unit: item.unit,
          type: item.serviceType,
          billingMethod: item.billingMethod,
          unitPrice: item.referencePrice,
          mandatory: item.mandatory,
          active: item.active,
        })) as unknown as ManagerRecord<K>[],
      }
    }
    return request(
      'manager',
      `${scope(branchId)}/${resource}?${new URLSearchParams({ page: '0', size: '8', ...filters })}`,
      pageSchema(managerSchemas[resource]) as z.ZodType<Page<ManagerRecord<K>>>,
    )
  },
  get: async <K extends ManagerResource>(
    branchId: string,
    resource: K,
    id: string,
  ): Promise<ManagerRecord<K>> => {
    if (resource === 'services' && provider('utilities') === 'real') {
      const item = await utilitiesService.catalog.get(id)
      return {
        id: item.id,
        branchId: item.branchId,
        code: item.code,
        name: item.name,
        unit: item.unit,
        type: item.serviceType,
        billingMethod: item.billingMethod,
        unitPrice: item.referencePrice,
        mandatory: item.mandatory,
        active: item.active,
      } as unknown as ManagerRecord<K>
    }
    return request(
      'manager',
      `${scope(branchId)}/${resource}/${encodeURIComponent(id)}`,
      managerSchemas[resource] as unknown as z.ZodType<ManagerRecord<K>>,
    )
  },
  action: async (
    branchId: string,
    resource: ManagerResource,
    id: string | null,
    action: string,
    value: unknown,
    method: 'POST' | 'PATCH' | 'DELETE' = 'POST',
  ) => {
    if (resource === 'services' && provider('utilities') === 'real') {
      if (method === 'POST') {
        const v = value as {
          code: string
          name: string
          unit: string
          type: 'ELECTRICITY' | 'WATER' | 'INTERNET' | 'PARKING' | 'CLEANING' | 'SECURITY' | 'OTHER'
          billingMethod: 'METERED' | 'PER_PERSON' | 'PER_ROOM' | 'FIXED' | 'PER_USE'
          unitPrice: number
          mandatory?: boolean
        }
        const created = await utilitiesService.catalog.create(branchId, {
          code: v.code,
          name: v.name,
          unit: v.unit,
          serviceType: v.type,
          billingMethod: v.billingMethod,
          referencePrice: v.unitPrice,
          mandatory: v.mandatory ?? false,
        })
        return { id: created.id }
      }
      if (method === 'PATCH') {
        const v = value as {
          name?: string
          unit?: string
          billingMethod?: 'METERED' | 'PER_PERSON' | 'PER_ROOM' | 'FIXED' | 'PER_USE'
          unitPrice?: number
          active?: boolean
        }
        const updated = await utilitiesService.catalog.update(id!, {
          name: v.name,
          unit: v.unit,
          billingMethod: v.billingMethod,
          referencePrice: v.unitPrice,
          active: v.active,
        })
        return { id: updated.id }
      }
    }
    if (resource === 'room-services' && provider('utilities') === 'real') {
      if (method === 'POST') {
        const v = value as {
          roomId: string
          serviceId: string
          startDate: string
          endDate: string
          unitPrice: number
          billingConfig?: unknown
        }
        const created = await utilitiesService.roomServices.configure(branchId, v)
        return { id: created.id }
      }
      if (method === 'PATCH' && id) {
        const v = value as {
          endDate?: string
          unitPrice?: number
          billingConfig?: unknown
          status?: 'ACTIVE' | 'ENDED'
        }
        const updated = await utilitiesService.roomServices.update(id, v)
        return { id: updated.id }
      }
    }
    if (resource === 'service-requests' && provider('utilities') === 'real' && id) {
      const v = value as { decision: string; reason?: string }
      const decision =
        v.decision === 'approve' ? 'APPROVED' : v.decision === 'reject' ? 'REJECTED' : (v.decision as any)
      const updated = await utilitiesService.contractServices.process(id, {
        decision,
        reason: v.reason,
      })
      return { id: updated.id }
    }
    if (resource === 'utility-charges' && provider('utilities') === 'real') {
      if (action === 'finalize') {
        const chargeIds = id ? [id] : ((value as { chargeIds?: string[] })?.chargeIds || [])
        await utilitiesService.charges.finalize(chargeIds)
        return null
      }
      if (method === 'POST') {
        const v = value as any
        const first = v.segments?.[0]
        const created = await utilitiesService.charges.create({
          roomServiceId: v.serviceId || v.roomServiceId,
          billingMonth: v.billingMonth,
          startDate: first?.startDate || v.startDate,
          endDate: first?.endDate || v.endDate,
          segmentNumber: 1,
          meterReference: first?.meterReference,
          segmentReason: first?.reason || 'NORMAL',
          startReading: first?.previousReading,
          endReading: first?.currentReading,
          quantity: first?.quantity,
          unitPrice: first?.unitPrice || 0,
        })
        return { id: created.id }
      }
    }
    if (resource === 'cost-allocations' && provider('utilities') === 'real' && id) {
      const v = value as { decision: 'APPROVED' | 'REJECTED'; reason?: string }
      const updated = await utilitiesService.costAllocation.process(id, v)
      return { id: updated.id }
    }
    return request(
      'manager',
      `${scope(branchId)}/${resource}${id ? `/${encodeURIComponent(id)}` : ''}${action ? `/${action}` : ''}`,
      result,
      write(method, value),
    )
  },
  users: (branchId: string) =>
    request(
      'manager',
      `${scope(branchId)}/eligible-users`,
      z.array(
        z.object({
          id: z.string().uuid(),
          fullName: z.string(),
          email: z.string(),
          phone: z.string().nullable(),
        }),
      ),
    ),
  sources: (branchId: string, tenancyId: string, month: string, invoiceId?: string) =>
    request(
      'manager',
      `${scope(branchId)}/invoice-sources?${new URLSearchParams({ contractTenantId: tenancyId, month, ...(invoiceId ? { invoiceId } : {}) })}`,
      invoiceSourcesSchema,
    ),
  summary: (branchId: string, month: string) =>
    request(
      'manager',
      `${scope(branchId)}/summary?month=${encodeURIComponent(month)}`,
      reportSummarySchema,
    ),
  zalo: (branchId: string, value: unknown) =>
    request(
      'manager',
      `${scope(branchId)}/settings/zalo`,
      branchAssignmentSchema,
      write('PUT', value),
    ),
  demo: (scenario: 'reset' | 'revoke', branchId: string) =>
    request(
      'manager',
      '/api/manager/demo/scenario',
      z.null(),
      write('POST', { scenario, branchId }),
    ),
}
