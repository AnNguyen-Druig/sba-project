import { z } from 'zod'
import { ApiError, request, write } from './http'
import { provider } from './config'
import { tenantPageSchema, toTenancy, type TenantResponse } from './tenant'
import { realFurniture, realFurnitureAction, realFurnitures } from './manager-furniture'
import { realMaintenance, realMaintenanceAction, realMaintenanceList } from './manager-maintenance'
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
const scope = (branchId: string) => `/api/manager/branches/${encodeURIComponent(branchId)}`
const toManagerTenancy = (t: TenantResponse) => ({
  ...toTenancy(t),
  userName: t.fullName,
  // BE exposes the display name only (P1-31); contact data is not part of the contract.
  email: '',
  phone: null,
  sourceBookingId: null,
})
// GET /api/tenants takes exactly one of branchId/roomId/contractId and a 1-based page.
const realTenants = async (branchId: string, filters: Record<string, string>) => {
  const page = await request(
    'managerTenants',
    `/api/tenants?${new URLSearchParams({
      ...(filters.roomId ? { roomId: filters.roomId } : { branchId }),
      page: String(Number(filters.page || 0) + 1),
      size: filters.size || '8',
    })}`,
    tenantPageSchema,
  )
  return { ...page, content: page.content.map(toManagerTenancy) }
}
// No GET /api/tenants/{id}: scan the branch directory.
const realTenancy = async (branchId: string, id: string) => {
  for (let page = 0; ; page++) {
    const result = await realTenants(branchId, { page: String(page), size: '100' })
    const found = result.content.find((t) => t.id === id)
    if (found) return found
    if (page + 1 >= result.totalPages) throw new ApiError('Không tìm thấy quan hệ thuê.', 404)
  }
}
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
  list: <K extends ManagerResource>(
    branchId: string,
    resource: K,
    filters: Record<string, string> = {},
  ) =>
    resource === 'tenants' && provider('managerTenants') === 'real'
      ? (realTenants(branchId, filters) as Promise<Page<ManagerRecord<K>>>)
      : resource === 'furnitures' && provider('managerFurnitures') === 'real'
        ? (realFurnitures(branchId, filters) as unknown as Promise<Page<ManagerRecord<K>>>)
        : resource === 'maintenance' && provider('managerMaintenance') === 'real'
          ? (realMaintenanceList(filters) as Promise<Page<ManagerRecord<K>>>)
          : request(
              'manager',
              `${scope(branchId)}/${resource}?${new URLSearchParams({ page: '0', size: '8', ...filters })}`,
              pageSchema(managerSchemas[resource]) as z.ZodType<Page<ManagerRecord<K>>>,
            ),
  get: <K extends ManagerResource>(branchId: string, resource: K, id: string) =>
    resource === 'tenants' && provider('managerTenants') === 'real'
      ? (realTenancy(branchId, id) as Promise<ManagerRecord<K>>)
      : resource === 'furnitures' && provider('managerFurnitures') === 'real'
        ? (realFurniture(branchId, id) as unknown as Promise<ManagerRecord<K>>)
        : resource === 'maintenance' && provider('managerMaintenance') === 'real'
          ? (realMaintenance(id) as Promise<ManagerRecord<K>>)
          : request(
              'manager',
              `${scope(branchId)}/${resource}/${encodeURIComponent(id)}`,
              managerSchemas[resource] as unknown as z.ZodType<ManagerRecord<K>>,
            ),
  action: (
    branchId: string,
    resource: ManagerResource,
    id: string | null,
    action: string,
    value: unknown,
    method: 'POST' | 'PATCH' | 'DELETE' = 'POST',
  ) =>
    resource === 'furnitures' && provider('managerFurnitures') === 'real'
      ? (realFurnitureAction(id, action, value as Record<string, unknown>, method) as Promise<{
          id?: string
        } | null>)
      : resource === 'maintenance' && provider('managerMaintenance') === 'real'
        ? (realMaintenanceAction(id!, action, value as Record<string, unknown>) as Promise<{
            id?: string
          } | null>)
        : request(
            'manager',
            `${scope(branchId)}/${resource}${id ? `/${encodeURIComponent(id)}` : ''}${action ? `/${action}` : ''}`,
            result,
            write(method, value),
          ),
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
