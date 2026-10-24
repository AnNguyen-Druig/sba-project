import { beforeEach, afterEach, describe, it, expect, vi } from 'vitest'
const tenancyId = '10000000-0000-4000-8000-000000009001'
const roomId = '10000000-0000-4000-8000-000000009003'
const otherRoom = '10000000-0000-4000-8000-000000009004'
const furnitureId = '10000000-0000-4000-8000-000000009100'
const serviceId = '10000000-0000-4000-8000-000000009300'
const requestId = '10000000-0000-4000-8000-000000009400'
const summary = {
  id: requestId,
  roomId,
  requesterUserId: '10000000-0000-4000-8000-000000009500',
  title: 'Máy lạnh chảy nước',
  priority: 'HIGH',
  status: 'ACCEPTED',
  assignedTo: '10000000-0000-4000-8000-000000009600',
  completedAt: null,
  createdAt: '2026-10-01T08:00:00Z',
}
const detail = {
  ...summary,
  contractTenantId: tenancyId,
  description: 'Nước nhỏ giọt liên tục từ dàn lạnh.',
  evidenceRefs: [],
  slaDueAt: null,
  rating: null,
  ratingComment: null,
  ratedAt: null,
  updatedAt: '2026-10-01T08:00:00Z',
  version: 1,
  furnitureItems: [{ furnitureId, damageDescription: null }],
  serviceItems: [],
  records: [
    {
      id: '10000000-0000-4000-8000-000000009700',
      reportRequestId: requestId,
      recordType: 'STATUS',
      parentCostRecordId: null,
      contractTenantId: null,
      responsibleParty: null,
      amount: null,
      description: null,
      performedBy: '10000000-0000-4000-8000-000000009600',
      assignedTo: null,
      statusAfter: 'ACCEPTED',
      approvalStatus: null,
      approvedBy: null,
      approvedAt: null,
      evidenceRefs: [],
      occurredAt: '2026-10-01T09:00:00Z',
    },
  ],
}
const furniturePage = {
  content: [
    { id: furnitureId, name: 'Máy lạnh', condition: 'Tốt', status: 'IN_USE' },
    {
      id: '10000000-0000-4000-8000-000000009101',
      name: 'Ghế cũ',
      condition: null,
      status: 'RETIRED',
    },
  ],
  number: 1,
  size: 100,
  totalElements: 2,
  totalPages: 1,
}
const services = [
  { id: serviceId, serviceName: 'Internet', status: 'ACTIVE' },
  { id: '10000000-0000-4000-8000-000000009301', serviceName: 'Giữ xe', status: 'INACTIVE' },
]
const json = (data: unknown, status = 200) =>
  new Response(JSON.stringify({ status, code: 0, message: 'API result', data }), { status })
const route = (url: string) =>
  url.includes('/api/furnitures')
    ? furniturePage
    : url.includes('/services')
      ? services
      : url.includes('/my')
        ? {
            content: [summary, { ...summary, id: furnitureId, roomId: otherRoom }],
            number: 1,
            size: 100,
            totalElements: 2,
            totalPages: 1,
          }
        : detail
beforeEach(() => {
  vi.resetModules()
  vi.stubEnv('VITE_API_MODE', 'real')
  vi.stubEnv('VITE_API_BASE_URL', 'https://api.example.test')
})
afterEach(() => {
  vi.unstubAllEnvs()
  vi.unstubAllGlobals()
  vi.resetModules()
})
describe('Tenant maintenance provider', () => {
  it('follows the tenant provider unless overridden', async () => {
    vi.stubEnv('VITE_API_MODE', 'hybrid')
    const { provider } = await import('./config')
    expect(provider('tenantMaintenance')).toBe('mock')
    vi.stubEnv('VITE_TENANT_MAINTENANCE_PROVIDER', 'real')
    vi.resetModules()
    expect((await import('./config')).provider('tenantMaintenance')).toBe('real')
  })
  it('lists only reportable furniture and active room services', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn((url: string) => Promise.resolve(json(route(url)))),
    )
    const { tenantApi } = await import('./tenant')
    expect(await tenantApi.reportOptions(roomId)).toEqual([
      { id: furnitureId, name: 'Máy lạnh', category: 'FURNITURE', condition: 'Tốt' },
      { id: serviceId, name: 'Internet', category: 'SERVICE', condition: '' },
    ])
  })
  it("narrows /my to the tenancy's room and maps detail with item names", async () => {
    const fetch = vi.fn((url: string) => Promise.resolve(json(route(url))))
    vi.stubGlobal('fetch', fetch)
    const { tenantApi } = await import('./tenant')
    const list = await tenantApi.reports(tenancyId, roomId)
    expect(fetch.mock.calls[0][0]).toBe(
      'https://api.example.test/api/maintenance-requests/my?page=1&size=100',
    )
    expect(list.map((r) => r.id)).toEqual([requestId])
    expect(list[0].assignedTo).toBe('Đã phân công')
    const report = await tenantApi.report(tenancyId, requestId, roomId)
    expect(report).toMatchObject({
      assetId: furnitureId,
      assetName: 'Máy lạnh',
      category: 'FURNITURE',
      contractTenantId: tenancyId,
      attachments: [],
    })
    expect(report.records[0]).toMatchObject({ status: 'ACCEPTED', description: '', cost: 0 })
  })
  it('creates a request for the selected furniture or service', async () => {
    const fetch = vi.fn<(url: string, init?: RequestInit) => Promise<Response>>(() =>
      Promise.resolve(json(detail, 201)),
    )
    vi.stubGlobal('fetch', fetch)
    const { tenantApi } = await import('./tenant')
    const input = {
      category: 'SERVICE' as const,
      assetId: serviceId,
      title: 'Mất mạng',
      description: 'Internet mất kết nối từ tối qua.',
      priority: 'NORMAL' as const,
      attachments: [],
    }
    const created = await tenantApi.createReport(tenancyId, input, roomId)
    expect(fetch.mock.calls[0][0]).toBe('https://api.example.test/api/maintenance-requests')
    expect(JSON.parse(String(fetch.mock.calls[0][1]!.body))).toEqual({
      roomId,
      title: 'Mất mạng',
      description: 'Internet mất kết nối từ tối qua.',
      priority: 'NORMAL',
      furnitureItems: [],
      serviceItems: [{ roomServiceId: serviceId }],
      evidence: [],
    })
    expect(created.id).toBe(requestId)
  })
})
