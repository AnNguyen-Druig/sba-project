import { beforeEach, afterEach, describe, it, expect, vi } from 'vitest'
const branchId = '10000000-0000-4000-8000-000000000010'
const roomId = '10000000-0000-4000-8000-000000009003'
const requestId = '10000000-0000-4000-8000-000000009400'
const costId = '10000000-0000-4000-8000-000000009800'
const ownerShare = '10000000-0000-4000-8000-000000009801'
const tenantShare = '10000000-0000-4000-8000-000000009802'
const record = (over: Record<string, unknown>) => ({
  id: costId,
  recordType: 'COST',
  parentCostRecordId: null,
  responsibleParty: null,
  amount: 500000,
  description: 'Thay tụ máy lạnh',
  performedBy: null,
  statusAfter: null,
  approvalStatus: 'PENDING',
  occurredAt: '2026-10-02T08:00:00Z',
  ...over,
})
const detail = {
  id: requestId,
  roomId,
  title: 'Máy lạnh chảy nước',
  priority: 'HIGH',
  status: 'IN_PROGRESS',
  assignedTo: null,
  createdAt: '2026-10-01T08:00:00Z',
  contractTenantId: null,
  description: 'Nước nhỏ giọt.',
  furnitureItems: [],
  serviceItems: [],
  rating: 4,
  ratingComment: 'Nhanh',
  records: [
    record({}),
    record({
      id: ownerShare,
      recordType: 'LIABILITY',
      parentCostRecordId: costId,
      responsibleParty: 'OWNER',
      amount: 300000,
    }),
    record({
      id: tenantShare,
      recordType: 'LIABILITY',
      parentCostRecordId: costId,
      responsibleParty: 'TENANT',
      amount: 200000,
      approvalStatus: 'APPROVED',
    }),
  ],
}
const json = (data: unknown, status = 200) =>
  new Response(JSON.stringify({ status, code: 0, message: 'API result', data }), { status })
type Fetch = (url: string, init?: RequestInit) => Promise<Response>
const calls = (fetch: ReturnType<typeof vi.fn<Fetch>>) =>
  fetch.mock.calls.map(
    ([url, init]) => `${init?.method || 'GET'} ${url.replace('https://api.example.test', '')}`,
  )
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
describe('Manager maintenance provider', () => {
  it('lists by room only and folds liabilities into their cost record', async () => {
    const fetch = vi.fn<Fetch>((url) =>
      Promise.resolve(
        json(
          url.includes('/api/furnitures')
            ? { content: [], number: 1, size: 100, totalElements: 0, totalPages: 0 }
            : url.includes('/services')
              ? []
              : detail,
        ),
      ),
    )
    vi.stubGlobal('fetch', fetch)
    const { managerApi } = await import('./manager')
    expect((await managerApi.list(branchId, 'maintenance', { page: '0' })).content).toEqual([])
    expect(fetch).not.toHaveBeenCalled()
    const report = await managerApi.get(branchId, 'maintenance', requestId)
    expect(report.records).toHaveLength(1)
    expect(report.records[0]).toMatchObject({
      cost: 500000,
      approvalStatus: 'PENDING',
      responsibleParty: null,
    })
    expect(report.records[0].description).toContain('Chủ nhà: 300000')
    expect(report).toMatchObject({ rating: 4, ratingComment: 'Nhanh' })
  })
  it('maps transitions, assignment and a cost record with its liability', async () => {
    const fetch = vi.fn<Fetch>(() => Promise.resolve(json({ id: costId })))
    vi.stubGlobal('fetch', fetch)
    const { managerApi } = await import('./manager')
    await managerApi.action(branchId, 'maintenance', requestId, 'accept', {
      reason: 'Đã nhận yêu cầu',
    })
    await managerApi.action(branchId, 'maintenance', requestId, 'assign', { handler: ownerShare })
    await managerApi.action(branchId, 'maintenance', requestId, 'record', {
      description: 'Thay tụ',
      cost: 500000,
      responsibleParty: 'TENANT',
    })
    await managerApi.action(branchId, 'maintenance', requestId, 'record', {
      description: 'Kiểm tra',
      cost: 0,
      responsibleParty: null,
    })
    const base = `/api/maintenance-requests/${requestId}`
    expect(calls(fetch)).toEqual([
      `POST ${base}/accept`,
      `POST ${base}/assign`,
      `POST ${base}/records`,
      `POST ${base}/records/${costId}/liabilities`,
      `POST ${base}/records`,
    ])
    const body = (n: number) => JSON.parse(String(fetch.mock.calls[n][1]!.body))
    expect(body(0)).toEqual({ note: 'Đã nhận yêu cầu' })
    expect(body(1)).toEqual({ assigneeUserId: ownerShare })
    expect(body(2)).toMatchObject({ recordType: 'COST', amount: 500000 })
    expect(body(3)).toEqual({ responsibleParty: 'TENANT', amount: 500000, description: 'Thay tụ' })
    expect(body(4)).toMatchObject({ recordType: 'WORK', description: 'Kiểm tra' })
  })
  it('approves pending liabilities before the cost, and loads branch cost stats', async () => {
    const fetch = vi.fn<Fetch>((url) =>
      Promise.resolve(
        json(
          url.includes('cost-stats')
            ? [{ roomId, responsibleParty: 'TENANT', totalAmount: 200000 }]
            : url.endsWith(requestId)
              ? detail
              : null,
        ),
      ),
    )
    vi.stubGlobal('fetch', fetch)
    const { managerApi } = await import('./manager')
    const { costStats } = await import('./manager-maintenance')
    await managerApi.action(branchId, 'maintenance', requestId, 'cost-approve', {
      recordId: costId,
    })
    const base = `/api/maintenance-requests/${requestId}`
    expect(calls(fetch)).toEqual([
      `GET ${base}`,
      `POST ${base}/records/${ownerShare}/approve`,
      `POST ${base}/records/${costId}/approve`,
    ])
    expect(await costStats(branchId)).toHaveLength(1)
    expect(fetch.mock.calls[3][0]).toContain(`/cost-stats?branchId=${branchId}`)
  })
})
describe('Student 6 phase 2 helpers', () => {
  it('rates a completed request and loads furniture insights', async () => {
    const fetch = vi.fn<Fetch>((url) =>
      Promise.resolve(
        json(
          url.includes('/rating')
            ? { ...detail, contractTenantId: requestId, rating: 5, ratingComment: null }
            : url.includes('/expiring') || url.includes('/frequent') || url.includes('/services')
              ? []
              : { content: [], number: 1, size: 100, totalElements: 0, totalPages: 0 },
        ),
      ),
    )
    vi.stubGlobal('fetch', fetch)
    const { tenantApi } = await import('./tenant')
    const rated = await tenantApi.rateReport(roomId, requestId, 5, '')
    expect(rated.rating).toBe(5)
    expect(JSON.parse(String(fetch.mock.calls[0][1]!.body))).toEqual({ rating: 5, comment: null })
    const { furnitureInsights } = await import('./manager-furniture')
    expect(await furnitureInsights(roomId)).toEqual({ expiring: [], failures: [] })
    expect(calls(fetch).slice(-2)).toEqual([
      `GET /api/furnitures/warranties/expiring?roomId=${roomId}&withinDays=30`,
      `GET /api/furnitures/frequent-failures?roomId=${roomId}&minRequests=2`,
    ])
  })
})
