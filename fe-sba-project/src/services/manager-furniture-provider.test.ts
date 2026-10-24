import { beforeEach, afterEach, describe, it, expect, vi } from 'vitest'
const branchId = '10000000-0000-4000-8000-000000000010'
const roomId = '10000000-0000-4000-8000-000000009003'
const furnitureId = '10000000-0000-4000-8000-000000009100'
const warrantyId = '10000000-0000-4000-8000-000000009200'
const furniture = {
  id: furnitureId,
  roomId,
  assetCode: 'TS-01',
  name: 'Máy lạnh',
  furnitureType: 'Điện lạnh',
  serialNumber: null,
  purchaseDate: '2026-01-10',
  assetValue: 7000000,
  condition: 'Tốt',
  status: 'IN_USE',
}
const warranty = {
  id: warrantyId,
  furnitureId,
  provider: 'Daikin',
  startDate: '2026-01-10',
  endDate: '2027-01-10',
  terms: null,
  warrantyCode: 'BH-01',
}
const json = (data: unknown, status = 200) =>
  new Response(JSON.stringify({ status, code: 0, message: 'API result', data }), { status })
const body = (fetch: ReturnType<typeof vi.fn>, n: number) =>
  JSON.parse(String((fetch.mock.calls[n][1] as RequestInit).body))
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
describe('Furniture provider', () => {
  it('needs a room before listing and maps the room-scoped 1-based page', async () => {
    const fetch = vi
      .fn()
      .mockResolvedValue(
        json({ content: [furniture], number: 1, size: 8, totalElements: 1, totalPages: 1 }),
      )
    vi.stubGlobal('fetch', fetch)
    const { managerApi } = await import('./manager')
    expect((await managerApi.list(branchId, 'furnitures', { page: '0' })).content).toEqual([])
    expect(fetch).not.toHaveBeenCalled()
    const result = await managerApi.list(branchId, 'furnitures', { roomId, page: '0', q: 'x' })
    expect(fetch.mock.calls[0][0]).toBe(
      `https://api.example.test/api/furnitures?roomId=${roomId}&page=1&size=8`,
    )
    expect(result.content[0]).toMatchObject({
      code: 'TS-01',
      category: 'Điện lạnh',
      serial: '',
      value: 7000000,
      operationalStatus: 'IN_USE',
    })
  })
  it('loads detail with its warranties', async () => {
    vi.stubGlobal(
      'fetch',
      vi
        .fn()
        .mockImplementation((url: string) =>
          Promise.resolve(json(url.endsWith('/warranties') ? [warranty] : furniture)),
        ),
    )
    const { managerApi } = await import('./manager')
    const asset = await managerApi.get(branchId, 'furnitures', furnitureId)
    expect(asset.warranties).toEqual([
      {
        id: warrantyId,
        provider: 'Daikin',
        reference: 'BH-01',
        startDate: '2026-01-10',
        endDate: '2027-01-10',
        terms: '',
      },
    ])
  })
  it('maps create/update/warranty actions to the BE routes and rejects hard delete', async () => {
    const fetch = vi
      .fn()
      .mockResolvedValueOnce(json(furniture, 201))
      .mockResolvedValueOnce(json(furniture))
      .mockResolvedValueOnce(json(warranty, 201))
      .mockResolvedValueOnce(json(warranty))
      .mockResolvedValueOnce(json(null))
    vi.stubGlobal('fetch', fetch)
    const { managerApi } = await import('./manager')
    const form = {
      roomId,
      code: 'TS-01',
      name: 'Máy lạnh',
      category: '',
      serial: '',
      purchaseDate: '2026-01-10',
      value: 7000000,
      condition: 'Tốt',
      operationalStatus: 'IN_USE',
    }
    await managerApi.action(branchId, 'furnitures', null, '', form, 'POST')
    expect(fetch.mock.calls[0][0]).toBe('https://api.example.test/api/furnitures')
    expect(body(fetch, 0)).toEqual({
      roomId,
      assetCode: 'TS-01',
      name: 'Máy lạnh',
      furnitureType: null,
      serialNumber: null,
      purchaseDate: '2026-01-10',
      assetValue: 7000000,
      condition: 'Tốt',
      status: 'IN_USE',
    })
    await managerApi.action(branchId, 'furnitures', furnitureId, '', form, 'PATCH')
    expect(fetch.mock.calls[1][1].method).toBe('PUT')
    expect(body(fetch, 1).roomId).toBeUndefined()
    const w = {
      provider: 'Daikin',
      reference: 'BH-01',
      startDate: '2026-01-10',
      endDate: '2027-01-10',
      terms: '',
    }
    await managerApi.action(branchId, 'furnitures', furnitureId, 'warranty-save', w)
    expect(fetch.mock.calls[2][0]).toBe(
      `https://api.example.test/api/furnitures/${furnitureId}/warranties`,
    )
    expect(body(fetch, 2)).toEqual({
      ...w,
      reference: undefined,
      terms: null,
      warrantyCode: 'BH-01',
    })
    await managerApi.action(branchId, 'furnitures', furnitureId, 'warranty-save', {
      ...w,
      warrantyId,
    })
    expect(fetch.mock.calls[3][1].method).toBe('PUT')
    expect(fetch.mock.calls[3][0]).toContain(`/warranties/${warrantyId}`)
    await managerApi.action(branchId, 'furnitures', furnitureId, 'warranty-delete', { warrantyId })
    expect(fetch.mock.calls[4][1].method).toBe('DELETE')
    await expect(
      managerApi.action(branchId, 'furnitures', furnitureId, '', {}, 'DELETE'),
    ).rejects.toThrow()
    expect(fetch).toHaveBeenCalledTimes(5)
  })
})
