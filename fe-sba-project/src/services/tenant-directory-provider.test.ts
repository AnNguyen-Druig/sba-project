import { beforeEach, afterEach, describe, it, expect, vi } from 'vitest'
const branchId = '10000000-0000-4000-8000-000000000010'
const tenancy = {
  contractTenantId: '10000000-0000-4000-8000-000000009001',
  contractId: '10000000-0000-4000-8000-000000009002',
  branchId,
  roomId: '10000000-0000-4000-8000-000000009003',
  roomSlotId: null,
  tenantUserId: '10000000-0000-4000-8000-000000009004',
  fullName: 'Trần Bảo Ngọc',
  role: 'REPRESENTATIVE',
  status: 'ACTIVE',
  startDate: '2026-01-01',
  endDate: '2026-12-31',
  actualLeaveDate: null,
  branchName: 'TroPro Thủ Đức',
  roomCode: 'P101',
  slotName: null,
  contractCode: 'HD-001',
  contractStatus: 'ACTIVE',
  rentalMode: 'ROOM',
  monthlyRent: 3500000,
  firstMonthRent: 3500000,
  lastMonthRent: 3500000,
  prepaidFirstRequired: 3500000,
  prepaidLastRequired: 0,
  terms: 'Điều khoản riêng',
  contractTerms: 'Điều khoản chung',
}
const page = (content: unknown[], totalPages = 1) => ({
  content,
  number: 1,
  size: 100,
  totalElements: content.length,
  totalPages,
})
const json = (data: unknown, status = 200) =>
  new Response(JSON.stringify({ status, code: 0, message: 'API result', data }), { status })
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
describe('Tenant directory provider', () => {
  it('follows the tenant/manager workspace provider unless overridden', async () => {
    vi.stubEnv('VITE_API_MODE', 'hybrid')
    vi.stubEnv('VITE_MANAGER_TENANTS_PROVIDER', 'real')
    const { provider } = await import('./config')
    expect(provider('tenancies')).toBe('mock')
    expect(provider('managerTenants')).toBe('real')
  })
  it('maps GET /api/tenants/me into the tenant context', async () => {
    const fetch = vi.fn().mockResolvedValue(json(page([tenancy])))
    vi.stubGlobal('fetch', fetch)
    const { tenantApi } = await import('./tenant')
    const [t] = await tenantApi.tenancies()
    expect(fetch.mock.calls[0][0]).toBe('https://api.example.test/api/tenants/me?page=1&size=100')
    expect(t).toMatchObject({
      id: tenancy.contractTenantId,
      userId: tenancy.tenantUserId,
      roomName: 'P101',
      code: 'HD-001',
      sharedTerms: 'Điều khoản chung',
      document: null,
    })
  })
  it('lists manager tenants by branch (or room) with a 1-based page', async () => {
    const fetch = vi.fn().mockImplementation(() => Promise.resolve(json(page([tenancy]))))
    vi.stubGlobal('fetch', fetch)
    const { managerApi } = await import('./manager')
    const result = await managerApi.list(branchId, 'tenants', { page: '0', q: 'ignored' })
    expect(fetch.mock.calls[0][0]).toBe(
      `https://api.example.test/api/tenants?branchId=${branchId}&page=1&size=8`,
    )
    expect(result.content[0]).toMatchObject({ userName: 'Trần Bảo Ngọc', email: '', phone: null })
    await managerApi.list(branchId, 'tenants', { roomId: tenancy.roomId, page: '1' })
    expect(fetch.mock.calls[1][0]).toBe(
      `https://api.example.test/api/tenants?roomId=${tenancy.roomId}&page=2&size=8`,
    )
  })
  it('resolves a tenancy detail from the branch directory and 404s when absent', async () => {
    vi.stubGlobal(
      'fetch',
      vi
        .fn()
        .mockResolvedValueOnce(json(page([], 2)))
        .mockResolvedValueOnce(json(page([tenancy], 2)))
        .mockResolvedValue(json(page([]))),
    )
    const { managerApi } = await import('./manager')
    const found = await managerApi.get(branchId, 'tenants', tenancy.contractTenantId)
    expect(found.id).toBe(tenancy.contractTenantId)
    await expect(managerApi.get(branchId, 'tenants', branchId)).rejects.toMatchObject({
      status: 404,
    })
  })
})
