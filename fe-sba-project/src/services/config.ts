export type Feature =
  'auth' | 'catalog' | 'leasing' | 'profile' | 'notifications' | 'recovery' | 'tenant' | 'manager' | 'owner' | 'admin' | 'adminUsers'
export type ApiMode = 'mock' | 'real' | 'hybrid'
const configured = import.meta.env.VITE_API_MODE || 'mock'
if (!['mock', 'real', 'hybrid'].includes(configured))
  throw new Error('VITE_API_MODE phải là mock, real hoặc hybrid.')
export const apiMode = configured as ApiMode
export function provider(feature: Feature): 'mock' | 'real' {
  const override = import.meta.env[`VITE_${feature === 'adminUsers' ? 'ADMIN_USERS' : feature.toUpperCase()}_PROVIDER`]
  if (override && !['mock', 'real'].includes(override))
    throw new Error(`Provider của ${feature} chưa hợp lệ.`)
  if (override) return override
  if (apiMode !== 'hybrid') return apiMode
  return feature === 'auth' || feature === 'notifications' || feature === 'adminUsers' ? 'real' : 'mock'
}
export const apiBase = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '')
export const hasMocks = (
  [
    'auth',
    'catalog',
    'leasing',
    'profile',
    'notifications',
    'recovery',
    'tenant',
    'manager',
    'owner',
    'admin',
    'adminUsers',
  ] as Feature[]
).some((f) => provider(f) === 'mock')
export function endpoint(feature: Feature, path: string) {
  return provider(feature) === 'mock' ? `/__mock${path}` : `${apiBase}${path}`
}
