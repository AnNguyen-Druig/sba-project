import type { Feature } from './config'

/** Only contracts verified against the current backend are enabled for real requests.
 * See docs/frontend/real-api-integration-audit.md for pending contracts and decisions.
 */
export function realApiIssue(feature: Feature, path: string, method = 'GET'): string | null {
  const pathname = path.split('?')[0]
  const verb = method.toUpperCase()
  const id = '[0-9a-fA-F-]{36}'
  const matches = (pattern: string) => new RegExp(`^${pattern}$`).test(pathname)

  if (feature === 'auth' && verb === 'POST' &&
      matches('/api/auth/(login|register|verify-email|refresh|logout)')) return null

  if (feature === 'catalog' && verb === 'GET' &&
      matches(`/api/v1/public/rooms(/${id})?`)) return null

  if (feature === 'adminUsers') {
    if (pathname === '/api/users' && ['GET', 'POST'].includes(verb)) return null
    if (matches(`/api/users/${id}`) && verb === 'GET') return null
    if (matches(`/api/users/${id}/active-status`) && verb === 'PATCH') return null
  }

  if (feature === 'notifications') {
    if (verb === 'GET' && matches(`/api/notifications(/unread-count|/${id})?`)) return null
    if (verb === 'PUT' && matches('/api/notifications/(mark-read|mark-all-read)')) return null
  }

  // Student 6 (docs/backend/STUDENT6_API.md): BE enforces JWT principal + Branch/tenancy scope.
  if (feature === 'adminVerifications') {
    if (verb === 'GET' && pathname === '/api/admin/owner-verifications') return null
    if (verb === 'POST' && matches(`/api/admin/owner-verifications/${id}/(approve|reject)`)) return null
  }

  if (feature === 'tenancies' && verb === 'GET' && pathname === '/api/tenants/me') return null
  if (feature === 'managerTenants' && verb === 'GET' && pathname === '/api/tenants') return null

  if (feature === 'managerFurnitures') {
    if (verb === 'GET' &&
        matches(`/api/furnitures(/${id}(/warranties)?|/warranties/expiring|/frequent-failures)?`)) return null
    if (verb === 'POST' && matches(`/api/furnitures(/${id}/warranties)?`)) return null
    if (verb === 'PUT' && matches(`/api/furnitures/${id}(/warranties/${id})?`)) return null
    if (verb === 'DELETE' && matches(`/api/furnitures/${id}/warranties/${id}`)) return null
  }

  if (feature === 'tenantMaintenance') {
    if (verb === 'GET' &&
        (pathname === '/api/furnitures' || matches(`/api/rooms/${id}/services`) ||
         matches(`/api/maintenance-requests/(my|${id})`))) return null
    if (verb === 'POST' && matches(`/api/maintenance-requests(/${id}/rating)?`)) return null
  }

  if (feature === 'managerMaintenance') {
    if (verb === 'GET' && matches(`/api/maintenance-requests(/${id}|/cost-stats)?`)) return null
    if (verb === 'POST' && (
      matches(`/api/maintenance-requests/${id}/(accept|reject|start|complete|assign|evidence|records)`) ||
      matches(`/api/maintenance-requests/${id}/records/${id}/(approve|reject|liabilities)`))) return null
  }

  return 'Chức năng này hiện chưa khả dụng. Vui lòng thử lại khi có thông báo cập nhật.'
}
